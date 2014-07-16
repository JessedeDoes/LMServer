package eu.transcriptorium.lattice;

import java.io.Serializable;
import java.util.*;

/**
 * HTK-like representation of word graphs.
 */
public class Lattice implements Serializable, Cloneable
{

	/**
	 * 
	 */

	public static String sentenceStartSymbol = "<s>";
	public static String sentenceEndSymbol = "</s>";
	public static String nullWordSymbol = "!NULL";

	private static final long serialVersionUID = 1L;
	Map<String,Node> nodes = new HashMap<String,Node>();
	List<Arc> arcs = new ArrayList<Arc>();

	Map<String, String> properties = new HashMap<String,String>();
	int N = 0; // number of nodes
	int L = 0; // number of arcs
	
	public double lmscale = 1.0;
	public double acscale = 1.0;
	public double wdpenalty = 0.0;

    Node startNode = null;
   
	public Node getStartNode()
	{
		 if (startNode != null)
			 return startNode;
		 else // unreachable arcs should be removed except for the start state....
		 {
			 this.addIncomingArcs();
			 for (Node n: getNodes())
				 if (n.incomingArcs.size() == 0)
				 {
					 this.startNode = n;
					 return n;
				 }
		 }
		return getNode("0"); // HM. THIS IS WRONG
	}

	public Node getNode(String id)
	{
		// TODO Auto-generated method stub
		return nodes.get(id);
	}

	public List<Node> getFinalNodes()
	{
		List<Node> F = new ArrayList<Node>();

		for (Node n: nodes.values())
		{
			if (n.arcs == null || n.arcs.size() ==0)
			{
				//System.err.println("Final: " + n);
				F.add(n);
			}
		}
		return F;
	}

	public Collection<Node> getNodes()
	{
		return this.nodes.values();
	}

	public int getSize()
	{
		return this.nodes.keySet().size(); // ahem not reliable
	}

	public static Lattice concatenate(Lattice l1, Lattice l2)
	{
		try
		{
			Lattice l1x = (Lattice) l1.clone();
			Lattice l2x = (Lattice) l2.clone();
			return concatenateNoClone(l1x,l2x);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public Lattice clone()
	{
		Lattice l = new Lattice();

		l.N = this.N;
		l.L = this.L;

		l.properties = new HashMap();
		l.properties.putAll(this.properties);

		for (Node n: this.getNodes())
		{
			Node n1 = n.clone(); 
			l.nodes.put(n1.id,n1);
		}

		// we need to redirect the node pointers to the cloned objects

		for (Node n: l.getNodes())
		{
			for (Arc a: n.arcs)
			{
				a.source = l.getNode(a.source.id);
				a.destination = l.getNode(a.destination.id);
			}
		}
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
		return l;
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

	public void rebuildArcList()
	{
		this.arcs.clear();
		int  J=0;
		for (Node n: this.getNodes())
		{
			for (Arc a: n.arcs)
			{
				a.id = J + "";
				arcs.add(a);
				J++;
			}
		}
	}

	public void addIncomingArcs()
	{
		for (Node n: getNodes())
			n.incomingArcs.clear();

		for (Node n: getNodes())
		{
			for (Arc a: n.arcs)
			{
				a.destination.incomingArcs.add(a);
			}
		}
	}

	public void addArc(Node s, Node d, double a, double l)
	{
		System.err.printf("Adding arc [%s:%s/%d]--[%s:%s/%d] a=%f l=%f\n",s.id,  s.word,s.v, d.id,  d.word,d.v,  a, l); 
		Arc arc = new Arc();
		s.arcs.add(arc);
		arc.source = s;
		arc.destination = d;
		arc.acoustic = a;
		arc.language = l;
	}


	public void findTransitionsAcrossSentenceStart(Node n,  List<Arc> addTo, double l, double a)
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

	public void removeLinebreaks()
	{
		addIncomingArcs();
		for (Node n: getNodes())
		{
			String w = n.word;
			if (w.equals(Lattice.sentenceEndSymbol))
			{
				List<Arc> arcsToNextLine = new ArrayList<Arc>();
				this.findTransitionsAcrossSentenceStart(n, arcsToNextLine, 0, 0);

				if (arcsToNextLine.size() > 0)
				{
					n.arcs = new ArrayList<Arc>(); 
				}

				// and lots of transitions need to be REMOVED....
				// and useless nodes as well

				for (Arc a0: n.incomingArcs)
				{
					Node n0 = a0.source;
					n0.lastInLine = true; // do we really have to rewrite the decoder in java ... kwilnie! ....
					
					double A0 = a0.acoustic;
					double L0 = a0.language;
					
					deleteTransition(n0,n);
					
					for (Arc a: arcsToNextLine)
					{
						double A = A0 + a.acoustic ;
						double L = L0 + a.language;
						this.addArc(a0.source, a.destination, A, L);
					}
				}
			}
		}
		removeUnreachableNodes();
		this.rebuildArcList();
	}

	private void deleteTransition(Node source, Node destination)
	{
		List<Arc> keep = new ArrayList<Arc>();
		for (Arc a: source.arcs)
		{
			if (a.destination != destination)
				keep.add(a);
		}
		source.arcs = keep;
	}
	
	/**
	 * 
	 */
	void removeUnreachableNodes()
	{
		System.err.println("Nodes now " +this.getNodes().size());
		Set<String> reachable = new HashSet<String>();
		checkReachable(reachable, this.getStartNode());
		Set<String> allNodeIds = new HashSet<String>();
		allNodeIds.addAll(this.nodes.keySet());
		for (String s: allNodeIds )
		{
			if (!reachable.contains(s))
			{
				System.err.println("Removing useless node " + nodes.get(s));
				nodes.remove(s);
			}
		}
		System.err.println("Nodes after pruning " +this.getNodes().size());
	}
	
	void checkReachable(Set<String> reachable, Node n)
	{
		if (reachable.contains(n.id))
			return;
		reachable.add(n.id);
		for (Arc a: n.arcs)
			checkReachable(reachable, a.destination);
	}
	
	// SRILM uses the NEW scales to compute the arc weight
	void resetArcWeights(double acscale, double lmscale, double wdpenalty)
	{
		for (Node n: getNodes())
			for (Arc a: n.arcs)
				a.setWeight(acscale, lmscale, wdpenalty);
		this.addIncomingArcs(); // should I?
	}
}
