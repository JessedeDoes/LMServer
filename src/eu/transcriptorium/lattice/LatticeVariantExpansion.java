package eu.transcriptorium.lattice;
import eu.transcriptorium.lm.VariantLexicon;
import eu.transcriptorium.lm.VariantLexicon.Variant;
public class LatticeVariantExpansion
{
	static void expand(Lattice lattice, VariantLexicon lex, boolean adaptProbabilities)
	{
		for (Node n: lattice.getNodes())
		{
			String nw = n.word;
			Variant v = lex.getVariantFromNormalForm(nw, n.v-1);
			if (v != null)
			{
				//System.err.println(nw + "(" + n.v  + ") ->" + v.variantForm);
				// n.word = v.variantForm; // nee dit niet doen, alleen de probs! anders werkt LM niet!

				if (adaptProbabilities)
				{
					
					for (Arc a: n.incomingArcs) // zijn die er all......
					{
						//System.err.println("applying alejandro weights... before: "  + a.acoustic + " :  " + v.probability);
						a.acoustic += Math.log(v.probability); // of log2?
						//System.err.println("applying alejandro weights... after: "  + a.acoustic);
					}
				}
			} else if (!nw.equals(Lattice.nullWordSymbol))
			{
				System.err.println("Could not get variants for "  + nw);
			}
		}
		//StandardLatticeFile.printLattice(System.err, lattice);
	}
}
