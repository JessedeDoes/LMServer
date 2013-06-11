package eu.transcriptorium.lm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.lm.ArrayEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.BoundedList;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;
/*
 * Need to redefine both indexing and scoring
 * actually why not override getLogProb?
 */
public class LexiconEnabledLM 
{
	VariantLexicon lexicon;
	
	private static void usage() 
	{
		System.err.println("Usage: <lmOrder> <ARPA lm output file> <textfiles>*");
		System.exit(1);
	}

	static class Builder
	{
		public static void main(final String[] argv) 
		{
			if (argv.length < 2) 
			{
				usage();
			}
			final int lmOrder = Integer.parseInt(argv[0]);
			final String lexiconFilename = argv[1];
			final String outputFile = argv[2];

			final List<String> inputFiles = new ArrayList<String>();
			for (int i = 3; i < argv.length; ++i) 
			{
				inputFiles.add(argv[i]);
			}
			VariantLexicon vl = new VariantLexicon(); 
			vl.loadFromFile(lexiconFilename);
			LexiconEnabledLM lelm = new LexiconEnabledLM();
			lelm.makeModel(inputFiles, lmOrder, outputFile);
		}
	}
	
	public static void makeModel(List<String> inputFiles, int lmOrder, String outputFile)
	{
		if (inputFiles.isEmpty()) inputFiles.add("-");
		
		Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
		Logger.startTrack("Reading text files " + inputFiles + " and writing to file " + outputFile);
		final NormalizingStringWordIndexer wordIndexer = new NormalizingStringWordIndexer();
		wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
		wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
		wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
		
		LmReaders.createKneserNeyLmFromTextFiles(inputFiles, (WordIndexer<String>) wordIndexer, lmOrder, new File(outputFile), new ConfigOptions());
		
		Logger.endTrack();
	}
	
	/**
	 * This assumes that each witnessed for maps to only one normalized form,
	 * which is a bit unlikely for linguistically motivated models ...<br>
	 * If we assume that a witness form has several possible 'normal forms'
	 * (e.g. german kain -> "kein" | "Kain", etc), sentences have to be scored using 
	 * a viterbi or HMM forward evaluation procedure
	 * @param sentence
	 * @param lm
	 * @return
	 */
	public  float scoreSentence(final List<String> sentence, final NgramLanguageModel<String> lm) 
	{
		List<String> normalizedSentence = new ArrayList<String>();
		
		for (String w: sentence)
		{
			normalizedSentence.add(lexicon.getNormalizedWordform(w));
		}
		
		final List<String> normalizedSentenceWithBounds = new BoundedList<String>(normalizedSentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
		final List<String> sentenceWithBounds = 
				new BoundedList<String>(sentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
		final int lmOrder = lm.getLmOrder();
		float sentenceScore = 0.0f;
	
		for (int i = 1; i < lmOrder - 1 && i <= normalizedSentenceWithBounds.size() + 1; ++i) 
		{
			final List<String> ngram = normalizedSentenceWithBounds.subList(-1, i);
			final float scoreNgram = lm.getLogProb(ngram);
			sentenceScore += scoreNgram + lexicon.getLogRealizationProbability(normalizedSentenceWithBounds.get(i), sentence.get(i));
		}
		
		for (int i = lmOrder - 1; i < normalizedSentenceWithBounds.size() + 2; ++i) 
		{
			final List<String> ngram = normalizedSentenceWithBounds.subList(i - lmOrder, i);
			final float scoreNgram = lm.getLogProb(ngram);
			sentenceScore += scoreNgram + lexicon.getLogRealizationProbability(normalizedSentenceWithBounds.get(i), sentence.get(i));
		}
		
		return sentenceScore;
	}
	
	/**
	 * 
	 */
	public void testViterbiStuff()
	{
		try
		{
			List<String> inputFiles = Arrays.asList("resources/exampleData/normalizedTrainingCorpus.txt");
			File out = File.createTempFile("test", "lm");
			
			final StringWordIndexer wordIndexer = new StringWordIndexer();
			wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
			wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
			wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
			
			// TODO: order 2 does not work.. fix it...
			LmReaders.createKneserNeyLmFromTextFiles(inputFiles,  wordIndexer, 3, out, new ConfigOptions());
			
			VariantLexicon lexicon  = new VariantLexicon();
			lexicon.loadFromFile("resources/exampleData/variantLexicon.txt");
			
			NgramLanguageModel<String> lm = LmReaders.readArrayEncodedLmFromArpa(out.getCanonicalPath(), false);
			List<String> sentence = Arrays.asList("x","x","x","x","x", "x", "x","x", "x","x","x","x","x","x","x");
			
			for (int i=0; i < 1; i++)
			{
				SimpleViterbiDecoder sv = new SimpleViterbiDecoder();
				sv.scoreSentence(sentence, lm, lexicon);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		LexiconEnabledLM lelm = new LexiconEnabledLM();
		lelm.testViterbiStuff();
	}
}
