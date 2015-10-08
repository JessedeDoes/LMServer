package eu.transcriptorium.lm.subword;
import java.util.List;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;
import eu.transcriptorium.lm.charsets.ProcessingForCharacterLM;
import eu.transcriptorium.lm.subword.MultiLevelText.Node;
import eu.transcriptorium.util.Counter;
import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.LmReaders;

import java.io.*;

public class InterpolationOfWordAndSubWordLM
{
	private NgramLanguageModel<String> wordLM = null;
	private NgramLanguageModel<String> subWordLM = null;
	private float lambda = (float) 0.3; // weight of the word level LM
	CharacterSet characterSet;
	
	public InterpolationOfWordAndSubWordLM(NgramLanguageModel<String> wordLM, 
			NgramLanguageModel<String> subWordLM)
	{
		this.wordLM  = wordLM;
		this.subWordLM = subWordLM;
		wordLM.setOovWordLogProb(Float.NEGATIVE_INFINITY);
		characterSet = new ProcessingForCharacterLM();
		characterSet.loadFromHMMList("resources/CharacterSets/AuxHMMsList");
		//characterSet.setAcceptAll();
	}
	
	double  evaluate(String sentence)
	{
		 String s = characterSet.normalize(characterSet.cleanLine(sentence));
		 MultiLevelText t = new MultiLevelText(2);
		  t.parseFromString(s);	
		  return evaluate(t);
	}
	
    void testFromFile(String fileName) throws IOException
	{
		BufferedReader b = new BufferedReader(new FileReader(fileName));
		
		String txt ="";
		String l;
		while ((l=b.readLine())!=null)
		{
			txt += " " + l;
		}
		testSomeLambdas(txt);
	}
	void  testSomeLambdas(String sentence)
	{
		 String s = characterSet.normalize(characterSet.cleanLine(sentence));
		 MultiLevelText t = new MultiLevelText(2);
		  t.parseFromString(s);	
		  testSomeLambdas(t);
	}
	
	void testSomeLambdas(MultiLevelText  txt)
	{
		for (lambda=(float) 0.001; lambda < 0.999; lambda+= 0.01)
		{
			double d = evaluate(txt);
			System.err.println(lambda + " "  + d);
		}
	}
	
	double evaluate(MultiLevelText txt)
	{
		final List<String> wordList =  boundSentence(txt.levels.get(0), wordLM);
		final List<String>  subWordList =  boundSentence(txt.levels.get(1), subWordLM);
	    float[] subwordScores = scoreSentence(subWordList, subWordLM);
	    float[] wordScores = scoreSentence(wordList, wordLM);
	    Counter<String> OOVCounter = new   Counter<String>();
	    int nOOV = 0;
	    double p=0;
	   // System.err.println(wordList);
		for (Node n: txt.base)
		{
			float x = 0;
			for (int i: n.children)
				x += subwordScores[i];
			n.oovProb = x;
			//System.err.println(n + " ::: "  + n.oovProb);
		}
		for (int i=0; i < txt.base.size(); i++)
		{
			Node n = txt.base.get(i);
			n.logProb = wordScores[i];
			boolean oov=isOOV(wordLM,n.word);
			if (oov) nOOV++;
			//System.err.println("at node "  + i);
			
		
			n.combiProb = Math.log(lambda * Math.exp(n.logProb) + (1-lambda) * Math.exp(n.oovProb));
			p += n.combiProb;
			
			if (oov)
			{
				//System.err.println("OOV:"  + n.word);
				OOVCounter.increment(n.word);
			    //System.err.println(n + " -> "  +String.format("p:%f, c:%f, w:%f (lambda=%f)", n.combiProb, n.oovProb, n.logProb, lambda));
			}
		}
		System.err.println("words: " + wordList.size() + " nOOV: " + nOOV);
		System.err.println("OOVs: " + OOVCounter.keyList());
		return p;
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

	public float getLambda()
	{
		return lambda;
	}

	public void setLambda(float lambda)
	{
		this.lambda = lambda;
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
	
	static String[] defaultArgs =  // supercalifragilisticexpialidocious
		{
		"BenthamNewTokenization/Interpolation/languageModel.lm", //  Bentham_train_bigram
		"BenthamNewTokenization/Bentham_train_bigram_char/languageModel.lm",
		"It is clear \"that\" the supercalifragilisticexpialidocious one will do this. Besides, do you like dogs?",
		"BenthamNewTokenization/BenthamValidation/bentham_validation.txt"
		};
	
	public static void main(String[] args) throws IOException
	{
		if (args.length < 3)
			args = defaultArgs;
		
		NgramLanguageModel wlm = readLM(args[0]);
		NgramLanguageModel clm = readLM(args[1]);
		String s = args[2];
		InterpolationOfWordAndSubWordLM i = new InterpolationOfWordAndSubWordLM(wlm,clm);
		i.evaluate(s);
		i.testFromFile(args[3]);
		//i.testSomeLambdas(s);
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




