package eu.transcriptorium.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;

public class LMBuilder 
{
	//public String destinationFolder = "/datalokaal/Corpus/LM/build";
	public File buildLM(int lmOrder, List<String> inputFiles)
	{
		File outputFile = null;
		try
		{
			outputFile = File.createTempFile("lmx", ".lm");
		
			buildLM(lmOrder, inputFiles, outputFile.getCanonicalPath());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return outputFile;
	}

	private void buildLM(int lmOrder, List<String> inputFiles, String outputFile) 
	{
		Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
		Logger.startTrack("Reading text files " + inputFiles + " and writing to file " + outputFile);

		final StringWordIndexer wordIndexer = new StringWordIndexer();
		wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
		wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
		wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
		LmReaders.createKneserNeyLmFromTextFiles(inputFiles, wordIndexer, lmOrder, new File(outputFile), new ConfigOptions());
	}
	
	public void buildLMFromOneText(int lmOrder, String inputFileName, String outputFileName)
	{
		List<String> in = new ArrayList<String>();
		in.add(inputFileName);
		buildLM(lmOrder,in,outputFileName);
	}
}
