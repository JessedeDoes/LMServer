package eu.transcriptorium.jafar;

import eu.transcriptorium.lm.CharacterSet;

public class TextAndLexicalProcessing
{
	CharacterSet characterSet;
	
	//  java -classpath $CLASSPATH eu.transcriptorium.jafar.WordFrequencySort -i $OUTPUT/cleanedText.txt -o $OUTPUT/not_used_list.txt -n $CUTOFF -s $OUTPUT/wordFrequencyList.txt

	public TextAndLexicalProcessing(String className, String fileName)
	{
		try
		{
			Class c = Class.forName(className);
			Object o  = c.newInstance();
			CharacterSet cs = (CharacterSet) o;
			System.err.println("Character set: " + cs);
			cs.loadFromHMMList(fileName);
			this.characterSet = cs;
		} catch (Exception  e)
		{
			e.printStackTrace();
		}
	}
	
	public void processing(String inputFileName, String outputFolder, int cutoff)
	{
		FinalCleaningText tc = new FinalCleaningText(characterSet);
		String cleanedFileName = outputFolder + "/"  + "cleanedText.txt";
		String normalizedFileName = outputFolder + "/"  + "normalizedText.txt";
		String dictionaryFileName = outputFolder + "/"  + "dictionary.txt";
		String frequencyListFileName = outputFolder + "/wordFrequencyList.txt";
		String normalizedWordListFileName = outputFolder + "/normalizedWordList.txt";
		
		tc.processFile(inputFileName, cleanedFileName, normalizedFileName);
		
		WordFrequencySort s = new WordFrequencySort(characterSet);
		
		String[] sortArgs = 
			{
				"-i",  cleanedFileName,
				"-o", outputFolder + "/not_used_list.txt",
				"-n", new Integer(cutoff).toString(),
				"-s", frequencyListFileName
			};
		try
		{
			s.process(sortArgs);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// java -classpath $CLASSPATH eu.transcriptorium.jafar.BuildDictionaryFromOriginalText 
		// $CHARSET $OUTPUT/wordFrequencyList.txt $OUTPUT/dictionary.txt $OUTPUT/normalizedWordList.txt

		BuildDictionaryFromOriginalText  db = new BuildDictionaryFromOriginalText(characterSet);
		db.processFiles(frequencyListFileName, dictionaryFileName, normalizedWordListFileName);
	}
	
	public static void main(String[] args)
	{
		TextAndLexicalProcessing talp = new TextAndLexicalProcessing(args[0], args[1]);
		int cutoff = Integer.parseInt(args[3]);
		talp.processing(args[2], args[4], cutoff);
	}
}
