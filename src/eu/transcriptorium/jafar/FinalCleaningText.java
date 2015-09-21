/*   Final code for cleaning text         */
/*   Last Edit 20 June 2014            */

package eu.transcriptorium.jafar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Pattern;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;

/**
 * Dit is WProc.jar
 * Pas op: wat voor raar karakter komt hiet in voor?
 * **/
public class FinalCleaningText
{

	public CharacterSet characterSet;
	
	public FinalCleaningText(CharacterSet cs)
	{
		this.characterSet = cs;
	}
	
	public void processFile(String inFile, String cleanedFile, String normalizedFile)
	{
		try
		{
			InputStream is = new FileInputStream(inFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf8"));
			PrintWriter cleanedOutput = new PrintWriter(new BufferedWriter(new FileWriter(cleanedFile)));
			PrintWriter normalizedOutput = new PrintWriter(new BufferedWriter(new FileWriter(normalizedFile)));
			
			String line;
			
			while ((line = br.readLine()) != null)
			{
				for (String w: line.split("\\s+"))
				{
					//String[] chars = characterSet.wordToModelNames(w);
					//System.err.println(Arrays.asList(chars));
					String cleaned = characterSet.cleanWord(w);
					if (cleaned != null && cleaned.length() > 0)
					{
						cleanedOutput.print(cleaned +  " ");
						String normalized = characterSet.normalize(cleaned);
						normalizedOutput.print(normalized +  " ");
					}
				}
				cleanedOutput.println();
				normalizedOutput.println();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		CharacterSet cs = new AlejandrosNewBenthamTokenization();
		cs.loadFromHMMList(args[1]);
		FinalCleaningText ftc = new FinalCleaningText(cs);
		ftc.processFile(args[0], args[2], args[3]);
	}
}
