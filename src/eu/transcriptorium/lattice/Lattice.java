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
	public static String interLatticeGlue = "PLAK!PLAK!PLAK!";
	
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
		l.getStartNode();
		return l;
	}

	

	// this keeps the </s> <s> sequence between l1 final and l2 initial

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


	void deleteTransition(Node source, Node destination)
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
	
	void renumberNodes()
	{
		int i = 0;
		Map<String,Node> newNodeMap  = new HashMap<String,Node>();
		for (Node n: getNodes())
		{
			n.id = i + "";
			newNodeMap.put(n.id, n);
			i++;
		}
		this.nodes = newNodeMap;
		this.rebuildArcList();
	}
}
