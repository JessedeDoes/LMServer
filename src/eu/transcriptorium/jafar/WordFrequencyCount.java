package eu.transcriptorium.jafar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is jafar's WFreq.jar
 * 
 * Currently not in use
 * @author does
 *
 */
@Deprecated
public class WordFrequencyCount
{

	private static final int N = 1;

	public static void main(String args[]) throws Exception
	{

		if (args == null)
		{
			System.out.println("please identy the parameters.....");
			System.out
					.println("-i input file name \n -o output file name, which is the word list\n -n number of frequency \n -s sorted file name.\n ");
			// wait(0);
		} else if (args.length < 8)
		{
			System.out
					.println("please identy the parameters. The guidelnie is as follow:");
			System.out
					.println("-i input file name \n -o output file name, which is the word list\n -n number of frequency \n -s sorted file name. ");

		} else
		{
			// System.out.println("args[0]==-i && args[2]==-o && args[4]==-n ==>"
			// + args[0]+" "+args[2]+" "+args[4]);
			if ((args[0].contains("-i") && (args[2].contains("-o"))
					&& (args[4].contains("-n")) && (args[6].contains("-s"))))
			{
				String filePath = args[1];
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("tanha.txt")));
				PrintWriter output = new PrintWriter(new BufferedWriter(
						new FileWriter(args[3])));
				PrintWriter sortoutput = new PrintWriter(new BufferedWriter(
						new FileWriter(args[7])));

				System.out.println(" The process was started!\n\n");
				// Map File from filename to byte buffer

				FileInputStream in = new FileInputStream(filePath);
				FileChannel filech = in.getChannel();
				int fileLen = (int) filech.size();
				MappedByteBuffer buf = filech.map(
						FileChannel.MapMode.READ_ONLY, 0, fileLen);

				// Convert to character buffer
				Charset chars = Charset.forName("UTF-8");
				CharsetDecoder dec = chars.newDecoder();
				CharBuffer charBuf = dec.decode(buf);

				// Create line pattern
				Pattern linePatt = Pattern.compile(".*$", Pattern.MULTILINE);

				// Create word pattern
				Pattern wordBrkPatt = Pattern.compile("[^.*\\S.*]");

				// Match line pattern to buffer
				Matcher lineM = linePatt.matcher(charBuf);
				Map m = new TreeMap();
				Integer one = new Integer(1);

				// For each line
				while (lineM.find())
				{
					// Get line
					CharSequence lineSeq = lineM.group();
					// Get array of words on line
					String words[] = wordBrkPatt.split(lineSeq);
					// For each word
					int freq = 0;
					for (int i = 0, n = words.length; i < n; i++)
					{
						if (words[i].length() > 0)
						{
							Integer frequency = (Integer) m.get(words[i]);
							if (frequency == null)
							{
								frequency = one;
							} else
							{

								int value = frequency.intValue();
								frequency = new Integer(value + 1);
							}
							m.put(words[i], frequency);
							freq = frequency;
						}
					}
				}
				// System.out.println(m);
				out.println(m);
				out.close();
				in.close();

				BufferedReader br = new BufferedReader(new FileReader(
						"tanha.txt"));
				String line = br.readLine();
				String result = "", freq = "", str = "";
				int Num = Integer.parseInt(args[5]);
				List<String> lineList = new ArrayList<String>();
				int flag = 0;
				sortoutput.println("W.Frequency" + "\t\t" + "W.list");
				if (line != null)
				{
					// String [] words=line.split(" ");
					for (String Word : line.split(" "))
					{
						// System.out.println(Word);
						int index = Word.lastIndexOf("=");
						if (index > 0)
						{
							if (flag == 0)
							{
								result = Word.substring(1, index);
								flag = 1;
							} else
								result = Word.substring(0, index);// +"\t"+Word.substring(index+1,Word.length()-1);
							freq = Word.substring(index + 1, Word.length() - 1);
						} else
							System.out
									.println("Error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						// System.out.println("frequency of the words== "+freq+"  related wprd== "+result);
						int number = Integer.parseInt(freq);
						// System.out.println("number=========="+number);
						str = freq + '\t' + '\t' + result;
						if (number >= Num)
						{
							output.println(result);
							result = "";
						} else
						{
							result = "";
							System.out
									.println("lessssssss frequentttttttttttttttttttttttttttttt");
						}

						lineList.add(str);
					}
					Collections.sort(lineList);

					for (String outputLine : lineList)
					{
						sortoutput.println(outputLine);
					}

				} else
					System.out
							.println("The file is empty!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				br.close();
				sortoutput.close();
				output.close();
			} else
			{
				System.out.println("You have used wrong paramters!");
				System.out.println("please identy the parameters.....");
				System.out
						.println("-i input file name \n -o output file name, which is the word list\n -n number of frequency \n -s sorted file name.\n ");
			}
		}
		System.out.println("\n\n The process was finished!");

	}
}
