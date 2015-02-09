package eu.transcriptorium.lattice;
import java.io.Serializable;
import java.util.*;

public class Node  implements Serializable
{
	List<Arc> arcs = new ArrayList<Arc>();
	List<Arc> incomingArcs = new ArrayList<Arc>();
	String word = null;
	boolean lastInLine = false;
	int v  = 0; // pronunciation variant number
	String id=null;
	int topSortOrder=-1;
	final double nodeLanguageScore = Double.NaN;
	Object htkinfo = "Hallo";
	
	public boolean isFinal()
	{
		return 	(arcs == null ||arcs.size() ==0);
	}
	
	public String toString()
	{
		return "Node(id="  + id + ",W=" + word + ",arcs="  + arcs + ")\n"; 
	}
	
	public Node clone() // does NOT clone the node references in the arcs
	{
		return clone(false);
	}
	
	public Node clone(boolean connect) // does NOT clone the node references in the arcs
	{
		Node n = new Node();
		n.word = this.word;
		n.v = this.v;
		n.id = this.id;

		n.arcs = new ArrayList<Arc>();
		
		for (Arc a: this.arcs)
		{
			Arc a1 = a.clone();
			n.arcs.add(a1);
			if (connect)
			{
				Arc a2 = a.clone();
				Node n1 = a.destination;
				a2.source = n;
				n1.incomingArcs.add(a2);
			}
		}
		
		if (connect)
		{
			for (Arc a: this.incomingArcs)
			{
				Arc a1 = a.clone();
				n.incomingArcs.add(a1);
				
				Node n0 = a1.source;
				
				Arc a2 = a.clone();
				a2.source = n0;
				a2.destination = n;
				n0.arcs.add(a2);
			}
		}
		// should we not clone the incoming arcs as well...
		return n;
	}
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		Node o = (Node) other;
		return this.id.equals(o.id);
	}
	
	public static abstract class Test
	{
		public abstract boolean test(Node n);
	}
}
