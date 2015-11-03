package eu.transcriptorium.lm.pfsg;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ContextEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.map.HashNgramMap;
import edu.berkeley.nlp.lm.map.NgramMap;
import edu.berkeley.nlp.lm.values.ProbBackoffPair;
import eu.transcriptorium.util.StringUtils;
import eu.transcriptorium.util.trie.Trie;
import eu.transcriptorium.util.trie.Trie.NodeAction;
import eu.transcriptorium.util.trie.Trie.TrieNode;

import java.util.*;

/**
 * Check: the should be an OOV symbol in the LM?
 * Or else there is no way to backoff?
 */

public class LM2PFSG 
{
	NgramLanguageModel lm;

	float logScale = (float) 1; // (2.30258509299404568402 * 10000.5);
	float half = (float) 0.5;
	boolean check_bows = false;
	boolean no_empty_bo = false;
	float epsilon = (float) 1e-5;         // # tolerance for lowprob detection
	boolean debug= false;
	
	int numNodes = 0;
	int numTrans = 0;

	Map <String,Integer> nodeNum = new HashMap<String,Integer> ();
	Map <Integer,String> nodeString = new HashMap<Integer,String> ();
	Map<String, Float> bows = new HashMap<String, Float>();

	List<PFSG.Transition> transitions = new ArrayList<PFSG.Transition>();

	PFSG pfsg = new PFSG();

	float rint(float x)
	{
		if (x < 0)
			return (float) Math.floor(x-half);
		else
			return (float) Math.floor(x+half);
	}

	float scaleLog(float x)
	{
		return x; // Math.round(x * logScale);
	}

	static String outputForNode(String name)
	{
		String[] tokens = name.split("\\s+");
		if (tokens.length == 0)
			return null;
		else 
		{
			String l = tokens[tokens.length-1];
			if (l.equals(PFSG.start_tag) || l.equals(PFSG.end_tag))
				return PFSG.nullWord;
			else
				return l;
		}
	}

	int nodeIndex(String name)
	{
		Integer i = nodeNum.get(name);
		if (i == null)
		{
			i = numNodes++;
			nodeNum.put(name, i);
			nodeString.put(i, outputForNode(name));
			pfsg.addNode(i, nodeString.get(i), name);
			if (debug)
				System.err.println("node " + i + " = " + name + ", output=" + nodeString.get(i));
		}
		return i;
	}

	boolean node_exists(String name)
	{
		return nodeNum.get(name) != null;
	}

	void addTrans(String from, String to, float prob)
	{
		numTrans++;
		if (debug)
			System.err.println("add_trans " + from + " -> " + to +  " " + prob);
		PFSG.Transition t = new PFSG.Transition(nodeIndex(from), nodeIndex(to), scaleLog(prob));
		pfsg.addTransition(t);
		transitions.add(t);
	}

