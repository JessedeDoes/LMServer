package eu.transcriptorium.lm;

import edu.berkeley.nlp.lm.io.*;
import edu.berkeley.nlp.lm.*;
import java.io.*;
import java.util.*;


/**
 Reads an arpa LM, a sentence-per-line corpus and scores each sentence
 */

public class TestArpaLM
{
	public static void test1(String[] args)
	{
		String modelName = args[0];
		ArrayEncodedProbBackoffLm<String> model = LmReaders.readArrayEncodedLmFromArpa(modelName,false);
		System.err.println("model loaded ... ");
		String s;
		int rejects=0;
		try
		{
			BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
			float total=0;
			int k=0;
			while ((s = b.readLine()) != null)
			{
				String[] words = s.split("\\s+");
				List<String> wordList = Arrays.asList(words);
				// there is no need to add sentence bounds, the scoreSentence method does that
				// (but not during training...) (?) it appears that it does, so why the earlier trouble??
				float p = model.scoreSentence(wordList) / (float) words.length;
				if (!Float.isNaN(p))
				{
					total += p;
					k++;
					System.out.println(p + "\t" + s);
				} else
				{
					rejects++;
				}
			}
			System.err.println("Average sentence cross entropy: " + total / (double) k + " rejects: "  + rejects);
		} catch (Exception e)
		{
		}
	}


	public static void main(String[] args)
	{
		test1(args);
	}
}
