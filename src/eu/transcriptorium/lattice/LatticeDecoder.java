package eu.transcriptorium.lattice;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lattice.TopologicalSort.LatticeException;
import eu.transcriptorium.lm.VariantLexicon;
import eu.transcriptorium.lm.VariantLexicon.Variant;
import eu.transcriptorium.util.ArrayUtils;

import eu.transcriptorium.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;


/**
 * This is (almost) a java clone of the SRI lattice decoder
 * <p>
 * Why is it needed?
 * <ul>
 * <li>Hyphenated models
 * <li>Output of non-normalized words (? is that really impossible with SRI)
 * <li>To be added: evaluation with ground truth, so we can use this to optimize parameters
 * </ul>
 * @author does
 *
 */
public class LatticeDecoder
{
	private NgramLanguageModel<String> lm = null;
	private double beamWidth = 0;
	private double lmscale = 20;
	private VariantLexicon variantLexicon = null;
	
	static final double LogP_Zero = Double.NEGATIVE_INFINITY;
	static final double LogP_One = 0;
	static final String Vocab_None = Lattice.nullWordSymbol; // CHECK....
	
	private double logP_floor = Double.NEGATIVE_INFINITY;
	
	private int contextLen = 2; // doe maar wat...
	private int maxFanIn = 0;
	private int maxPaths = Integer.MAX_VALUE ;
	
	//private int maxWords;
	
	private Set<String> ignoreWords = new HashSet<String>();
	
	transient private double oldLmScale = Double.NaN;
	transient private NodePathInfo [] nodeinfo = null;
	transient private List<Node> sortedNodes = null;
	transient private int finalPosition = -1;

	public void setLanguageModel(NgramLanguageModel lm)
	{
		this.lm = lm;
	}

	public void setBeamWidth(double b)
	{
		this.beamWidth = b;
	}

	public List<String> decode(Lattice lattice)
	{
		lattice.addIncomingArcs(); 
		this.resetTransient();
		int maxWords = lattice.getSize(); // meer dan dat kunnen er niet uitkomen
		List<String> words = new ArrayList<String>();
		this.decode1BestOuter(lattice, words, maxWords, getIgnoreWords(), lm, contextLen, beamWidth, Double.NEGATIVE_INFINITY, maxWords);
		return words;
	}

	private void resetTransient()
	{
		// TODO Auto-generated method stub
		nodeinfo = null;
		sortedNodes = null;
		finalPosition = -1;
		oldLmScale = Double.NaN;
	}

	private double decode1BestOuter(Lattice lattice, List<String> words, int maxWords, Set<String> ignoreWords,
			NgramLanguageModel lm , int contextLen, double beamwidth, double  logP_floor, 
			int maxPaths)
	{
		this.oldLmScale = lattice.lmscale;
		NBestWordInfo[] winfo = new NBestWordInfo [ maxWords + 1];
		for (int i=0; i < maxWords+1; i++)
			winfo[i] = new NBestWordInfo();

		setLanguageModel(lm);

		double result;

		try
		{
			result = decode1BestInner(lattice,  winfo, maxWords, ignoreWords,  contextLen, logP_floor, maxPaths);
			System.err.println("Result=" + result);
		} catch (LatticeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return LogP_Zero;
		}

		if (result != LogP_Zero) 
		{
			for (int i = 0; i < maxWords; i++) 
			{
				String w = winfo[i].word;
				if (!w.equals(Vocab_None))
				    words.add(w);
				if (w == Vocab_None)
					break;
			}
		} else
		{
			words.add(Vocab_None);
		}
		return result;
	}

	private  double decode1BestInner(Lattice lattice,  NBestWordInfo[] winfo, 
			int maxWords, Set<String> ignoreWords, 
			int contextLen, 
			double logP_floor, 
			int maxPaths) throws LatticeException
	{
		sortedNodes = new TopologicalSort().sortNodes(lattice, false);

		this.logP_floor = logP_floor; 
	
		int numNodes = lattice.getSize();
		for (int i=0; i < sortedNodes.size(); i++)
		{
			Node n = sortedNodes.get(i);
			if (n.isFinal())
				finalPosition = i;
		}

		NodePathInfo [] nodeinfo = mainDecodingLoop(lattice, 0);
		// System.err.println("Decoded: " + Arrays.asList(nodeinfo));
		if (nodeinfo == null) 
		{
			return LogP_Zero;
		}
		double result = buildNBestInfoList(winfo, maxWords, ignoreWords,
				finalPosition, nodeinfo);
		return result;
	}
	
/**
 * We do not really use this.... NBest lists are not useful right now.
 * @param winfo
 * @param maxWords
 * @param ignoreWords
 * @param finalPosition
 * @param nodeinfo
 * @return
 */
	
