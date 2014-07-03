package eu.transcriptorium.lattice;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import eu.transcriptorium.lattice.TopologicalSort.LatticeException;
import eu.transcriptorium.util.ArrayUtils;

import java.util.*;

public class LatticeDecoder
{
	NgramLanguageModel<String> lm = null;
	double beamWidth = 0;
	double lmscale = 1;
	static final double LogP_Zero = Double.NEGATIVE_INFINITY;
	static final double LogP_One = 0;
	static final String Vocab_None = Lattice.nullWordSymbol; // CHECK....
	double logP_floor = 0;
	int contextLen = 2; // doe maar wat...
	int maxFanIn = 0;
	private int maxPaths = Integer.MAX_VALUE ;
	private int maxWords;

	public void setLanguageModel(NgramLanguageModel lm)
	{
		this.lm = lm;
	}

	public double decode1Best(Lattice lattice, String[] words, int maxWords, Set<String> ignoreWords,
			NgramLanguageModel lm , int contextLen, double beamwidth, double  logP_floor, 
			int maxPaths)
	{
		NBestWordInfo[] winfo = new NBestWordInfo [ maxWords + 1];

		setLanguageModel(lm);
		double result;
		try
		{
			result = decode1Best(lattice, winfo, maxWords, ignoreWords,  contextLen, logP_floor, maxPaths);
		} catch (LatticeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return LogP_Zero;
		}

		if (result != LogP_Zero) 
		{
			for (int i = 0; i < maxWords; i++) {
				words[i] = winfo[i].word;
				if (words[i] == Vocab_None)
					break;
			}
		} else {
			words[0] = Vocab_None;
		}
		return result;
	}

	public double decode1Best(Lattice lattice,  NBestWordInfo[] winfo, int maxWords, Set<String> ignoreWords, int contextLen, double logP_floor, int maxPaths) throws LatticeException
	{
		List<Node> sortedNodes = new TopologicalSort().sortNodes(lattice, false);
		this.logP_floor = logP_floor; 
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
		if (nodeinfo == null) 
		{
			return LogP_Zero;
		}
		LatticeDecodePath  path = nodeinfo[finalPosition].m_PList[0];
		double result = path.m_Prob;
		int num = 0;
		double  gprob = LogP_One;

		while (path != null && num < maxWords) 
		{
			Node  node = path.node;
			if (!ignoreWord(node.word) && ignoreWords.contains(node.word)) 
			{

				NBestWordInfo  wi = winfo[num++];

				if (node.htkinfo == null && node.word.equals(Vocab_None))  
				{
					wi.invalidate();
				} if (node.htkinfo == null) 
				{
					// only word label is available, e.g., when processing PFSGs
					wi.word = node.word;
					wi.languageScore = path.m_GProb; // this is cumulative score
				} else 
				{
					// find the time of the predecessor node
					double startTime = 0.f;
					double  lastGProb = LogP_One;

					Node  prevNode;

					if (path.m_Prev != null && ((prevNode = path.m_Prev.node) != null) && 
							prevNode.htkinfo != null) 
					{	  
						// startTime = prevNode->htkinfo->time;	  
					}


					wi.word = node.word;
					wi.start = startTime;

					//wi.duration = node->htkinfo->time - startTime;
					//wi.acousticScore = node->htkinfo->acoustic;	
					//	wi.languageScore = path->m_GProb; // this is cumulative score
				}
				wi.wordPosterior = wi.transPosterior = 1.0;
			}
			path = path.m_Prev;

		}

		if (path != null)
		{
			System.err.println("Decode1Best: word string longer than " +  maxWords);
			winfo[0].word = Vocab_None;
			return LogP_Zero;
		} else
		{
			// reverse the sequence
			winfo = (NBestWordInfo[]) ArrayUtils.reverse(winfo);
			winfo[num].word = Vocab_None;
			for (int i = num - 1; i > 0; i--) 
			{
				winfo[i].languageScore -= winfo[i-1].languageScore;
			}
		}
		return result;
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

		mainLoop: for (int n = 1 ; n <= finalPosition; n++) 
		{
			Node  node = sortedNodes.get(n);
			String word = node.word;
			NodePathInfo info = nodeinfo[n];
			double oldlmscore = info.m_OldLMScore;
			boolean nolmword = ignoreWord(word);
			boolean uselm = (lm != null) && (!nolmword);
			double probBwd = info.m_ProbBwd;

			// go through all in-transitions

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
						if (numPaths > maxPaths)
							break mainLoop;

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

			int num = info.m_PHash.size();     
			info.m_PList = new LatticeDecodePath [ num ];

			int I = 0;
			for (LatticeDecodePath ldp: info.m_PHash.keySet())
			{
				info.m_PList[I++] = ldp;
				if (nbest != 0 && maxFanIn != 0)
				{
					ldp.truncLinks(maxFanIn);
				}
			}
			info.m_PHash.clear();
			info.m_NumPaths = num;
		}

		//FINAL (mainLoop broken)

		NodePathInfo  pfinal = nodeinfo[finalPosition];

		if (pfinal.m_NumPaths == 0) 
		{
			return null;
		}

		Arrays.sort(pfinal.m_PList, new LatticeDecodePath.PathComparator());  
		return nodeinfo;
	}

	/*
	 * Todo hier klopt geen barst van
	 */
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
