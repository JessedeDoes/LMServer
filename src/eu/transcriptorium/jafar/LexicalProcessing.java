package eu.transcriptorium.jafar;

import eu.transcriptorium.lm.CharacterSet;

public class LexicalProcessing extends TextAndLexicalProcessing
{
	public LexicalProcessing(String className, String fileName)
	{
	    super(className,fileName);
	}
	
	public void processPlainCorpusText(String inputFileName, String outputFolder, int cutoff)
	{
		FinalCleaningText tc = new FinalCleaningText(characterSet);
		String cleanedFileName = outputFolder + "/"  + "cleanedText.txt";
		String normalizedFileName = outputFolder + "/"  + "normalizedText.txt";
		String dictionaryFileName = outputFolder + "/"  + "dictionary.txt";
		String frequencyListFileName = outputFolder + "/wordFrequencyList.txt";
		String normalizedWordListFileName = outputFolder + "/normalizedWordList.txt";
		
		// tc.processFile(inputFileName, cleanedFileName, normalizedFileName);
		
		WordFrequencySort s = new WordFrequencySort(characterSet);
		
		String[] frequencyListArgs = 
			{
				"-i", cleanedFileName,
				"-o", outputFolder + "/not_used_list.txt",
				"-n", new Integer(cutoff).toString(),
				"-s", frequencyListFileName
			};
		try
		{
			s.process(frequencyListArgs);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		BuildDictionaryFromOriginalText  db = new BuildDictionaryFromOriginalText(characterSet);
		db.processFiles(frequencyListFileName, dictionaryFileName, normalizedWordListFileName);
	}
	
	public static void main(String[] args)
	{
		TextAndLexicalProcessing talp = new LexicalProcessing(args[0], args[1]);
		int cutoff = Integer.parseInt(args[3]);
		talp.processPlainCorpusText(args[2], args[4], cutoff);
	}
}
