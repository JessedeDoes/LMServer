/* This code has been edited in June 2014  */

package eu.transcriptorium.jafar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;
import impact.ee.util.StringUtils;

/** Dit is WDic.jar in de laatste versie (jafar's HTR dictionary building command).
 * The name is a bit misleading: it builds from previously constructed word lists...
 * TODO: make this flexible WRT the way whitespace etc is handled
 * Argumenten: 
 * 0. Character list
 * 1. word list file (gewoon 1 genormalizeerd woord per regel)
 * 2. frequentielijst (ongenormalizeerd woord, met syntax <woord> tab tab <frequentie>
 * 3. Output file
 * **/

public class BuildDictionaryFromOriginalText
{

	/**
	 * @param args
	 */

	CharacterSet characterSet;
	

	public BuildDictionaryFromOriginalText(CharacterSet cs) 
	{
		this.characterSet = cs;
	}

	/**
	 * looks in a case-insensitive way for occurrences of line in list, returns list of indexes.
	 * this seems horribly slow if done this way.
	 * @param wline
	 * @param wList
	 * @return
	 */
	
	Map<String,Integer> caseSensitiveTypeFrequency = new HashMap<String,Integer>();
	Map<String, List<String>> normalized2Variants = new HashMap<String,List<String>>();
	ArrayList<String> WList = new ArrayList<String>();
	ArrayList<Integer> Wfreq = new ArrayList<Integer>();

	
	public void processFiles(String wordList, String frequencyList, String outFile)
	{
		try
		{
			BufferedReader wordlistReader = new BufferedReader(new FileReader(wordList));
			BufferedReader frequencylistReader = new BufferedReader(new FileReader(frequencyList));
			PrintWriter out = new PrintWriter(new BufferedWriter( new FileWriter(outFile)));
			
			outputSpecialTokens(out);
			
			try
			{
				StringBuilder sb = new StringBuilder();
				
				readFrequencyList(frequencylistReader);
				readWordList(wordlistReader, out);
				wordlistReader.close();

				frequencylistReader.close();
				out.flush();
				out.close();
			} finally
			{
				wordlistReader.close();
				frequencylistReader.close();
				out.close();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This should depend on character processing settings...
	 * @param out
	 */
	
	private void outputSpecialTokens(PrintWriter out) 
	{
		out.println("\"" + "<s>" + "\"" + "\t" + "[" + "]" + "\t" + "@");
		out.println("\"" + "</s>" + "\"" + "\t" + "[" + "]");
	}

	/**
	 * Reads in (unnormalized!) word list and outputs variants
	 * Dictionary sorting is a bit strange this way...
	 * @param wordlistReader
	 * @param out
	 * @throws IOException
	 */
	
	private void readWordList(BufferedReader wordlistReader, PrintWriter out) throws IOException 
	{
		String wline;
		while ((wline = wordlistReader.readLine()) != null)
		{
			String norm = characterSet.normalize(wline);
			List<String> variants = normalized2Variants.get(norm);
			
			// System.err.println("Variants: " + variants);
			
			if (variants == null) continue;
			if (variants.size() == 1)
			{
				outputVariant(variants.get(0), 1.0, out);
			} else if (variants.size() > 1)
			{
				int sum  = 0;
				for (String v: variants)
				{
					int f = caseSensitiveTypeFrequency.get(v);
					sum += f;
				}
				for (String v: variants)
				{
					int f = caseSensitiveTypeFrequency.get(v);
					double num = (double) f / sum;
					outputVariant(v,num,out);
				}
			} 
			normalized2Variants.remove(norm);
		}
	}

	/**
	 * Read the frequency list store variants and frequencies
	 * @param frequencylistReader
	 * @throws IOException
	 */
	private void readFrequencyList(BufferedReader frequencylistReader) throws IOException 
	{
		String line; 
	
		while ((line = frequencylistReader.readLine()) != null)
		{
			
			String[] columns = line.split("\t+");
			
			String wn = characterSet.normalize(columns[0]);
			List<String> variants = normalized2Variants.get(wn);

			if (variants == null)
			{
				variants = new ArrayList<String>();
				normalized2Variants.put(wn, variants);
			}
			variants.add(columns[0]);
			int f = Integer.parseInt(columns[1]);
			caseSensitiveTypeFrequency.put(columns[0], f);
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		if (args.length < 3)
		{
			System.err.println("\n\n\t\t You have used a wrong parameter setting!\n");
			System.err.println(" \t\t Please use WDic.jar wordlistfile frequencylistfile (from original text file) output \n");
		} else
		{
			CharacterSet cs = new AlejandrosNewBenthamTokenization();
			cs.loadFromHMMList(args[0]);
			BuildDictionaryFromOriginalText db = new BuildDictionaryFromOriginalText(cs);
			db.processFiles(args[1] , args[2], args[3]);
		}
	}

	private  void outputVariant(String unnormalizedWord, double num, PrintWriter out)
	{
		
		out.print("\"" + characterSet.normalize(unnormalizedWord) + "\"" + "\t");
		
		String unescaped = characterSet.unescapeWord(unnormalizedWord);
		
		DecimalFormat numberFormat = new DecimalFormat("#.000000000");
		String frequency = numberFormat.format(num);
		
		String gap = "<gap/>";
		if (unescaped.contains(gap)) // this should be inside the character processing class
		{
			out.print("[" + unescaped + "]" + "\t" + frequency + "\t" + "<GAP>" + " ");
		} else
		{	
			String[] modelNames = characterSet.wordToModelNames(unescaped);
			out.print("[" + unescaped + "]" + "\t" + frequency + "\t" + eu.transcriptorium.util.StringUtils.join(modelNames, " "));
		}	
			
		out.println();
	}

}
