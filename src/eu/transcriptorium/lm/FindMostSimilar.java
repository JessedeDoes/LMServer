package eu.transcriptorium.lm;

import java.io.BufferedReader;
import java.io.*;
import java.io.InputStreamReader;
import java.util.*;

import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.ArpaLmReader;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;

public class FindMostSimilar 
{
	Map<String, Float> scores = new HashMap<String,Float>();
	NgramLanguageModel lm = null;
	
	public static void buildLM(String outputFile, int lmOrder, final List<String> inputFiles) 
	{	
		

		final StringWordIndexer wordIndexer = new StringWordIndexer();
		wordIndexer.setStartSymbol(ArpaLmReader.START_SYMBOL);
		wordIndexer.setEndSymbol(ArpaLmReader.END_SYMBOL);
		wordIndexer.setUnkSymbol(ArpaLmReader.UNK_SYMBOL);
		//LmReaders.readKneserNeyLmFromTextFile(files, wordIndexer, lmOrder, compress, opts, tmpFile)
		LmReaders.createKneserNeyLmFromTextFiles(inputFiles, wordIndexer, lmOrder, new File(outputFile), new ConfigOptions());
		
	}

	public float score(String fileName)
	{
		String s;
		int rejects=0;
		float sum=0;
		
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fileName));
			float total=0;
			int k=0;
			
			while ((s = b.readLine()) != null)
			{
				s = s.replaceAll("\\p{P}", "");
				s = s.replaceAll("\\s+", " ");
				s = s.trim();
				String[] words = s.split("\\s+");
				
				List<String> wordList = new ArrayList<String>();
				
				for (String w: words)
				{
					if (w.matches("\\S+"))
					wordList.add(w);
				}
				if (wordList.size() < 1)
					continue;
				
				//System.err.println(wordList + " " + wordList.size());
				List<String> w = new ArrayList<String>();
				//w.add(ArpaLmReader.START_SYMBOL);
				w.addAll(wordList);
				//w.add("beter"); // Berkely lm bug? sentence-final word backoff does somehow NOT work. Why???
				w.add(ArpaLmReader.END_SYMBOL);
				wordList = w;
				// there is no need to add sentence bounds, the scoreSentence method does that
				// (but not during training...) (?) it appears that it does, so why the earlier trouble??
			
				float p = lm.scoreSentence(wordList) / (float) words.length;
				if (!Float.isNaN(p))
				{
					total += p;
					k++;
					System.err.println(p + "\t" + s);
				} else
				{
					System.err.println("Could not score "  + wordList);
					rejects++;
				}
			}
			float avg = total / (float) k;
			return avg;
			///System.err.println("Average sentence cross entropy: " + total / (double) k + " rejects: "  + rejects);
		} catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	public class Comp implements Comparator<String>
	{

		@Override
		public int compare(String arg0, String arg1) {
			// TODO Auto-generated method stub
			Float f0 = scores.get(arg0);
			Float f1 = scores.get(arg1);
			return f0.compareTo(f1);
		}
		
	}
	public void findMostSimilar(String target, List<String> files)
	{
		try
		{
			File tmpModel = File.createTempFile("model.", ".lm");
			List<String> targets = new ArrayList<String>(); 
			targets.add(target);
			buildLM(tmpModel.getCanonicalPath(),3,targets);
			String languageModel = tmpModel.getCanonicalPath();

			// languageModel = null;

			if (languageModel != null)
			{
				if (!languageModel.endsWith(".bin"))
					lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
				else
					lm = LmReaders.readLmBinary(languageModel);
				System.err.println("finished reading LM");
			}
			//lm.setOovWordLogProb(-1);
			for (String f: files)
			{
				scores.put(f, score(f));
			}
			Collections.sort(files,new Comp());
			for (String s: files)
			{
				System.out.println(s +  "\t" + scores.get(s));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		List<String> l = new ArrayList<String>();
		for (int i=1; i < args.length; i++)
			l.add(args[i]);
		FindMostSimilar f = new FindMostSimilar();
		f.findMostSimilar(args[0], l);
	}
}
