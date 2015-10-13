package eu.transcriptorium.lm.subword;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ContextEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.map.HashNgramMap;
import edu.berkeley.nlp.lm.map.NgramMap;
import edu.berkeley.nlp.lm.values.ProbBackoffPair;

import java.util.*;

public class WordLMAsCharacterLM
{

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
		
		if (wordLM instanceof ArrayEncodedProbBackoffLm )
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
			System.err.println(hist + " -->" + successorMap.get(hist));
		}
	}
	
	public static void main(String[] args)
	{
		WordLMAsCharacterLM x = new WordLMAsCharacterLM();
		x.wordLM = x.readLM(args[0]);
		x.collectNextWords();
	}
} 