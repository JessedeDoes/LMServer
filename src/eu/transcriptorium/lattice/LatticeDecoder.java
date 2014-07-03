package eu.transcriptorium.lattice;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import eu.transcriptorium.lattice.TopologicalSort.LatticeException;

import java.util.*;

public class LatticeDecoder
{
	NgramLanguageModel<String> lm;
	double beamWidth = 0;
	double lmscale = 1;
	static final double LogP_Zero = Double.NEGATIVE_INFINITY;
	static final double LogP_One = 0;
	static final String Vocab_None = Lattice.nullWordSymbol; // CHECK....
	private static final double logP_floor = 0;
	int contextLen = 2; // doe maar wat...
	private int maxPaths = Integer.MAX_VALUE ;

	public void setLanguageModel(NgramLanguageModel lm)
	{
		this.lm = lm;
	}

	public void decode1Best(Lattice lattice) throws LatticeException
	{
		List<Node> sortedNodes = new TopologicalSort().sortNodes(lattice, false);

		try
		{
			lmscale = Double.parseDouble(lattice.properties.get("lmscale"));
		} catch (Exception e)
		{
			lmscale = 1;
		}
		int numNodes = lattice.getSize();


		int finalPosition = -1;
		for (int i=0; i < sortedNodes.size(); i++)
		{
			Node n = sortedNodes.get(i);
			if (n.isFinal())
				finalPosition = i;
		}
		NodePathInfo [] nodeinfo = decode(lattice, sortedNodes, finalPosition,0);
	}

	private List<String> singleton(String w)
	{
		List<String> x = new ArrayList<String>();
		x.add(w);
		return x;
	}

	public NodePathInfo[] decode(Lattice lattice, List<Node> sortedNodes, int finalPosition, int nbest)
	{
		int nn = lattice.getSize();
		NodePathInfo [] nodeinfo = new NodePathInfo[nn];

		for (int i=0; i < nn; i++)
		{
			nodeinfo[i]  = new NodePathInfo();
		}

		int numPaths = 0;
		double thresh = LogP_Zero;

		if (this.beamWidth > 0)  // determine threshold value
		{
			nodeinfo[finalPosition].m_ProbBwd = LogP_One;
			for (int i = finalPosition; i >= 0; i --) 
			{
				Node n = sortedNodes.get(i);
				double unigramScore = LogP_One;
				if (this.lm != null)
					unigramScore = lm.getLogProb(singleton(n.word));
				double prob = nodeinfo[i].m_ProbBwd;

				for (Arc a: n.incomingArcs)
				{
					double prob2 = a.a + prob + unigramScore;
					NodePathInfo fromInfo = nodeinfo[a.source.topSortOrder];
					if (prob2 > fromInfo.m_ProbBwd)
					{
						fromInfo.m_ProbBwd = prob2;
					}
				}
			}
			thresh = nodeinfo[0].m_ProbBwd - beamWidth;
		} else
		{
			for (int i = 0; i <= finalPosition; i++)
				nodeinfo[i].m_ProbBwd = LogP_One;
		}

		// get old LM scores to correctly compute new transition weight
		if (lm != null) 
		{
			for (int i = 0; i <= finalPosition; i++)
			{
				Node node = sortedNodes.get(i);
				if (node.l != Double.NaN) // ToDo: where do we get the node weights from???
					nodeinfo[i].m_OldLMScore = node.l * lmscale; // NO.... 
				else
					nodeinfo[i].m_OldLMScore = 0;
			}
		}

		// for the initial node
		LatticeDecodePath  path0 = new LatticeDecodePath(); // this is dummy initial one
		numPaths = 1;

		path0.node = lattice.getStartNode();
		if (contextLen > 1) 
		{
			path0.m_Context.set(0,  path0.node.word);
		}

		for (int i = 1; i < contextLen - 1; i++)  
			path0.m_Context.set(i, Vocab_None);

		path0.m_Context.set(contextLen - 1, Vocab_None); 

		nodeinfo[0].m_PList = new LatticeDecodePath  [1];
		nodeinfo[0].m_PList[0] = path0;
		nodeinfo[0].m_NumPaths = 1;


		// go through all the nodes, make all transitions
		//NodeIndex n;
		for (int n = 1 ; n <= finalPosition; n++) 
		{

			Node  node = sortedNodes.get(n);
			String word = node.word;
			NodePathInfo info = nodeinfo[n];
			double oldlmscore = info.m_OldLMScore;
			boolean nolmword = ignoreWord(word);
			boolean uselm = (lm != null) && (!nolmword);
			double probBwd = info.m_ProbBwd;

			// go through all in-transitions
			//TRANSITER_T<NodeIndex,LatticeTransition> inTransIter(node->inTransitions);
			//while (LatticeTransition * inTrans = inTransIter.next(fromNodeIndex)) {

			for (Arc a: node.incomingArcs) 
			{
				Node fromNode =a.source;
				NodePathInfo  fromInfo = nodeinfo[fromNode.topSortOrder];

				for (int i = 0; i < fromInfo.m_NumPaths; i++) 
				{
					LatticeDecodePath  path = fromInfo.m_PList[i];

					double prob = path.m_Prob + a.a - oldlmscore;
					double lmscore = LogP_One;
					double gprob = LogP_One;

					if (uselm) 
					{
						gprob = wordProb(lm, word, path.m_Context);
						if (gprob < logP_floor) gprob = logP_floor;
						lmscore = gprob * lmscale;
						prob += lmscore;
					} else 
					{
						if (node.l != Double.NaN) 
							gprob = node.l ;
					}
					
					if ((prob + probBwd) >= thresh) 
					{
						  LatticeDecodePath  newpath = new LatticeDecodePath();
						  
						  numPaths ++;
						  // if (numPaths > maxPaths) goto FINAL;
						  newpath.m_Prob = prob;
						  newpath.m_GProb = path.m_GProb + gprob;
						  newpath.node = node;
						  newpath.m_Prev = path;
						  
						  if (nbest != 0) 
						  {
						    newpath.addLink(path, prob - path.m_Prob); // beware: implement this!
						  }
						  
						  shiftContext(word, nolmword, path, newpath); // reorganize this, lists are a bit silly in this respect

						  // I do not get the pathHash and merge thing yet...
						  // hoe maakt hij uniek? Do ga je nooit 
						  
						  /*
						  LatticeDecodePath ** p;
						  if ((p = info.m_PHash.find(PATH_PTR(newpath)))) {
						    LatticeDecodePath * op = *p;
						    op->merge(newpath, nbest);
						  } else {
						    *info.m_PHash.insert(PATH_PTR(newpath)) = newpath;
						  }
						  */
						} 
						
				}
			}
		}
		// sort the list
		return nodeinfo;
	}

	protected void shiftContext(String word, boolean nolmword,
			LatticeDecodePath path, LatticeDecodePath newpath)
	{
		if (nolmword) // copy from path if word is skipped by LM
		  {
			  newpath.m_Context.clear();
			  newpath.m_Context.addAll(path.m_Context);
		  } else 
		  {
		    newpath.m_Context.set(0, word);
		    for (int j=1; j < contextLen-1; j++)
		    	 newpath.m_Context.set(j,  path.m_Context.get(j-1));
		    newpath.m_Context.set(contextLen - 1, Vocab_None);
		  }
	}

	private double wordProb(NgramLanguageModel<String> lm2, String word,
			List<String> m_Context)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	boolean ignoreWord(String word)
	{
		return false;
	}
}
