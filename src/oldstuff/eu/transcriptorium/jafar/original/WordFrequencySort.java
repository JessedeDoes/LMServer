/*  Last changes in June 2014 */
package oldstuff.eu.transcriptorium.jafar.original;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 
 * @author jafar Dits is WList.jar
 */

public class WordFrequencySort
{

	/**
	 * @param args
	 */
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
				// PrintWriter out = new PrintWriter(new BufferedWriter(new
				// FileWriter("d:/tanha.txt")));
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

				in.close();
				int Num = Integer.parseInt(args[5]);

				Map<String, Integer> unsortlits = new HashMap<String, Integer>();

				unsortlits = printMapHash(m, output, Num);

				output.close();

				Map<String, Integer> sorted = sortByValues(unsortlits);

				printMap(sorted, sortoutput, Num);

				sortoutput.close();

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

	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(
			Map<K, V> map)
	{
		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2)
			{
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();

		for (Map.Entry<K, V> entry : entries)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static void printMap(Map<String, Integer> map,
			PrintWriter sortoutput, int num)
	{
		sortoutput.println("W.List" + "\t\t" + "W.Frequency");
		// sortoutput.println("!ENTER"+"\t\t"+"1");
		// sortoutput.println("!EXIT"+"\t\t"+"1");
		// sortoutput.println("<s>"+"\t\t"+"1");
		// sortoutput.println("</s>"+"\t\t"+"1");

		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			// System.out.println("Key : " + entry.getKey() + " Value : "
			// + entry.getValue());
			int freq = entry.getValue();
			if (freq > num)
				sortoutput.println(entry.getKey() + "\t\t" + entry.getValue());
		}
	}

	public static Map<String, Integer> printMapHash(Map<String, Integer> map,
			PrintWriter sortoutput, int num)
	{
		// sortoutput.println("!ENTER");
		// sortoutput.println("!EXIT");
		// sortoutput.println("<s>");
		// sortoutput.println("</s>");

		Map<String, Integer> unsortlits = new HashMap<String, Integer>();

		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			// System.out.println("Key : " + entry.getKey() + " Value : " +
			// entry.getValue());
			String st = (String) entry.getKey();
			int freq = entry.getValue();
			unsortlits.put(st, freq);
			if (freq > num)
				sortoutput.println(entry.getKey());
		}
		return unsortlits;
	}
}
