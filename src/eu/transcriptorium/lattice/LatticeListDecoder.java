package eu.transcriptorium.lattice;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import eu.transcriptorium.lm.VariantLexicon;

public class LatticeListDecoder 
{
	//LatticeDecoder decoder;
	List<LatticeDecoder> decoders = new ArrayList<LatticeDecoder>();
	//List<NodePathInfo[]> partialPaths = new ArrayList<NodePathInfo[]>();
	//List<Integer> partialPathLengths = new ArrayList<Integer>();

	List<NodePathInfo> endPoints = new ArrayList<NodePathInfo>();
	NgramLanguageModel lm;
	VariantLexicon variantLexicon;
	
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
			
			if (i==0)
			{
				decodingResult = decoder.decode(lattices.get(0));
				endPoints.add(decoder.getLastPathInfo());
			}
			else
			{
				NodePathInfo prev = decoder.getLastPathInfo();
				decoder.setPreviousNodeinfo(prev);
				decodingResult = decoder.decode(lattices.get(i));
				endPoints.add(decoder.getLastPathInfo());
			}
		}
		return decodingResult;
		// now use the last decoder to get out everything (in fact, each step traces back to the first?)
		// en dan weer de hele rompslomp om het beste pad er uit te peuteren...
		// zal best wel gedoe zijn enzo.... BOE BOE
	}
}
