package eu.transcriptorium.hyphen;
import edu.berkeley.nlp.lm.*;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lattice.ParagraphDecoder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

// dit is toch weer niet de manier.... ... beter in shiftContext functie ellende stoppen

public class HyphenationAwareLM 
{
	NgramLanguageModel<String> baseModel;
	HyphenationDictionary hyphenationDictionary;

	/*
	 * Iterate over all ngrams to add hyphenated probabilities
	 */

	public HyphenationAwareLM(NgramLanguageModel<String> baseLM, HyphenationDictionary hDict)
	{
		this.baseModel= baseLM;
		this.hyphenationDictionary = hDict;
	}

	public double scoreSentence(List<String> sentence)
	{
		double p = 0;
		int M = this.baseModel.getLmOrder();
		List<String> normalizedSentence = new ArrayList<String>();
		
		final List<String> sentenceWithBounds = 
				new BoundedList<String>(sentence, baseModel.getWordIndexer().getStartSymbol(), baseModel.getWordIndexer().getEndSymbol());
		final int lmOrder = baseModel.getLmOrder()+1;
		float sentenceScore = 0.0f;
		float previousScore = 0.0f;
		
		// beginnetje
		
		for (int i = 1; i < lmOrder - 1 && i <= sentenceWithBounds.size() + 1; ++i) 
		{
			final List<String> ngram = sentenceWithBounds.subList(-1, i);
			final float scoreNgram = (float) getLogProb(ngram, previousScore); 
			sentenceScore += scoreNgram;
			previousScore = scoreNgram;
		}

		// rest
		
		for (int i = lmOrder - 1; i < sentenceWithBounds.size() + 2; ++i) 
		{
			final List<String> ngram = sentenceWithBounds.subList(i - lmOrder, i);
			final float scoreNgram = (float) getLogProb(ngram,  previousScore);
			sentenceScore += scoreNgram;
			previousScore = scoreNgram;
		}

		return sentenceScore;
	}
	
	public int findBoundary(List<String> ngram)
	{
		int b = -1;
		for (int i=0; i < ngram.size(); i++)
		{
			if (ngram.get(i).endsWith("-"))
			{
				return i;
			}
		}
		return b;
	}
	
	public double getLogProb(List<String> ngram, double previousScore)
	{
		
		return getLogProb(ngram, findBoundary(ngram), previousScore);
	}
	
	// this does NOT work correctly... (stop this line)
	// 
	public float getLogProb(List<String> ngram, int boundaryPosition, double previousScore) // bp zit op index waarna splitsing
	{
		float p=0;
		float basep = baseModel.getLogProb(ngram);
		float baseP = (float) Math.exp(basep);
		
		if (boundaryPosition == ngram.size()-1) // logP=0 to defer LM scoring until the next token (dit kan dus niet meneer)
		{
			System.err.println("Hyphen last in: " + ngram);
			return basep;
		}
		
		if (boundaryPosition < 0) // no line boundary: refer to base model
		{
			System.err.println("Nothing special: " + ngram);
			return basep;
		}
		
		//  we need to join parts

		String p1 = ngram.get(boundaryPosition);
		String p2 = ngram.get(boundaryPosition+1);
		
		String P1 = p1.replaceAll("-$", "");
		String w = P1+p2;

		double pHyph = hyphenationDictionary.getHyphenationProbability(w, p1, p2);

		List<String> joined = new ArrayList<String>();
		
		for (int i=0; i < boundaryPosition; i++)
			joined.add(ngram.get(i));
		joined.add(w);
		
		for (int i=boundaryPosition+2; i < ngram.size(); i++)
			joined.add(ngram.get(i));
		
		System.err.println("Joined ngram: "  + joined);
		
		double joinedP = Math.exp(baseModel.getLogProb(joined));
		
		if (boundaryPosition == ngram.size()-2)
		{
			System.err.println("Penultimate, dividing " + joinedP + " by " + Math.exp(previousScore));
			joinedP /= Math.exp(previousScore);
		}
		
		System.err.println(ngram +  " joined: (" + boundaryPosition + ") " + joinedP + " apart: " + baseP + " pHyph " + pHyph);
		
		return (float) Math.log((1-pHyph) * baseP + pHyph* joinedP);
	}

	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;
		String languageModel =  args[0];


		ParagraphDecoder p = new ParagraphDecoder();
		// languageModel = null;
		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}

		HyphenationAwareLM  hlm = new HyphenationAwareLM(lm, new SimpleHyphenationDictionary());
		String line;
		try
		{
			BufferedReader b = new BufferedReader(new  InputStreamReader(System.in));
			while ((line = b.readLine()) != null)
			{
				String[] tokens = line.split("\\s+");
				List<String> x = new ArrayList<String>();
			
				for (String s: tokens)
				{
					//if (s.endWith)
					 x.add(s);
				}
				System.out.println(line +  " " + hlm.scoreSentence(x));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
