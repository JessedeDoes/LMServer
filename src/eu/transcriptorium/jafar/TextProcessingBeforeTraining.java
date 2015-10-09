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
	String charsetFileName;
	String trainingCorpusFileName;

	int cutoff = 0;
	int numStates = 6;
	
	public void processText(String characterProcessingClassName, 
			String specialLabelFile, String textLineDir, String trainingPartitionList, String trainingLinesFile, String outputFolder)
	{
		labelFileName = outputFolder + "/"  + "labelFile.mlf";
		dictionaryFileName = outputFolder + "/"  + "dictionary.txt";
		HMMListFileName = outputFolder + "/" + "HMMs.list";
		charsetFileName = outputFolder + "/" + "charset.txt";
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
		
		if (specialLabelFile != null)
			characterSet.loadFromHMMList(specialLabelFile);
		characterSet.setAcceptAll();
		textExtraction.setCharacterSet(characterSet);
		textExtraction.printLabelFileFromDirectoryWithLineTranscriptions(textLineDir, 
				trainingPartitionList, 
				trainingLinesFile,
				trainingCorpusFileName,
				labelFileName);
		textExtraction.printHMMList(HMMListFileName, charsetFileName, numStates);
		talp = new TextAndLexicalProcessing(characterSet);
		talp.processPlainCorpusText(trainingCorpusFileName, outputFolder, cutoff);
	}

	static String[] defaultArgs = 
		{
				"eu.transcriptorium.lm.charsets.DutchArtesTokenization",
				"/home/jesse/workspace/LMServer/resources/CharacterSets/special_labels.txt",
				"/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Transcriptions",
				"/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/train.lst",
				"/home/jesse/TUTORIAL-HTR/EXP-MEERMANNO/TRAIN/Train-Lines.lst.x",
				"/tmp/MMTest"
		};

	public static void main(String[] args)
	{
		TextProcessingBeforeTraining tpbt = new TextProcessingBeforeTraining();
		if (args.length != 4)
			args = defaultArgs;
		tpbt.processText(args[0], args[1], args[2], args[3], args[4], args[5]);
	}
}
