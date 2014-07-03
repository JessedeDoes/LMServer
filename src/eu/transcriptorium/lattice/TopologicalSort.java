package eu.transcriptorium.lattice;

import java.util.*;

public class TopologicalSort
{
	int numVisited = 0;
	Set<Node> tempMark = new HashSet<Node>();
	Set<Node> defMark = new HashSet<Node>();
	LinkedList<Node> sortedList = new LinkedList<Node>();

	class LatticeException extends Exception
	{
		public LatticeException(String w)
		{
			super(w);
		}
	};
	
	void visit(Node node) throws LatticeException
	{
		if (tempMark.contains(node))
		{
			System.err.println("Error: loop in lattice, cannot sort nodes");
			throw new LatticeException("Der zit een loop in je lattice!");
		}
		if (defMark.contains(node))
			return;
		tempMark.add(node);
		
		for (Arc a: node.arcs)
			visit(a.destination);

		defMark.add(node);
		tempMark.remove(node);

		sortedList.addFirst(node);
	}

	public List<Node> sortNodes(Lattice l,  boolean reversed) throws LatticeException
	{
		tempMark.clear();
		defMark.clear();
		sortedList.clear();

		visit(l.getStartNode());

		if (reversed)  // reverse the node order from the way we generated it
			 Collections.reverse(sortedList);

		for (int i=0; i < sortedList.size(); i++)
		{
			sortedList.get(i).topSortOrder = i;
		}
		
		ArrayList<Node> a = new ArrayList<Node>();
		a.addAll(sortedList);
		// mark the sort order in the lattice nodes
	
		return a;
	}
}
