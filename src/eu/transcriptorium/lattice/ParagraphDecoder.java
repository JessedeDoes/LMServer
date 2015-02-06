package eu.transcriptorium.lattice;

import java.io.*;
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
	NgramLanguageModel  lm  = null;
	VariantLexicon v = null;
	
	private double lmscale = 20;// was 20
	private double wdpenalty=0;
	private boolean setWeights = false;
	
	
	private  void decodeFilesInFolder(String dirname,
			NgramLanguageModel<String> lm, VariantLexicon v, PrintWriter out) 
	{
		Map<String, List<String>> regions = makeParagraphMap(dirname);
		decodePerParagraph(lm, v, out, regions); // this is the same as in listdecoder, dedouble!
	}

	public static Map<String, List<String>> makeParagraphMap(String dirname)
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
			//System.err.println("dir: "  + d);
			File[] entries = d.listFiles();
			Arrays.sort(entries);
			for (File subdir: entries)
			{
				//System.err.println("subdir: " + subdir);
				if (subdir.isDirectory())
				{
					String key = subdir.getName();
					File[] dentries = subdir.listFiles(fi);
					for (File f: dentries)
					{
						//System.err.println("entry: " + f);
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
		}
		return regions;
	}

	public  void decodePerParagraph(NgramLanguageModel<String> lm,
			VariantLexicon v, PrintWriter out, Map<String, List<String>> regions)
	{
		Set<String> keySet = regions.keySet();
		List<String> sortedKeys = new ArrayList<String>();
		sortedKeys.addAll(keySet);
		Collections.sort(sortedKeys);
		
		for (String r: sortedKeys)
		{
			out.println("<region>");
			List<String> lines = regions.get(r);
			{
				Collections.sort(lines);
				List<Lattice> lattices = new ArrayList<Lattice>();
				LatticeListDecoder decoder = new LatticeListDecoder(lm,v); // hm dit is niet zo handig?
				
				if (this.setWeights)
				{
					decoder.setLmscale(lmscale);
					decoder.setWdpenalty(wdpenalty);
				}
				
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
					out.println("<" + lines.get(k) +  "> " + sOut + " # " + fw + " # " + lw);
					k++;
				}
				out.println("</region>");
				//System.out.println("DECODE: " + lattices.size() + " " + );
			}
		}
	}

	public double getWdpenalty()
	{
		return wdpenalty;
	}


	public void setWdpenalty(double wdpenalty)
	{
		this.wdpenalty = wdpenalty;
		this.setWeights = true;
	}


	public double getLmscale()
	{
		return lmscale;
	}


	public void setLmscale(double lmscale)
	{
		this.lmscale = lmscale;
		this.setWeights = true;
	}
	
	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;
		String languageModel =  args[0];
		String dir = null;
		
		ParagraphDecoder p = new ParagraphDecoder();
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
		p.decodeFilesInFolder(dir,lm, v, new PrintWriter(System.out));
	}
}