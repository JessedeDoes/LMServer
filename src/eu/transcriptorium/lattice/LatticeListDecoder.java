package eu.transcriptorium.lattice;
import java.util.*;

public class LatticeListDecoder 
{
	LatticeDecoder decoder;
	List<NodePathInfo[]> partialPaths = new ArrayList<NodePathInfo[]>();
	
	public LatticeListDecoder(LatticeDecoder d)
	{
		this.decoder = d;
	}
	
	public void decode(List<Lattice> lattices)
	{
		for (int i=0; i < lattices.size(); i++)
		{
			NodePathInfo[] partial;
			if (i==0)
				partial = decoder.mainDecodingLoop(lattices.get(0), 0);
			else
				partial = decoder.mainDecodingLoop(lattices.get(i), 0, partialPaths.get(i-1));
			partialPaths.add(partial);
		}
		// en dan weer de hele rompslomp om het beste pad er uit te peuteren...
		// zal best wel gedoe zijn enzo.... BOE
	}
}
