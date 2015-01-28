package eu.transcriptorium.lattice;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lattice.LatticeListDecoder.isRealWord;
import eu.transcriptorium.lm.VariantLexicon;
import eu.transcriptorium.util.StringUtils;

public class ParagraphDecoder
{
	private static void decodeFilesInFolder(String dirname,
			NgramLanguageModel<String> lm, VariantLexicon v) 
	{
		File d = new File(dirname);
		Map<String, List<String>> regions = new HashMap<String, List<String>> ();


		FilenameFilter fi = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".lattice");
			}
		};

		if (d.isDirectory())
		{
			File[] entries = d.listFiles(fi);
			Arrays.sort(entries);
			for (File subdir: entries)
			{
				if (subdir.isDirectory())
				{
					String key = subdir.getName();
					File[] dentries = subdir.listFiles(fi);
					for (File f: dentries)
					{
						List<String> l = regions.get(key);

						if (l == null)
						{
							l = new ArrayList<String>();
							regions.put(key, l);
						}
						try
						{
							l.add(f.getCanonicalPath());
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			Set<String> keySet = regions.keySet();
			List<String> sortedKeys = new ArrayList<String>();
			sortedKeys.addAll(keySet);
			Collections.sort(sortedKeys);
			for (String r: sortedKeys)
			{
				System.out.println("<region>");
				List<String> lines = regions.get(r);
				{
					Collections.sort(lines);
					List<Lattice> lattices = new ArrayList<Lattice>();
					LatticeListDecoder decoder = new LatticeListDecoder(lm,v);
					for (String l: lines)
					{
						lattices.add(StandardLatticeFile.readLatticeFromFile(l));
					}
					List<String> result = decoder.decode(lattices);
					List<List<String>> sentences = LatticeListDecoder.splitList(result);
					int k=0;
					for (List<String> s: sentences)
					{
						String sOut = StringUtils.join(s, " ");
						Lattice l_k = lattices.get(k);
						Set<String> fw = l_k.getFirstWords(new isRealWord());
						Set<String> lw = l_k.getLastWords(new isRealWord());
						//System.out.println(lines.get(k) + "/" + k + ":" + sOut);
						System.out.println("<" + lines.get(k) +  "> " + sOut + " # " + fw + " # " + lw);
						k++;
					}
					System.out.println("</region>");
					//System.out.println("DECODE: " + lattices.size() + " " + );
				}
			}
		}

	}

	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;
		String languageModel =  args[0];
		String dir = null;
		// languageModel = null;
		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}
		VariantLexicon v = null;
		if (args.length > 2)
		{
			v = new VariantLexicon();
			v.loadFromFile(args[1]);
			dir = args[2];
		} 	else
		{
			dir = args[1];
		};
		decodeFilesInFolder(dir,lm, v);
	}
}