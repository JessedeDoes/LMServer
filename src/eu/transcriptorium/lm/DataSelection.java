package eu.transcriptorium.lm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.io.LmReaders;

/**
 * Implements moore et. al.  method for data selection: select all sentences <i>S</i> from the larger corpus such that 
 * <tt>
 * (cross entropy of <i>S</i> wrt domain model) - (cross entropy of <i>S</i> wrt general model) larger than a certain threshold
 * </tt>
 * <p>
 * Since we do not know a decent threshold,  just output all sentences, sorted with the most in-domain-like first.
 * <p>
 * One wonders: why is the selection performed on isolated sentences? Paragraphs or sections or whole texts might be an alternative
 * @author does
 */
public class DataSelection 
{
	ArrayEncodedProbBackoffLm<String> inDomainModel;
	ArrayEncodedProbBackoffLm<String> generalModel;

	List<ScoredSentence> scoredSentences = new ArrayList<ScoredSentence>();

	public DataSelection(String idm, String gm)
	{
		generalModel = LmReaders.readArrayEncodedLmFromArpa(gm,false);
		inDomainModel = LmReaders.readArrayEncodedLmFromArpa(idm,false);
		System.err.println("Models loaded\n");
	}

	public static void test2(String[] args)
	{
		DataSelection ds = new DataSelection(args[0],args[1]);
		ds.testMooreMethod(args[2]);
	}

	public void testMooreMethod(String fileName)
	{
		try
		{
			int k=0;
			BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String s;
			while ((s = b.readLine()) != null)
			{
				String[] words = s.split("\\s+");
				List<String> wordList = Arrays.asList(words);
				ScoredSentence ss = new ScoredSentence();
				ss.sentence = s;
				ss.pInDomain = inDomainModel.scoreSentence(wordList) / (float) words.length ;
				ss.pGeneral = generalModel.scoreSentence(wordList) / (float) words.length;
				ss.difference = ss.pInDomain - ss.pGeneral;
				if (!(Float.isNaN(ss.difference) || Float.isInfinite(ss.difference)))
					scoredSentences.add(ss);
				k++;
				if (k % 10000 == 0)
				{
					System.err.println(k + ": " + ss.difference + " " + s);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		Collections.sort(scoredSentences, new ScoredSentenceComparator());
		for (int i=scoredSentences.size()-1; i > 0; i--)
		{
			ScoredSentence ss = scoredSentences.get(i);
			System.out.println(ss.difference + "\t" + ss.sentence);
		}
	}

	public static void main(String[] args)
	{
		test2(args);
	}
}
