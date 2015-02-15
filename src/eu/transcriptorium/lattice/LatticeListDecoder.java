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
import eu.transcriptorium.hyphen.HyphenDictionaryFromLM;
import eu.transcriptorium.lm.VariantLexicon;
import eu.transcriptorium.util.StringUtils;

/**
 * This is an attempt to compensate partially the absence of part of the context at line breaks for language modeling. 
 * Instead of concatenating word graphs for a sequence of lines in a paragraph, 
 * we initialize a separate decoder for each line.<br/>
 * Before outputting the one-best hypothesis, each decoder produces, as usual in Viterbi decoding, 
 * an intermediate object consisting of a set of hypotheses per position with backward links to previous positions. 
 * We can connect the intermediate objects between lines,  by joining (before running decoder N), 
 * the initial points of paths of decoder N,  to the final path list of decoder N-1 of the previous
 * line.  Then, after running all decoders, 
 * we simply follow the backward links from the last position of the last line to get the 1-best paragraph decoding.
 * <p>
 * Results are not very encouraging as yet - an improvement of the WER by 0.5% on the Bentham competition set. 
 * Maybe this is partly just because we have not found the right data to work with:<br/>
 * 
 * Our best line-level trigram model gives an error rate of 17.17% (case sensitive) on the Bentham competition set.
   The paragraph-level decoder gives 16.65%.
   <br/>
 * There are 267 errors in the first word in the best line-by-line trigram decoder we have (and 186 in the last). 
 * For only 72 of them, the correct "word" (or punctuation symbol) is a hypothesis in the word graph.<br/>
 * This gives an upper bound for the improvement we can obtain from the first word, of about 0.91%.
 * After applying the paragraph-based decoder, we get an error rate of 16.65 (improvement by 0.5%).
 * Of this improvement (about 37 corrected errors), 7 errors were corrected in the first word, 16 were corrected in the last word.
 * <br/>
  
 * @author jesse
 */

public class LatticeListDecoder 
{
	//LatticeDecoder decoder;
	
	private double lmscale = 20;// was 20

	private double wdpenalty=0;
	
	private boolean setWeights = false;
	
	private boolean handleHyphenations = false;
	
	List<LatticeDecoder> decoders = new ArrayList<LatticeDecoder>();
	
	//List<NodePathInfo[]> partialPaths = new ArrayList<NodePathInfo[]>();
	//List<Integer> partialPathLengths = new ArrayList<Integer>();

	List<NodePathInfo> endPoints = new ArrayList<NodePathInfo>();
	NgramLanguageModel lm;
	VariantLexicon variantLexicon;

	private boolean expandVariants=true;

	private boolean useAlejandroProbabilities=true;
	
	public static class isRealWord extends Node.Test
	{

		@Override
		public boolean test(Node n) 
		{
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
	
	
	public List<String> decode(List<Lattice> lattices) // probleem: gewichten moeten al bekend zijn bij inlezen??
	{
		List<String> decodingResult = null;
		
		if (handleHyphenations)
		{
			for (int i=0; i < lattices.size()-1; i++)
			{
				LatticeConcatenate.addHyphenHypotheses(lattices.get(i), lattices.get(i+1), "-$", new HyphenDictionaryFromLM(this.lm));
			}
		}
		
		for (int i=0; i < lattices.size(); i++)
		{
			LatticeDecoder decoder = new LatticeDecoder();
			if (this.setWeights)
			{
				decoder.setLmscale(this.lmscale);
				decoder.setWordInsertionPenalty(this.wdpenalty);
			}
			decoders.add(decoder);
			decoder.setLanguageModel(lm);
			decoder.setVariantLexicon(variantLexicon);
			decoder.setIgnoreSentenceBoundaries(true);
			
			if (expandVariants && this.variantLexicon != null)
			{
				LatticeVariantExpansion.expand(lattices.get(i), decoder.variantLexicon, useAlejandroProbabilities);
			}
			
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
				endPoints.add(decoder.getLastPathInfo()); // is this used in any way?
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

		String languageModel =  "data/trigramModel.lm.bin"; // niet mee eens....
		String lex = null;
		String latticeDir = null;
		
		languageModel = args[0];
		
		lex = args[1];
		
		latticeDir = args[2];
		
		if (languageModel.equals("-"))
		{
			languageModel = "data/trigramModel.lm.bin";
		}
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
		if (!(lex.equals("-")))
		{
			v = new VariantLexicon();
			v.loadFromFile(lex);
		}

		decodeFilesInFolder(latticeDir,lm, v);
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
				System.err.println(key + " : " + f.getName());
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
}