	// ToDo bow=0 cases...
	void visitNgrams()
	{
		NgramMap<ProbBackoffPair> map = getBackoffMap(lm);
		WordIndexer<String> wi = lm.getWordIndexer();

		Map<List<String>, Set<String>> successorMap = new HashMap<List<String>, Set<String>>();

		if (map != null)
		{
			for (int currorder=0; currorder < lm.getLmOrder(); currorder++)
			{
				System.err.println("ORDER: " + currorder);
				for (NgramMap.Entry<ProbBackoffPair> e : map.getNgramsForOrder(currorder))
				{
					int[] inds = e.key;
					ProbBackoffPair pbp = e.value;

					List<String> words = new ArrayList<String>();

					for (int i: inds)
					{
						words.add(wi.getWord(i));
					}

					String  ngram = StringUtils.join(words, " ");

					float prob = pbp.prob;
					float bow = pbp.backoff;

					String first_word = words.get(0), 
							last_word = words.get(words.size()-1), 
							ngram_prefix = StringUtils.join(words.subList(0, words.size()-1), " "), 
							ngram_suffix = StringUtils.join(words.subList(1, words.size()), " "),
							target;
					//if (currorder > 0)
					//System.err.println(ngram + " PRE: " + ngram_prefix + " SUF " + ngram_suffix);
					if (currorder == 0)
					{
						// todo onbegrijpelijke code met NF
						// zet bow op 0 bij unigram waar hij niet bij staat (hoeft hier niet, is al geregeld?)
					} else if (currorder == 1)
					{

					} else if (currorder == 2)
					{

					}

					// bow conditie mag weg?

					if (bow != 0 && (currorder == 0 || currorder < lm.getLmOrder()-1))
					{
						bows.put(ngram, bow);
						String this_bo_name;
						if (no_empty_bo && ngram.equals(PFSG.start_tag))
							this_bo_name = PFSG.start_bo_name;
						else
							this_bo_name = PFSG.bo_name;
						// insert backoff transitions....
						if (false && currorder < lm.getLmOrder()-1) // TODO onduidelijk gedoe met read_contexts -- snap ik niet zo
						{
							addTrans(this_bo_name +  " " + ngram, this_bo_name + " " + ngram_suffix, bow);
							addTrans(ngram, this_bo_name + " " + ngram, 0);
						} else
						{
							addTrans(ngram, this_bo_name + " " + ngram_suffix, bow);
						}
					}

					if (last_word.equals(PFSG.start_tag))
					{
						if (currorder > 0)
							System.err.println("ignore ngram into start tag " + ngram);
					} else // insert N-gram transition to maximal suffix of target context
					{
						if (last_word.equals(PFSG.end_tag))
							target = PFSG.end_tag;
						else if (currorder == 0 || bows.get(ngram) != null)
							target = ngram;
						else if (bows.get(ngram_suffix) != null)
							target = ngram_suffix;
						else 
						{
							target = ngram_suffix;
							for (int i=2; i <= currorder; i++)
							{
								target = StringUtils.join(words.subList(i, words.size()), " ");
								if (bows.get(target) != null)
									break;
							}
						}
						if (currorder == 0 || currorder < lm.getLmOrder()-1)
						{
							addTrans(PFSG.bo_name + " " + ngram_prefix, target, prob);
							if (no_empty_bo && node_exists(PFSG.start_bo_name + " "  + ngram_prefix) && (!target.equals(PFSG.end_tag)))
								addTrans(PFSG.start_bo_name + " " + ngram_prefix, target, prob);
						} else
							addTrans(ngram_prefix, target, prob);

						if (check_bows)
						{

						}
					}


					if (false)
					{
						List<String> hist = words.subList(0, words.size()-1);
						Set<String> x = successorMap.get(hist);
						if (x == null)
						{
							x = new HashSet<String>();
							successorMap.put(hist, x);
						}
						x.add(words.get(words.size()-1));
					}
					//System.err.println(words + " " + e.value  + hist  + ": " + x.size()) ;
				}
			}
		}	
		System.err.println("Nodes: "  + nodeNum.keySet().size());
		System.err.println("Transitions: " + transitions.size());
	}

	private NgramMap<ProbBackoffPair> getBackoffMap(NgramLanguageModel lm) 
	{
		NgramMap<ProbBackoffPair> map = null;
		if (lm instanceof ArrayEncodedProbBackoffLm)
		{
			map = ((ArrayEncodedProbBackoffLm) lm).getNgramMap();
		} else if (lm instanceof ContextEncodedProbBackoffLm)
		{
			map = ((ContextEncodedProbBackoffLm) lm).getNgramMap();
		}
		return map;
	}

	static NgramLanguageModel readLM(String fileName)
	{
		// languageModel = null;
		if (fileName != null)
		{
			if (!fileName.endsWith(".bin"))
				return  LmReaders.readArrayEncodedLmFromArpa(fileName,false);
			else
				return LmReaders.readLmBinary(fileName);

		} else
			return null;
	}

	void print()
	{
		List<String> l = new ArrayList<String>();
		for (String x: nodeNum.keySet())
		{
			l.add(x);
		}
		Collections.sort(l);
		for (String x: l)
		{
			System.out.println(nodeNum.get(x) + ":  " +  x  +  " nodeName: " + nodeString.get(nodeNum.get(x)));
		}
		for (PFSG.Transition t: transitions)
		{
			System.out.println(t);
		}
	}
	
	public PFSG build(NgramLanguageModel lm)
	{
		this.lm = lm;
		numNodes = 0;
		numTrans=0;

		nodeNum = new HashMap<String,Integer> ();
		nodeString = new HashMap<Integer,String> ();
		bows = new HashMap<String, Float>();
		transitions = new ArrayList<PFSG.Transition>();

		this.pfsg = new PFSG();
		visitNgrams();
		pfsg.lm = lm;
		return pfsg;
	}
	
	public PFSG build(String lmFileName)
	{
		NgramLanguageModel lm = readLM(lmFileName);
		return build(lm);
	}

	public static void main(String[] args)
	{
		LM2PFSG x = new LM2PFSG();
		String arg0;
		if (args.length == 0)
			arg0 ="./Test/languageModel.lm";
		else
			arg0 = args[0];
		x.lm = readLM(arg0);
		x.visitNgrams();
		x.print();
		//System.out.println(x.nodeNum);
		//System.out.println(x.transitions);
	}
}
