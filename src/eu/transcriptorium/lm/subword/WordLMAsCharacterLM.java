package eu.transcriptorium.lm.subword;

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

public class WordLMAsCharacterLM
{
	Map<List<String>, Trie> successorTrieMap = new HashMap<List<String>, Trie>();
	
	static class P
	{
		double wordProb=0;
		double nodeProb=0;
		
		public P(double n)
		{
			this.wordProb= n;
		}
		
		public String toString()
		{
			return "w=" + wordProb + ",n="  + nodeProb;
		}
	};
	
	
	public void setNodeProb(TrieNode n)
	{
		double p=0;
		for (int i=0; i < n.nofTransitions(); i++)
		{
			TrieNode next = n.transition(i).node;
			setNodeProb(next);
			p += ((P) next.data).nodeProb;
		}
		if (n.data == null)
			n.data = new P(0);
		((P) n.data).nodeProb = p +	((P) n.data).wordProb ;
	}
	
	public void printNodeProb(TrieNode n, String prefix)
	{
		double p=0;
		System.err.println(prefix + ":" + n.data);
		for (int i=0; i < n.nofTransitions(); i++)
		{
			TrieNode next = n.transition(i).node;
			char c = (char) n.transition(i).character;
			printNodeProb(next, prefix + c);
		}
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

	private NgramLanguageModel<String> wordLM = null;

	void collectNextWords()
	{
		NgramMap<ProbBackoffPair> map = null;
		//NgramMap<Set<String>>  nextMap = new HashNgramMap<Set<String>> ();
		
		Map<List<String>, Set<String>> successorMap = new HashMap<List<String>, Set<String>>();
		
		if (wordLM instanceof ArrayEncodedProbBackoffLm)
		{
			map = ((ArrayEncodedProbBackoffLm) wordLM).getNgramMap();
		} else if (wordLM instanceof ContextEncodedProbBackoffLm)
		{
			map = ((ContextEncodedProbBackoffLm) wordLM).getNgramMap();
		}
		
		WordIndexer<String> wi = wordLM.getWordIndexer();
		

		if (map != null)
		{
			for (int n=0; n < wordLM.getLmOrder(); n++)
			{
				System.err.println("ORDER: " + n);
				for (NgramMap.Entry<ProbBackoffPair> e : map.getNgramsForOrder(n))
				{
					int[] inds = e.key;
					// int[] history = Arrays.copyOfRange(inds, 0, inds.length-1);
									
					// byte [] subArray = Arrays.copyOfRange(a, 4, 6);
					List<String> words = new ArrayList<String>();
					for (int i: inds)
					{
						words.add(wi.getWord(i));
					}
					List<String> hist = words.subList(0, words.size()-1);
					Set<String> x = successorMap.get(hist);
					if (x == null)
					{
						x = new HashSet<String>();
						successorMap.put(hist, x);
					}
					x.add(words.get(words.size()-1));
					//System.err.println(words + " " + e.value  + hist  + ": " + x.size()) ;
				}
			}
		}
		
		for (List<String> hist:  successorMap.keySet())
		{
			Set<String> nextWords = successorMap.get(hist);
			// System.err.println(hist + " -->" + nextWords );
			Trie trie = new Trie();
			for (String w: nextWords)
			{
				hist.add(w);
				P p = new P(Math.pow(10,wordLM.getLogProb(hist)));
				//System.err.println(w + ":" + p + " // " +  hist);
				trie.insertWord(w, p);
				hist.remove(hist.size()-1);
			}
			setNodeProb(trie.root);
			
			System.err.println(hist + " -->" + nextWords);
			
			// trie.forAllNodesPreOrder(na);
			//
			
			printNodeProb(trie.root,"");
			successorTrieMap.put(hist, trie);
		}
	}
	
	public void testModel(String[] characters)
	{
		
	}
	
	public static void main(String[] args)
	{
		WordLMAsCharacterLM x = new WordLMAsCharacterLM();
		String arg0;
		if (args.length == 0)
			arg0 ="BenthamNewTokenization/Bentham_train_bigram/languageModel.lm";
		else
			arg0 = args[0];
		x.wordLM = x.readLM(arg0);
		x.collectNextWords();
	}
} 