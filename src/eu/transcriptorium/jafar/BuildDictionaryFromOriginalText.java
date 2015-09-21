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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dit is WDic.jar in de laatste versie (jafar's HTR dictionary building command).
 * The name is wrong: it builds from previously constructed word lists.
 * TODO: make this flexible WRT the way whitespace etc is handled
 * Argumenten: 
 * 1. word list file (gewoon 1 genormalizeerd woord per regel)
 * 2. frequentielijst (ongenormalizeerd woord, met syntax <woord> tab tab <frequentie>
 * 3. Outputbestand
 * **/

public class BuildDictionaryFromOriginalText
{

	/**
	 * @param args
	 */

	private static boolean jafar = true;

	/**
	 * looks in a case-insensitive way for occurrences of line in list, returns list of indexes.
	 * this seems horribly slow if done this way.
	 * @param wline
	 * @param wList
	 * @return
	 */
	public static ArrayList<Integer> SearchArrayList(String wline,
			ArrayList<String> wList)
	{

		ArrayList<Integer> indexes = new ArrayList<Integer>();
		wline = wline.toLowerCase();
		// System.out.println("\n Lowercase Wlineeeeeeeee= "+wline);
		for (int i = 0; i < wList.size(); i++)
		{
			String st = wList.get(i);
			st = st.toLowerCase();
			// System.out.println("Wlist====================== "+st+"  Original worl in wlist is== "+wList.get(i)+"\n");
			if (wline.equals(st))
			{
				indexes.add(i);
				// System.out.println("yessssssssssssssssssssssssssssssssssss");
			}
		}

		return indexes;

	}

	public static int SumofWordFrequency(ArrayList<Integer> indexes,
			ArrayList<Integer> wfreq)
	{
		// TODO Auto-generated method stub
		int sum = 0;
		for (int i = 0; i < indexes.size(); i++)
			sum += wfreq.get(indexes.get(i));

		return sum;
	}

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub

		if (args.length < 3)
		{
			System.out
			.println("\n\n\t\t You have used a wrong parameter setting!\n");
			System.out
			.println(" \t\t Please use WDic.jar  wordlistfile frequencylistfile (from original text file) output \n");
		} else
		{
			BufferedReader wordlistReader = new BufferedReader(new FileReader(args[0]));
			// System.out.println(" The process was started!\n");

			BufferedReader frequencylistReader = new BufferedReader(new FileReader(args[1]));
			// System.out.println(" The process was started!\n");

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(args[2])));
			// PrintWriter check = new PrintWriter(new BufferedWriter(new
			// FileWriter("d:/check.txt")));

			/*
			 * out.println("\""+"!ENTER"+"\""+"\t"+"["+"!enter"+"]"+"\t"+1.0+"\t"
			 * +"! "+"e "+" n"+" t"+" e"+" r"+" @");
			 * out.println("\""+"!EXIT"+"\""
			 * +"\t"+"["+"!esit"+"]"+"\t"+1.0+"\t"+"! "
			 * +"e "+" s"+" i"+" t"+" @");
			 * out.println("\""+"<S>"+"\""+"\t"+"["+"<s>"
			 * +"]"+"\t"+1.0+"\t"+"x "+"s "+" x"+" @");
			 * out.println("\""+"</S>"+"\""
			 * +"\t"+"["+"</s>"+"]"+"\t"+1.0+"\t"+"x "+"/ "+" s"+" x"+" @");
			 * out.
			 * println("\""+"<s>"+"\""+"\t"+"["+"<s>"+"]"+"\t"+1.0+"\t"+"x "+
			 * "s "+" x"+" @");
			 * out.println("\""+"</s>"+"\""+"\t"+"["+"</s>"+"]"+
			 * "\t"+1.0+"\t"+"x "+"/ "+" s"+" x"+" @");
			 */

			// out.println("\""+"!ENTER"+"\""+"\t"+"["+"!enter"+"]"+"\t"+1.0+"\t"+"! "+"e "+" n"+" t"+" e"+" r"+" @");
			// out.println("\""+"!EXIT"+"\""+"\t"+"["+"!esit"+"]"+"\t"+1.0+"\t"+"! "+"e "+" s"+" i"+" t"+" @");
			// out.println("\""+"<S>"+"\""+"\t"+"["+"<s>"+"]"+"\t"+1.0+"\t"+"x "+"s "+" x"+" @");
			// out.println("\""+"</S>"+"\""+"\t"+"["+"</s>"+"]"+"\t"+1.0+"\t"+"x "+"/ "+" s"+" x"+" @");
			
			
			out.println("\"" + "<s>" + "\"" + "\t" + "[" + "]" + "\t" + "@");
			out.println("\"" + "</s>" + "\"" + "\t" + "[" + "]");

			try
			{
				StringBuilder sb = new StringBuilder();
				String wline = wordlistReader.readLine(); // format: just a list of words?
				String wfline = frequencylistReader.readLine(); // format word -tabtab - frequency??

				// wfline=wfr.readLine(); wfline=wfr.readLine();
				// wfline=wfr.readLine(); wfline=wfr.readLine();
				// wfline = frequencylistReader.readLine();
				//ArrayList<String> stringList = new ArrayList<String>();
				ArrayList<String> WList = new ArrayList<String>();
				ArrayList<Integer> Wfreq = new ArrayList<Integer>();

				Map<String,Integer> caseSensitiveMap = new HashMap<String,Integer>();
				Map<String, List<String>> caseInsensitiveIndex = new HashMap<String,List<String>>();
				int Num1 = 0, Num2 = 0;

				System.out
				.println("\n\n \t\t The Dictionary making program is started !!!");
				while (wfline != null)
				{
					// System.out.println();
					String[] NWord = wfline.split("\t\t");
					System.err.println(wfline);
					// System.out.print("NWord[0]==== "+NWord[0]);
					String wn = NWord[0].toLowerCase();
					List<String> variants = caseInsensitiveIndex.get(wn);

					if (variants == null)
					{
						variants = new ArrayList<String>();
						caseInsensitiveIndex.put(wn, variants);
					}
					variants.add(NWord[0]);
					System.err.println(variants);
					Num1 = Integer.parseInt(NWord[1]);
					caseSensitiveMap.put(NWord[0],Num1);

					
					WList.add(NWord[0]);


					// System.out.println("  NWord[1]====="+NWord[1]);
					Wfreq.add(Num1);
					wfline = frequencylistReader.readLine();
				}

				while (wline != null)
				{

					if (!jafar) // faster
					{
						List<String> variants = caseInsensitiveIndex.get(wline.toLowerCase());
						if (variants == null)
							continue;
						if (variants.size() == 1)
						{
							WriteInFile(variants.get(0), 1.0, out);
						} else if (variants.size() > 1)
						{
							int sum  = 0;
							for (String v: variants)
							{
								int f = caseSensitiveMap.get(v);
								sum += f;
							}
							for (String v: variants)
							{
								int f = caseSensitiveMap.get(v);
								double num = (double) f / sum;
								WriteInFile(v,num,out);
							}

						} 
					} else // original code, eventually remove
					{

						// System.out.println("indexesss======= "+indexes);

						ArrayList<Integer> indexes = new ArrayList<Integer>();

						indexes = SearchArrayList(wline, WList);
						if (indexes.size() == 1)
						{

							WriteInFile(WList.get(indexes.get(0)), 1.0, out);

						} else if (indexes.size() > 1)
						{
							// System.out.println("\n\n  more than one index==============================\n\n");
							int sum = SumofWordFrequency(indexes, Wfreq);
							for (int i = 0; i < indexes.size(); i++)
							{
								int x = Wfreq.get(indexes.get(i));
								double num = (double) x / sum;
								WriteInFile(WList.get(indexes.get(i)), num, out);
							}

							int n = indexes.size() - 1;
							for (int i = n; i >= 0; i--)
							{
								WList.remove(WList.get(indexes.get(i))); // useless
								Wfreq.remove(Wfreq.get(indexes.get(i))); 
							}
							// System.out.println("After removing "+ WList+
							// " freq=="+Wfreq);
						}

					}
					/*
					 * else{ System.out.println(
					 * "we have added before!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					 * break; }
					 */

					wline = wordlistReader.readLine();
				}

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

			System.out.println(" \n \t\t\t The process is finished! \n");

		}
	}

	private static void WriteInFile(String line, double num, PrintWriter out)
	{
		// TODO Auto-generated method stub
		out.print("\"" + line.toUpperCase() + "\"" + "\t");
		int i = 1, index = 1;
		String st = "";
		st = line;
		DecimalFormat numberFormat = new DecimalFormat("#.000000000");
		numberFormat.format(num);
		if (st.contains("<gap/>"))
		{
			System.out
			.println("Yessssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
			out.print("[" + st + "]" + "\t" + numberFormat.format(num) + "\t"
					+ "<GAP>" + " ");
		} else
		{
			// messing around with quotes...
			
			if (st.contains("\\\'") || st.contains("\\\""))
			{
				// System.out.println(st);
				while (st.contains("\\\"") || st.contains("\\\'"))
				{
					index = st.indexOf("\\\'");
					// System.out.println("indexxxx========= "+index);
					if (index == 0)
						st = st.substring(index + 1);
					else if (index > 0)
					{
						st = st.substring(0, index) + st.substring(index + 1);
						// System.out.println("st.substring(0, index)=="+st.substring(0,
						// index)+
						// "  st.substring(index+1)=="+st.substring(index));
					}
					if (st.contains("\\\""))
					{
						index = st.indexOf("\\\"");
						// System.out.println("indexxxx========= "+index);
						if (index == 0)
							st = st.substring(index + 1);
						else if (index > 0)
							st = st.substring(0, index)
							+ st.substring(index + 1);
					}
					// System.out.println("after replacing======= "+st);

				}

				out.print("[" + st + "]" + "\t" + numberFormat.format(num)
				+ "\t");
			}

			else
				out.print("[" + line + "]" + "\t" + numberFormat.format(num)
				+ "\t");
			char[] charst = line.toCharArray();
			i = 0;
			while (i < charst.length)
			{
				if (charst[i] == '\\' && charst[i + 1] == '\'')
				{
					out.print("<quote>" + " ");
					i++;
				} else if (charst[i] == '\\' && charst[i + 1] == '\"')
				{
					out.print("<dquote>" + " ");
					i++;
				} else
					out.print(charst[i] + " ");
				i++;

			}
		}

		out.println("@");
	}

}