	protected double buildNBestInfoList(NBestWordInfo[] winfo, int maxWords,
			Set<String> ignoreWords, int finalPosition, NodePathInfo[] nodeinfo)
	{
		LatticeDecodePath  path = nodeinfo[finalPosition].m_PList[0];
		double result = path.m_Prob;
		int num = 0;
		double  gprob = LogP_One;

		while (path != null && num < maxWords) 
		{
			Node  node = path.node;

			// System.err.println("Along path: " + node);

			if (!ignoreWord(node.word) && !ignoreWords.contains(node.word)) 
			{
				NBestWordInfo  wi = winfo[num++];
				if (node.htkinfo == null && node.word.equals(Vocab_None))  
				{
					wi.invalidate();
				} 
				if (node.htkinfo == null) // only word label is available, e.g., when processing PFSGs
				{
					wi.word = node.word;
				
					wi.languageScore = path.m_GProb; // this is cumulative score
				} else // find the time of the predecessor node 
				{
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

					// wi.duration = node->htkinfo->time - startTime;
					// wi.acousticScore = node->htkinfo->acoustic;	
					// wi.languageScore = path->m_GProb; // this is cumulative score
				}
				wi.wordPosterior = wi.transPosterior = 1.0;
				if (this.variantLexicon != null)
				{
					List<Variant> l = this.variantLexicon.getVariantsFromNormalForm(wi.word);
					//System.err.println(l);
					if (l != null && l.size() >= node.v)
					{
						String variant = l.get(node.v-1).variantForm;
						if (variant != null && variant.length() > 0)
							wi.word = variant;
					}
					else;
						//System.err.println(wi.word + " v=" + node.v + " not in lexicon!");
				}  else
				{
					//System.err.println("dit is niet zo");
				}
				//System.err.println("set wi.word to " + wi.word);
			}
			path = path.m_Prev;

		}

		//System.err.println("Num = "  + num);
		if (path != null) // failure to decode
		{
			System.err.println("Decode1Best: word string longer than " +  maxWords);
			winfo[0].word = Vocab_None;
			return LogP_Zero;
		} else // reverse 
		{
			ArrayUtils.<NBestWordInfo>reverse(winfo, num);

			winfo[num].word = Vocab_None;
			for (int i = num - 1; i > 0; i--) 
			{
				winfo[i].languageScore -= winfo[i-1].languageScore; // Hm??
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

	private NodePathInfo[] mainDecodingLoop(Lattice lattice, int nbest)
	{
		int nn = lattice.getSize();
		nodeinfo = new NodePathInfo[nn];

		for (int i=0; i < nn; i++)
		{
			nodeinfo[i]  = new NodePathInfo();
			if (i < nn-1)
				nodeinfo[i].node = sortedNodes.get(i);
		}

		int numPaths = 0;
		double threshold = LogP_Zero;

		if (this.beamWidth > 0)  // determine threshold value
		{
			threshold = determineThresholdForBeamSearch(sortedNodes, finalPosition, nodeinfo);
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
				if (!Double.isNaN(node.nodeLanguageScore)) // ToDo: where do we get the node weights from??? this does not really work...
					nodeinfo[i].m_OldLMScore = node.nodeLanguageScore * oldLmScale; // NO.... 
				else
					nodeinfo[i].m_OldLMScore = 0;
			}
		}

		numPaths = 1;
		// for the initial node
		setInitialNodePathInfo(lattice);


		// go through all the nodes, make all transitions
		//NodeIndex n;

		mainLoop: for (int n = 1 ; n <= finalPosition; n++) 
		{

			Node  node = sortedNodes.get(n);

			//System.err.println(" ======= At n= " + n + ":  " + node);

			String word = node.word;
			NodePathInfo info = nodeinfo[n];
			double oldlmscore = info.m_OldLMScore; // this does not work the way it should. We do not have a node old LM score
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
					Probabilities probs = new Probabilities();

					getTransitionProbability(node, word, oldlmscore, uselm, a, path, probs);

					if ((	probs.prob + probBwd) >= threshold) 
					{
						LatticeDecodePath  newpath = new LatticeDecodePath(node, path, probs);
						
						if (++numPaths > maxPaths) break mainLoop;

						if (nbest != 0) 
							newpath.addLink(path, probs.prob - path.m_Prob); // beware: implement this later if we are interested in nbest anyway

						shiftContext(word, nolmword, path, newpath);  

						LatticeDecodePath p;
						
						if ((p = info.m_PHash.get(newpath)) != null) 
						{
							p.merge(newpath, nbest);
						} else 
						{
							info.m_PHash.put(newpath, newpath);
						}
					} 
				}
			}

			int num = info.m_PHash.size();     
			info.m_PList = new LatticeDecodePath [ num ];

			int I = 0;
			for (LatticeDecodePath ldp: info.m_PHash.keySet())
			{
				// System.err.println("Path at  n= " + n + " with context: " + ldp.m_Context);
				info.m_PList[I++] = ldp;
				if (nbest != 0 && getMaxFanIn() != 0)
				{
					ldp.truncLinks(getMaxFanIn());
				}
			}
			info.m_PHash.clear();
			info.m_NumPaths = num;
		}

		//FINAL (mainLoop broken)

		NodePathInfo  pfinal = nodeinfo[finalPosition];

		if (pfinal.m_NumPaths == 0) 
		{
			System.err.println("No paths found");
			return null;
		}

		Arrays.sort(pfinal.m_PList, new LatticeDecodePath.PathComparator());  
		return nodeinfo;
	}

