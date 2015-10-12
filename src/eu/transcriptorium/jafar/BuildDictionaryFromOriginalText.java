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
import java.util.Collections;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;
import impact.ee.util.StringUtils;

/** This is main class in jafar's WDic.jar, updated and simplified version
 * The name is a bit misleading: it builds from previously constructed word frequency lists
 * TODO: make this more flexible WRT the way whitespace etc is handled
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
	DecimalFormat numberFormat = new DecimalFormat("#.000000000");

	Map<String,Integer> caseSensitiveTypeFrequency = new HashMap<String,Integer>();
	Map<String, List<String>> normalized2Variants = new HashMap<String,List<String>>();


	public BuildDictionaryFromOriginalText(CharacterSet cs) 
	{
		this.characterSet = cs;
	}

	public void processFiles(String frequencyList, String outFile, String normalizedWordListOut)
	{
		try
		{
			BufferedReader frequencylistReader = new BufferedReader(new FileReader(frequencyList));
			PrintWriter out = new PrintWriter(new BufferedWriter( new FileWriter(outFile)));

			outputSpecialTokens(out);

			try
			{
				readFrequencyList(frequencylistReader);
				frequencylistReader.close();
				printDictionary(out);
				if (normalizedWordListOut != null)
				{
					PrintWriter wout = new PrintWriter(new BufferedWriter( new FileWriter(normalizedWordListOut)));
					this.writeNormalizedWordList(wout);
					wout.close();
				}
				out.flush();
				out.close();
			} finally
			{
				frequencylistReader.close();
				out.close();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * TODO: This should depend on character processing settings...
	 * @param out
	 */

	private void outputSpecialTokens(PrintWriter out) 
	{
		out.println("\"" + "<s>" + "\"" + "\t" + "[" + "]" + "\t" + "<BS>");
		out.println("\"" + "</s>" + "\"" + "\t" + "[" + "]" + "\t" + "<ES>");
	}


	private void writeNormalizedWordList(PrintWriter out)
	{
		out.println("<s>");
		out.println("</s>");
		List<String> normalizedWords = new ArrayList<String>();
		normalizedWords.addAll(normalized2Variants.keySet());
		Collections.sort(normalizedWords);
		for (String norm: normalizedWords)
		{
			out.println(norm);
		}
	}

	private  void printDictionary(PrintWriter out) throws IOException 
	{
		String wline;
		List<String> normalizedWords = new ArrayList<String>();
		normalizedWords.addAll(normalized2Variants.keySet());
		Collections.sort(normalizedWords);

		for (String norm: normalizedWords)
		{
			List<String> variants = normalized2Variants.get(norm);
			if (variants == null) 
				continue;
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

		}
	}
	/**
	 * Read the frequency list, store variants and frequencies
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
			String arg3 = args.length > 3?args[3]:null;
			db.processFiles(args[1], args[2], arg3);
		}
	}

	@Deprecated
	private  void outputVariantX(String unnormalizedWord, double num, PrintWriter out)
	{
		String normalized = characterSet.normalize(unnormalizedWord);
		out.print("\"" + characterSet.escapeWord(normalized) + "\"" + "\t");

		// do we need to escape the square brackets?

		String unescaped =unnormalizedWord ; //.unescapeWord(unnormalizedWord);

		String gap = "<gap/>";

		if (unescaped.contains(gap)) // this should be inside the character processing class
		{
			out.print("[" + unescaped + "]" + "\t" + numberFormat.format(num) + "\t" + "<GAP>" + " ");
		} else
		{	
			String[] modelNames = characterSet.wordToModelNames(unnormalizedWord);
			out.print("[" + unescaped + "]" + "\t" + numberFormat.format(num)
			+ "\t" + eu.transcriptorium.util.StringUtils.join(modelNames, " "));
		}	
		out.println();
	}

	private  void outputVariant(String unnormalizedWord, double num, PrintWriter out)
	{
		out.print("\"" + characterSet.normalize(unnormalizedWord) + "\"" + "\t");

		String unescaped = characterSet.unescapeWord(unnormalizedWord);

		String gap = "<gap/>";

		if (unescaped.contains(gap)) // this should be inside the character processing class
		{
			out.print("[" + unescaped + "]" + "\t" + numberFormat.format(num) + "\t" + "<GAP>" + " ");
		} else
		{	
			String[] modelNames = characterSet.wordToModelNames(unnormalizedWord);
			out.print("[" + unescaped + "]" + "\t" + numberFormat.format(num)
			+ "\t" + eu.transcriptorium.util.StringUtils.join(modelNames, " "));
		}	
		out.println();
	}
}
