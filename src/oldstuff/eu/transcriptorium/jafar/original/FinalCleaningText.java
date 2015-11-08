/*   Final code for cleaning text         */
/*   Last Edit 20 June 2014            */

package oldstuff.eu.transcriptorium.jafar.original;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * Dit is WProc.jar
 * Pas op: wat voor raar karakter komt hiet in voor?
 * **/
public class FinalCleaningText
{

	public static String escapeCharacter(String w, char ch)
	{
		int l = 0, flag = 1, index = -1;
		int n = w.length();
		char[] charst = w.toCharArray();
		String result = "";
		while (l < n && flag == 1)
		{
			if (charst[l] == ch)
			{
				result = result + '\\' + ch;
			} else
				result = result + charst[l];
			l++;
		}
		return result;
	}

	public static void main(String[] args) throws IOException
	{
		if (args == null)
		{
			System.out.println("please identy the parameters.....");
			System.out
					.println("Use WProc.jar  inputfile CharacterSet outputfile \n\n ");
		} else if (args.length < 3)
		{
			System.out.println(" You have used a wrong parameter!\n");
			System.out
					.println(" Please use WProc.jar  inputfile CharacterSet outputfile \n");
		} else
		{
			InputStream is = new FileInputStream(args[0]);
			String UTF8 = "utf8";

			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					UTF8));
			// Character set files
			InputStream isCharSet = new FileInputStream(args[1]);
			String UTF8Set = "utf8";

			BufferedReader brCharSet = new BufferedReader(
					new InputStreamReader(isCharSet, UTF8Set));
			// BufferedReader br = new BufferedReader(new FileReader(args[0]));
			System.out
					.println("\n\n \t \t Text prepocessing of the original text is started! and It takes a few seconds!");
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(args[2])));

			try
			{
				String line = br.readLine(), st = "�"; // what is that???

				String acceptableWordRegex = ImportCharacterSet(brCharSet);
				// System.out.println("character set/\n"+SSampleStringForMatch);

				//String SampleStringForMatch = null;
				// "[a-zA-W0-9,!,#,&,(,),+,,,\\-,�,.,/,:,;,\',\",=,?,@,\\[,\\],_,|,�]*";
				// //Character Set

				//SampleStringForMatch = SSampleStringForMatch;
				int flag = 0;
				// System.out.println("SampleStringForMatch== "+SampleStringForMatch);
				while (line != null)
				{
					String[] wordsInLine = line.split(" ");
					for (String word : wordsInLine)
					{
						word = word.replaceAll("\\\\", "");
						String W = word;
						// System.err.println(W);
						boolean writeIt = W.matches(acceptableWordRegex);

						/*
						 * if(writeIt){ if(!(W.contains("<") || W.contains(">"))
						 * ) System.out.println(" accepted  word == "+W); }
						 */

						if (W.contains("\'"))
						{
							if (W.matches(acceptableWordRegex))
							{
								// System.out.println("before replacment queote==== "+words);

								word = escapeCharacter(word, '\'');
								// System.out.println("single queote==== "+words);
								// if(W.contains("\""))
								// W=ReplaceAll(W,'\"');
								// out.print(W+" ");
							}

						}
						
						if (W.contains("\""))
						{
							if (W.matches(acceptableWordRegex))
							{
								// System.out.println("before replace,ment dqueote==== "+words);

								word = escapeCharacter(word, '\"');
								// System.out.println("dqueote==== "+words);
								// if(W.contains("\'"))
								// W=ReplaceAll(W,'\'');
								// out.print(W+" ");
							}

						}
						/*
						 * else if(W.contains(st) ){ int index=W.indexOf(st);
						 * System.out.println("before changing=== "+W);
						 * W=W.substring(0, index)+'-'+W.substring(index+3,
						 * W.length()); out.print(W+" ");
						 * System.out.println("after  changing=== "+W); }
						 */
						if (Pattern.matches(acceptableWordRegex, W))
						{
							while (word.contains(st) == true)
							{
								int index = word.indexOf(st);
								// System.out.println("before changing=== "+W);
								word = word.substring(0, index)
										+ '-'
										+ word.substring(index + 1,
												word.length());
							}

							// System.out.println("after  changing=== "+W);

						}

						if (writeIt)
						{
							if (!(W.contains("<") || W.contains(">")))
							{
								out.print(word + " ");
								flag = 1;

							} else if (W.contains("<GAP/>")
									|| W.contains("<gap/>"))
							{
								out.print(word + " ");
								flag = 1;
							}
						}
					}// for

					if (flag == 1)
						out.println();
					flag = 0;
					line = br.readLine();
				}
				br.close();
				out.close();
			} finally
			{
				// System.out.println("Errorrrrrrrrrrrrrrrrrrrr");
			}

			System.out.println("\n \t\t It's done! and the results are  in: "
					+ args[2] + "  file");

		}

	}

	private static String ImportCharacterSet(BufferedReader brCharSet)
			throws IOException
	{
		// TODO Auto-generated method stub
		String line1 = brCharSet.readLine();
		String SSampleStringForMatch = "[" + line1;
		while (line1 != null)
		{
			line1 = brCharSet.readLine();
			if (line1 != null)
			{
				if (line1.contains("\'"))
					SSampleStringForMatch += "," + "\'";
				else if (line1.contains("\""))
					SSampleStringForMatch += "," + "\"";
				else if (line1.contains("-"))
					SSampleStringForMatch += "," + "\\-";
				else if (line1.contains("["))
					SSampleStringForMatch += "," + "\\[";
				else if (line1.contains("]"))
					SSampleStringForMatch += "," + "\\]";

				else
					SSampleStringForMatch += "," + line1;

			}

		}
		SSampleStringForMatch += "]*";
		return SSampleStringForMatch;
	}

}