	protected double determineThresholdForBeamSearch(List<Node> sortedNodes,
			int finalPosition, NodePathInfo[] nodeinfo)
	{
		double thresh;
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
				double prob2 = a.acoustic + prob + unigramScore;
				NodePathInfo fromInfo = nodeinfo[a.source.topSortOrder];
				if (prob2 > fromInfo.m_ProbBwd)
				{
					fromInfo.m_ProbBwd = prob2;
				}
			}
		}
		thresh = nodeinfo[0].m_ProbBwd - beamWidth;
		return thresh;
	}

	protected void setInitialNodePathInfo(Lattice lattice)
	{
		LatticeDecodePath  path0 = new LatticeDecodePath(); // this is dummy initial one
		path0.node = lattice.getStartNode();
		if (contextLen > 1) 
		{
			path0.m_Context.add(path0.node.word);
		}

		for (int i = 1; i < contextLen - 1; i++)  
			path0.m_Context.add(Vocab_None);

		path0.m_Context.add(Vocab_None); 

		nodeinfo[0].m_PList = new LatticeDecodePath  [1];
		nodeinfo[0].m_PList[0] = path0;
		nodeinfo[0].m_NumPaths = 1;
	}

	/**
	 * Determine prob and gprob from previous path, language model score and current transition.
	 * I do not understand the old LM score stuff. If we do it this way, one could remove the old weight before processing.
	 * Besides, I am now adding it instead of subtracting.... which means we interpolate old and new rather than substracting  ....
	 * SRI appears 
	 * @param node
	 * @param word
	 * @param oldlmscore
	 * @param uselm
	 * @param a
	 * @param path
	 * @param probs
	 */
	protected void getTransitionProbability(Node node, String word,
			double oldlmscore, boolean uselm, Arc a, LatticeDecodePath path,
			Probabilities probs)
	{
		
		double oldArcLMScore = a.language * oldLmScale;
		// System.err.println("OLD LM: "  + a.language + " * " + oldLmScale + "= " + oldArcLMScore);
		// SRI: path.m_Prob + a.weight - oldArcLMScore [ of zoiets]
		// which means that we would add AND subtract the old lm score..... (?)
		
		boolean emulateSRI = true;
		if (!emulateSRI)
		probs.prob = path.m_Prob + a.acoustic + oldArcLMScore; // oldlmscore not used yet...
		else
			probs.prob = path.m_Prob + a.weight - oldArcLMScore ;
			
		probs. gprob = LogP_One;

		if (uselm) 
		{
			double lmscore = LogP_One;
			probs.gprob = wordProb(lm, word, path.m_Context); // call the LM
			if (probs.gprob < logP_floor) 	
				probs.gprob = logP_floor;
			lmscore = 	
					probs.gprob * getLmscale();
			probs.prob  += lmscore; 
		} else // use a.language here?
		{
			probs.gprob = a.language;

			if (!Double.isNaN(node.nodeLanguageScore)) // is never the case, remove....
			{
				System.err.println("Node lm score found, exiting ");
				System.exit(1);
				probs.gprob = node.nodeLanguageScore ;
			}
		}
	}

	/*
	 * Todo check boundary conditions (no context)
	 */
	private void shiftContext(String word, boolean nolmword,
			LatticeDecodePath path, LatticeDecodePath newpath)
	{
		//if (true) return;  // TODO fix this
		if (nolmword) // copy from path if word is skipped by LM
		{
			newpath.m_Context.clear();
			newpath.m_Context.addAll(path.m_Context);
		} else 
		{
			newpath.m_Context.clear();
		
			for (int j=1; j < contextLen; j++)
				newpath.m_Context.add(path.m_Context.get(j));
			newpath.m_Context.add(word);
			//newpath.m_Context.set(contextLen - 1, Vocab_None);
		}
		//System.err.println("Shifted context with word " + word + " from " + path.m_Context + " to " + newpath.m_Context);
	}

	private double wordProb(NgramLanguageModel<String> lm, String word,
			List<String> m_Context)
	{
		// TODO Auto-generated method stub
		m_Context.add(word);
		
		double d =   lm.getLogProb(m_Context);
		//System.err.println("Lm prob for "  +m_Context + " = " + d );
		m_Context.remove(word);
		return d;
	}

	boolean ignoreWord(String word)
	{
		return this.getIgnoreWords().contains(word);
	}

	public Set<String> getIgnoreWords()
	{
		return ignoreWords;
	}

	void setIgnoreWords(Set<String> ignoreWords)
	{
		this.ignoreWords = ignoreWords;
	}

	
	public void decodeLatticeFile(String fileName)
	{
		Lattice l = StandardLatticeFile.readLatticeFromFile(fileName);
		this.resetTransient();
		
		long s = System.currentTimeMillis();
		
		List<String> decoded = this.decode(l);
		String sentence = StringUtils.join(decoded, " ");
		System.out.println("<" + fileName +  "> " + sentence);
		long e =  System.currentTimeMillis();
		System.err.printf("decode time in milliseconds for %s: " + (e-s)  + "\n", fileName);
	}
	
	public static void decodeLatticeFile(String fileName, NgramLanguageModel<String> lm, VariantLexicon v)
	{
		//Lattice l = StandardLatticeFile.readLatticeFromFile(fileName);		
		LatticeDecoder d = new LatticeDecoder();
		d.setVariantLexicon(v);
		
		d.setLanguageModel(lm);
		d.decodeLatticeFile(fileName);
	}
	
	public static void decodeFilesInFolder(String dirname, NgramLanguageModel<String> lm, VariantLexicon v)
	{
		LatticeDecoder decoder = new LatticeDecoder();
		
		decoder.setVariantLexicon(v);
		decoder.setLanguageModel(lm);
		File d = new File(dirname);
		
		FilenameFilter fi = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".lattice");
			}
		};
		
		if (d.isDirectory())
		{
			File[] entries = d.listFiles(fi);
			Arrays.sort(entries);
			long s = System.currentTimeMillis();
			for (File f: entries)
			{
				try
				{
					String p = f.getCanonicalPath();
					decoder.decodeLatticeFile(p);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			long e =  System.currentTimeMillis();
			double average = (e-s) / (double) entries.length;
			System.err.printf("%d entries\n", entries.length);
			System.err.println("Total decode time in milliseconds: " + (e-s)  + "  average " + average);
		}
	}
	

	public double getLmscale()
	{
		return lmscale;
	}

	public void setLmscale(double lmscale)
	{
		this.lmscale = lmscale;
	}

	public  VariantLexicon getVariantLexicon()
	{
		return variantLexicon;
	}

	public void setVariantLexicon(VariantLexicon variantLexicon)
	{
		this.variantLexicon = variantLexicon;
	}
	

	int getMaxFanIn()
	{
		return maxFanIn;
	}

	void setMaxFanIn(int maxFanIn)
	{
		this.maxFanIn = maxFanIn;
	}
	
	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;
		
		String languageModel =  "data/trigramModel.lm.bin";
		//languageModel = null;
		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
		     lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}
		VariantLexicon v = null;
		if (args.length > 1)
		{
			v = new VariantLexicon();
			v.loadFromFile(args[1]);
		}
		if (args.length == 0)
			decodeLatticeFile("resources/exampleData/115_070_002_02_18.lattice", lm, v);
		else
			decodeFilesInFolder(args[0],lm, v);
	}
}
