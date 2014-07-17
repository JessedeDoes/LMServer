package eu.transcriptorium.lattice;

import java.util.ArrayList;
import java.util.List;

public class LatticeConcatenate
{

	public static Lattice concatenate(Lattice l1, Lattice l2)
	{
		try
		{
			Lattice l1x = (Lattice) l1.clone();
			Lattice l2x = (Lattice) l2.clone();
			Lattice l =  concatenateNoClone(l1x,l2x);
			l.renumberNodes();
			return l;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Lattice concatenate(List<Lattice> list)
	{
		List<Lattice> clones = new ArrayList<Lattice>();
		for (Lattice l: list)
			clones.add(l.clone());
		return concatenateNoClone(clones);
	}

	// this keeps the </s> <s> sequence between l1 final and l2 initial
	
	private static Lattice concatenateNoClone(Lattice l1, Lattice l2)
	{
		Lattice l = l1;
	
		for (Node n: l1.getFinalNodes())
		{
			Arc a = new Arc();
			a.source = n;
			a.destination = l2.getStartNode();
			//System.err.println("Add arc from " + n +   " to " + a.destination);
			a.language = 0.0;
			a.acoustic = 0.0;
			n.arcs.add(a);
		}
	
		for (Node n: l2.getNodes())
		{
			int newId = l1.getSize() + Integer.parseInt(n.id);
			n.id = newId + "";
			l.nodes.put(n.id, n);
		}
	
		l.N += l2.getSize();
		l.L += l2.L;
		l.rebuildArcList();
	
		l.properties.put("UTTERANCE",
				l1.properties.get("UTTERANCE") + ";" + 
						l2.properties.get("UTTERANCE"));
	
	
		return l;
	}

	/**
	 * Destroys first entry in list!
	 */
	
	private static Lattice concatenateNoClone(List<Lattice> list)
	{
		Lattice l = list.get(0);
		for (Lattice l1: list)
		{
			if (l1 == l)
				continue;
			concatenateNoClone(l, l1);
		}
		l.renumberNodes();
		return l;
	}

	public static void findTransitionsAcrossSentenceStart(Node n,  List<Arc> addTo, double l, double a)
	{
		if (n.word.equals(Lattice.sentenceStartSymbol))
		{
			for (Arc arc: n.arcs)
			{
				Arc a1 = new Arc();
				a1.destination = arc.destination;
				a1.language = arc.language + l;
				a1.acoustic = arc.acoustic + a;
				addTo.add(a1);
			}
		} else
			for (Arc arc: n.arcs)
			{
				String w = arc.destination.word;
				if (w.equals(Lattice.nullWordSymbol))
					findTransitionsAcrossSentenceStart(arc.destination, addTo, arc.language+l, arc.acoustic+a);
				else if (w.equals(Lattice.sentenceStartSymbol))
					findTransitionsAcrossSentenceStart(arc.destination, addTo, arc.language+l, arc.acoustic+a);
			}
	}

	/**
	 * n0           n                n1           n2         n3
	 * W1 -- &lt;/s> -- !NULL -- &lt;s> -- W2
	 *       a0               a                a1            a2
	 */
	
	public static void removeLinebreaks(Lattice l)
	{
		l.addIncomingArcs();
		for (Node n: l.getNodes())
		{
			String w = n.word;
			if (w.equals(Lattice.sentenceEndSymbol)) // here we need to store some info about the nodes becoming sentence-final
			{
				List<Arc> arcsToNextLine = new ArrayList<Arc>();
				findTransitionsAcrossSentenceStart(n, arcsToNextLine, 0, 0);
	
				if (arcsToNextLine.size() > 0)
				{
					n.arcs = new ArrayList<Arc>(); 
				}
	
				// and lots of transitions need to be REMOVED....
				// and useless nodes as well
	
				for (Arc a0: n.incomingArcs)
				{
					a0.sentenceFinal = true;
					Node n0 = a0.source;
					n0.lastInLine = true; // do we really have to rewrite the decoder in java ... kwilnie! ....
					
					double A0 = a0.acoustic;
					double L0 = a0.language;
					
					l.deleteTransition(n0,n);
					
					for (Arc a: arcsToNextLine)
					{
						double A = A0 + a.acoustic ;
						double L = L0 + a.language;
						l.addArc(a0.source, a.destination, A, L);
					}
				}
			}
		}
		l.removeUnreachableNodes();
		l.rebuildArcList();
	}

}
