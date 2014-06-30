package eu.transcriptorium.lm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.io.LmReaders;

public class ScoreWordSubstitutions
{
	ArrayEncodedProbBackoffLm<String> model;
	
	public  ScoreWordSubstitutions(String modelName)
	{
		this.model = LmReaders.readArrayEncodedLmFromArpa(modelName,false);
	}
	
	public void test()
	{
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
				String[] parts = s.split("\t");
				String sentence = parts[0];
				String candidatez = parts[1];
				String[] candidates = candidatez.split("\\s+");

				List<Double>  scores = scoreCandidates(sentence, candidates) ;

				for (int i=0; i < candidates.length; i++)
				{
					System.out.print(candidates[i] + ":"  + scores.get(i)  + " ");
				}
				System.out.println();
			}
			System.err.println("Average sentence cross entropy: " + total / (double) k + " rejects: "  + rejects);
		} catch (Exception e)
		{
		}
	}

	public List<Double>  scoreCandidates(String sentence, String[] candidates)
	{
		List<Double> scores = new ArrayList<Double>();
		double sumOfProbs = 0;
		
		for (String candidate: candidates)
		{
			String sentenceX = sentence.replaceAll("<>", candidate);
			String[] words = sentenceX.split("\\s+");
			
			List<String> wordList = Arrays.asList(words);
		
			double p = Math.exp(model.scoreSentence(wordList)); 
			System.err.println(sentenceX + " " + p);

			scores.add(p);
			sumOfProbs += p;
		}
		
		for (int i=0; i < scores.size(); i++)
			scores.set(i,  scores.get(i) / sumOfProbs);
		
		return scores;
	}

	public static void main(String[] args)
	{
		ScoreWordSubstitutions s = new ScoreWordSubstitutions(args[0]);
		s.test();
	}
}
