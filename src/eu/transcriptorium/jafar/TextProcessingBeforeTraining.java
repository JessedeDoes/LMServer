package eu.transcriptorium.jafar;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.page.ExtractText;
import eu.transcriptorium.page.TEITextDecoder;
import eu.transcriptorium.page.XMLTextDecoder;

public class TextProcessingBeforeTraining 
{
	ExtractText textExtraction = new ExtractText();
	TextAndLexicalProcessing talp;
	CharacterSet characterSet;
	String labelFileName;
	String dictionaryFileName;
	String HMMListFileName;
	String trainingCorpusFileName;

	int cutoff = 0;

	public void processText(String characterProcessingClassName, String textLineDir, String outputFolder)
	{
		labelFileName = outputFolder + "/"  + "labelFile.mlf";
		dictionaryFileName = outputFolder + "/"  + "dictionary.txt";
		HMMListFileName = outputFolder + "/" + "HMMs.list";
		trainingCorpusFileName = outputFolder + "/" + "trainingCorpus.txt";
		XMLTextDecoder xtd = new TEITextDecoder(TEITextDecoder.type.EXPANSIONS);
		
		textExtraction.setXmlStripper(xtd);
		try 
		{
			characterSet = TextAndLexicalProcessing.instanceOfCharacterSet(characterProcessingClassName);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) 
		{
			e.printStackTrace();
		}
		characterSet.setAcceptAll();
		textExtraction.setCharacterSet(characterSet);
		textExtraction.printLabelFileFromDirectoryWithLineTranscriptions(textLineDir, 
				trainingCorpusFileName,
				labelFileName);
		textExtraction.printStatistics(HMMListFileName);
		talp = new TextAndLexicalProcessing(characterSet);
		talp.processPlainCorpusText(trainingCorpusFileName, outputFolder, cutoff);
	}

	static String[] defaultArgs = 
		{
				"eu.transcriptorium.lm.charsets.DutchArtesTokenization",
				"/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Transcriptions",
				"/tmp/MMTest"
		};

	public static void main(String[] args)
	{
		TextProcessingBeforeTraining tpbt = new TextProcessingBeforeTraining();
		if (args.length != 3)
			args = defaultArgs;
		tpbt.processText(args[0], args[1], args[2]);
	}
}
