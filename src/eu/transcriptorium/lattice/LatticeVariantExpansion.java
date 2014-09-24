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
				// System.err.println(nw + "(" + n.v  + ") ->" + v.variantForm);
				// n.word = v.variantForm; // nee dit niet doen, alleen de probs! anders werkt LM niet!

				if (adaptProbabilities)
					for (Arc a: n.incomingArcs)
					{
						a.acoustic += Math.log(v.probability); // of log2?
					}
			}
		}
		//StandardLatticeFile.printLattice(System.err, lattice);
	}
}
