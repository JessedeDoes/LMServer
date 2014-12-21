package eu.transcriptorium.lattice;
import java.util.*;

public class LatticeListDecoder 
{
	//LatticeDecoder decoder;
	List<LatticeDecoder> decoders = new ArrayList<LatticeDecoder>();
	//List<NodePathInfo[]> partialPaths = new ArrayList<NodePathInfo[]>();
	//List<Integer> partialPathLengths = new ArrayList<Integer>();

	List<NodePathInfo> endPoints = new ArrayList<NodePathInfo>();
	
	public LatticeListDecoder(LatticeDecoder d) // nee dus, je moet er meerdere hebben
	{
		//this.decoder = d;
	}

	public void decode(List<Lattice> lattices)
	{
		for (int i=0; i < lattices.size(); i++)
		{
			LatticeDecoder decoder = new LatticeDecoder();
			decoders.add(decoder);
			if (i==0)
			{
				decoder.decode(lattices.get(0));
				endPoints.add(decoder.getLastPathInfo());
			}
			else
			{
				NodePathInfo prev = decoder.getLastPathInfo();
				decoder.setPreviousNodeinfo(prev);
				decoder.decode(lattices.get(i));
				endPoints.add(decoder.getLastPathInfo());
			}
		}
		// en dan weer de hele rompslomp om het beste pad er uit te peuteren...
		// zal best wel gedoe zijn enzo.... BOE BOE
	}
}
