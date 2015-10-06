package eu.transcriptorium.jafar;

import eu.transcriptorium.lm.CharacterSet;

/**
 * This one is for text processing after training of the HTR system
 * @author jesse
 *
 */
public class TextAndLexicalProcessing
{
	CharacterSet characterSet;
		
	public TextAndLexicalProcessing(String className, String fileName)
	{
		try
		{
			CharacterSet cs = instanceOfCharacterSet(className);
			System.err.println("Character set: " + cs);
			if (fileName != null)
				cs.loadFromHMMList(fileName);
			this.characterSet = cs;
		} catch (Exception  e)
		{
			e.printStackTrace();
		}
	}

	public TextAndLexicalProcessing(CharacterSet cs)
	{
		this.characterSet = cs;
	}
	
	public static CharacterSet instanceOfCharacterSet(String className)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		Class c = Class.forName(className);
		Object o  = c.newInstance();
		CharacterSet cs = (CharacterSet) o;
		return cs;
	}
	
	public void processPlainCorpusText(String inputFileName, String outputFolder, int cutoff)
	{
		FinalCleaningText tc = new FinalCleaningText(characterSet);
		String cleanedFileName = outputFolder + "/"  + "cleanedText.txt";
		String normalizedFileName = outputFolder + "/"  + "normalizedText.txt";
		String dictionaryFileName = outputFolder + "/"  + "dictionary.txt";
		String frequencyListFileName = outputFolder + "/wordFrequencyList.txt";
		String normalizedWordListFileName = outputFolder + "/normalizedWordList.txt";
		
		tc.processFile(inputFileName, cleanedFileName, normalizedFileName);
		
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
		TextAndLexicalProcessing talp = new TextAndLexicalProcessing(args[0], args[1]);
		int cutoff = Integer.parseInt(args[3]);
		talp.processPlainCorpusText(args[2], args[4], cutoff);
	}
}