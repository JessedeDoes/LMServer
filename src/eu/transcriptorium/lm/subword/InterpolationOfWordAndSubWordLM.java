package eu.transcriptorium.lm.subword;
import java.util.List;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;
import eu.transcriptorium.lm.subword.MultiLevelText.Node;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;

public class InterpolationOfWordAndSubWordLM
{
	private NgramLanguageModel<String> wordLM = null;
	private NgramLanguageModel<String> subWordLM = null;
	float lambda = (float) 0.3;
	CharacterSet characterSet;
	
	public InterpolationOfWordAndSubWordLM(NgramLanguageModel<String> wordLM, 
			NgramLanguageModel<String> subWordLM)
	{
		this.wordLM  = wordLM;
		this.subWordLM = subWordLM;
		characterSet = new AlejandrosNewBenthamTokenization();
		characterSet.setAcceptAll();
	}
	
	void evaluate(String sentence)
	{
		
	}
	
	void evaluate(MultiLevelText txt)
	{
		final List<String> wordList =  boundSentence(txt.levels.get(0), wordLM);
		final List<String>  subWordList =  boundSentence(txt.levels.get(1), subWordLM);
	    float[] subwordScores = scoreSentence(subWordList, subWordLM);
	    float[] wordScores = scoreSentence(wordList, wordLM);
	    
		for (Node n: txt.base)
		{
			float x = 0;
			for (int i: n.children)
				x += subwordScores[i];
			n.oovProb = x;
		}
		for (int i=0; i < txt.base.size(); i++)
		{
			Node n = txt.base.get(i);
			n.logProb = wordScores[i];
			
			if (isOOV(wordLM,n.word))
				n.combiProb = n.oovProb;
			else
				n.combiProb = lambda * n.logProb + (1-lambda) * n.oovProb;
		}
	}

	public BoundedList<String> boundSentence(List<String> txt, NgramLanguageModel<String> lm)
	{
		return new BoundedList<String>(txt, 
				lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
	}
	
	public boolean isOOV( NgramLanguageModel<String> lm, String w)
	{
		WordIndexer<String> wi = lm.getWordIndexer();
		int ui = wi.getIndexPossiblyUnk(wi.getUnkSymbol());
		return (wi.getIndexPossiblyUnk(w) == ui);
	}
	
	public static float[]  scoreSentence(final List<String> sentence, final NgramLanguageModel<String> lm) 
	{
		final List<String> sentenceWithBounds =
		 new BoundedList<String>(sentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());

		final int lmOrder = lm.getLmOrder();
		float sentenceScore = 0.0f;
		float[] scores = new float[sentenceWithBounds.size()+1];
		for (int i=1;  i <=  sentenceWithBounds.size() + 1; i++)
		{
			final List<String>  ngram = (i < lmOrder-1)? 
					sentenceWithBounds.subList(-1, i): sentenceWithBounds.subList(i - lmOrder, i);
			final float scoreNgram = lm.getLogProb(ngram);
			sentenceScore += scoreNgram;
			scores[i-1] = scoreNgram;
		}
		return scores;
	}
}

/**
 * 
 * public static <T> float scoreSentence(final List<T> sentence, final ArrayEncodedNgramLanguageModel<T> lm) {
			final List<T> sentenceWithBounds =
			 new BoundedList<T>(sentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());

			final int lmOrder = lm.getLmOrder();
			float sentenceScore = 0.0f;
			for (int i = 1; i < lmOrder - 1 && i <= sentenceWithBounds.size() + 1; ++i) {
				final List<T> ngram = sentenceWithBounds.subList(-1, i);
				final float scoreNgram = lm.getLogProb(ngram);
				sentenceScore += scoreNgram;
			}
			for (int i = lmOrder - 1; i < sentenceWithBounds.size() + 2; ++i) {
				final List<T> ngram = sentenceWithBounds.subList(i - lmOrder, i);
				final float scoreNgram = lm.getLogProb(ngram);
				sentenceScore += scoreNgram;
			}
			return sentenceScore;
		}
 * 
 */




