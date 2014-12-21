package eu.transcriptorium.lattice;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lm.VariantLexicon;
import eu.transcriptorium.util.StringUtils;

/**
 * Attempt to solve the absence of context at line breaks for language modeling. 
 * Instead of concatenating word graphs for a sequence of lines in a paragraph, 
 * we initialize a separate decoder for each line, with initial paths joined to the final path list of the previous
 * line.  
 * <p>
 * Results are not very encouraging as yet.<br/>
 * In the Bentham test set (860 lines) there are 283 errors in the first word. 
 * For 156, the correct word is a hypothesis in the word graph.<br/>
 * This includes many uninteresting cases not at all related to LM, such as " at the start of a line, which is 
 * difficult for the HTR to pick up correctly.
 * <br/>
 * Whe we disregard punctuation, this becomes 141 errors and 46 promising hypotheses, which means that a better 
 * LM can improve the WER on real words by at most 46/7868 = 0.6%. 
 * @author jesse
 */

public class LatticeListDecoder 
{
	//LatticeDecoder decoder;
	
	List<LatticeDecoder> decoders = new ArrayList<LatticeDecoder>();
	
	//List<NodePathInfo[]> partialPaths = new ArrayList<NodePathInfo[]>();
	//List<Integer> partialPathLengths = new ArrayList<Integer>();

	List<NodePathInfo> endPoints = new ArrayList<NodePathInfo>();
	NgramLanguageModel lm;
	VariantLexicon variantLexicon;
	
	static class isRealWord extends Node.Test
	{

		@Override
		public boolean test(Node n) {
			// TODO Auto-generated method stub
			return !(n.word.equals(Lattice.nullWordSymbol) 
					|| Lattice.isSentenceDelimiter(n.word));
		}
	}

	public LatticeListDecoder(NgramLanguageModel lm, VariantLexicon v) // nee dus, je moet er meerdere hebben
	{
		//this.decoder = d;
		this.lm = lm;
		this.variantLexicon = v;
	}
	
	
	public List<String> decode(List<Lattice> lattices)
	{
		List<String> decodingResult = null;
		for (int i=0; i < lattices.size(); i++)
		{
			LatticeDecoder decoder = new LatticeDecoder();
			decoders.add(decoder);
			decoder.setLanguageModel(lm);
			decoder.setVariantLexicon(variantLexicon);
			decoder.setIgnoreSentenceBoundaries(true);
			
			if (i==0)
			{
				decodingResult = decoder.decode(lattices.get(0));
				endPoints.add(decoder.getLastPathInfo());
			}
			else
			{
				NodePathInfo prev = decoders.get(i-1).getLastPathInfo();
				decoder.setPreviousNodeinfo(prev);
				//System.err.println("Hi there: " + prev);
				decodingResult = decoder.decode(lattices.get(i));
				endPoints.add(decoder.getLastPathInfo());
			}
		}
		return decodingResult;
		// now use the last decoder to get out everything (in fact, each step traces back to the first?)
		// en dan weer de hele rompslomp om het beste pad er uit te peuteren...
		// zal best wel gedoe zijn enzo.... BOE BOE
	}
	
	
	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;

		String languageModel =  "data/trigramModel.lm.bin";
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
		if (args.length > 1)
		{
			v = new VariantLexicon();
			v.loadFromFile(args[1]);
		}
		String dir = "./Lattices";
		if (args.length == 0)
		{
			//for (int i=0; i < 10; i++)
			//decodeLatticeFile("resources/exampleData/115_070_002_02_18.lattice", lm, v);
		}
		
		else
		{
			dir = args[0];
		};
		decodeFilesInFolder(dir,lm, v);
	}

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
			for (File f: entries)
			{
				String key = f.getName();
				key = key.replaceAll("_[0-9]+.lattice", "" );
				List<String> l = regions.get(key);
				
				if (l == null)
				{
					System.err.println(key);
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
		
		Set<String> keySet = regions.keySet();
		List<String> sortedKeys = new ArrayList<String>();
		sortedKeys.addAll(keySet);
		Collections.sort(sortedKeys);
		for (String r: sortedKeys)
		{
			System.out.println("<region>");
			List<String> lines = regions.get(r);
			Collections.sort(lines);
			List<Lattice> lattices = new ArrayList<Lattice>();
			LatticeListDecoder decoder = new LatticeListDecoder(lm,v);
			for (String l: lines)
			{
			   lattices.add(StandardLatticeFile.readLatticeFromFile(l));
			}
			List<String> result = decoder.decode(lattices);
			List<List<String>> sentences = splitList(result);
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
	
	public static List<List<String>> splitList(List<String> sentences)
	{
		List<List<String>> r = new ArrayList<List<String>>();
		List<String> currentSentence = new ArrayList<String>();
		r.add(currentSentence);
		int k=0;
		for (String s: sentences)
		{
			if (s.equals(Lattice.sentenceStartSymbol))
			{
				if (k > 0)
				{
					currentSentence = new ArrayList<String>();
					r.add(currentSentence);
				}
				k++;
			}
			currentSentence.add(s);
		}
		return r;
	}
}
