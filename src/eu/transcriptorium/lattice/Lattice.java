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
	
	public Node getStartNode()
	{
		return getNode("0"); // HM.
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
		return this.N;
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
			a.l = 0.0;
			a.a = 0.0;
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
		
		l.removeLinebreaks(); // AHEM?
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
			for (Arc a: n.arcs)
			{
				a.destination.incomingArcs.add(a);
			}
	}
	
	public void addArc(Node s, Node d, double a, double l)
	{
	    System.err.printf("Adding arc [%s:%s/%d]--[%s:%s/%d] a=%f l=%f\n",s.id,  s.word,s.v, d.id,  d.word,d.v,  a, l); 
		Arc arc = new Arc();
		s.arcs.add(arc);
		arc.source = s;
		arc.destination = d;
		arc.a = a;
		arc.l = l;
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
				for (Arc a: n.arcs)
				{
					Node n1 = a.destination;
					if (n1.word.equals(Lattice.sentenceStartSymbol)) // does not happen
					{
						
					} else if (n1.word.equals(Lattice.nullWordSymbol))
					{
						for (Arc a1: n1.arcs)
						{
							Node n2 = a1.destination;
							if (n2.word.equals(Lattice.sentenceStartSymbol))
							{
								for (Arc a2: n2.arcs)
								{
									// 
									Node n3 = a2.destination;
									for (Arc a0: n.incomingArcs)
									{
										double A = a0.a + a.a + a1.a + a2.a;
										double L = a0.l + a.l + a1.l + a2.l;
										addArc(a0.source, n3,  A, L);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
