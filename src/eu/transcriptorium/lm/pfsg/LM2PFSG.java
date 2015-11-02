package eu.transcriptorium.lm.pfsg;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ContextEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.map.HashNgramMap;
import edu.berkeley.nlp.lm.map.NgramMap;
import edu.berkeley.nlp.lm.values.ProbBackoffPair;
import eu.transcriptorium.util.trie.Trie;
import eu.transcriptorium.util.trie.Trie.NodeAction;
import eu.transcriptorium.util.trie.Trie.TrieNode;

import java.util.*;

public class LM2PFSG 
{
	NgramLanguageModel lm;

	float logScale = (float) (2.30258509299404568402 * 10000.5);
	float half = 0.5;
	static String start_tag = "<s>";
	static String end_tag  = "</s>";
	static String nullWord = "NULL";
	int numNodes = 0;
	int numTrans=0;
	
	Map <String,Integer> nodeNum = new HashMap<String,Integer> ();
	Map <Integer,String> nodeString = new HashMap<Integer,String> ();
	
	static class Transition
	{
		int from;
		int to;
		float p;
		
		public Transition(int from, int to, float p)
		{
			this.from = from;
			this.to = to;
			this.p=p;
		}
	}
	
	List<Transition> transitions = new ArrayList<Transition>();
	
	
	float rint(float x)
	{
		if (x < 0)
			return (float) Math.floor(x-half);
		else
			return (float) Math.floor(x+half);
	}
	
	float scaleLog(float x)
	{
		return rint(x * logScale);
	}
	
	static String outputForNode(String name)
	{
		String[] tokens = name.split("\\s+");
		if (tokens.length == 0)
			return null;
		else 
		{
			String l = tokens[tokens.length-1];
			if (l.equals(start_tag) || l.equals(end_tag))
				return nullWord;
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
		}
		return i;
	}
	
	void addTrans(String from, String to, float prob)
	{
		numTrans++;
		transitions.add(new Transition(nodeIndex(from), nodeIndex(to), scaleLog(prob)));
	}
	
	private NgramMap<ProbBackoffPair> getBackoffMap(NgramLanguageModel lm) 
	{
		NgramMap<ProbBackoffPair> map = null;
		if (lm instanceof ArrayEncodedProbBackoffLm)
		{
			map = ((ArrayEncodedProbBackoffLm) wordLM).getNgramMap();
		} else if (lm instanceof ContextEncodedProbBackoffLm)
		{
			map = ((ContextEncodedProbBackoffLm) wordLM).getNgramMap();
		}
		return map;
	}
}
