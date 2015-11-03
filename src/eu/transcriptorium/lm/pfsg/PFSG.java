package eu.transcriptorium.lm.pfsg;
import java.util.*;

import eu.transcriptorium.lm.pfsg.PFSG.Transition;

// BTW what happens to OOV here??? back to startState ???

public class PFSG 
{
	Node startNode;
	Node endNode;
	
	List<Node> nodes = new ArrayList<Node>();
	
	static String bo_name = "__BACKOFF__";
	static String start_bo_name = bo_name + " __FROM_START__";
	static String nullWord = "!NULL";
	static String end_tag  = "</s>";
	static String start_tag = "<s>";
	
	static class Node
	{
		String output;
		String fullName;
		int id;
		List<Transition> transitions = new ArrayList<Transition>();
		
		public Node(int i, String output)
		{
			this.id = i;
			this.output = output;
		}
		
		public String toString()
		{
			return id + ": "  + output + " ("  + fullName + ")";
		}
	}

	public void addNode(int i, String output, String fullName)
	{	
		Node n = new Node(i,output);
		nodes.add(n);
		n.fullName = fullName; // for debugging purposes..
		
		if (fullName.equals(start_tag))
			this.startNode = n;
		if (fullName.equals(end_tag))
			this.endNode = n;
		
		if (nodes.get(i) != n)
		{
			System.err.println("Tellingen niet in de haak....!");
			//System.exit(1);
		}
	}
	
	
	public Node transition(Node n, String word)
	{
		Set<Node> possible = new HashSet<Node>();
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1 != null && n1.output.equals(word))
				possible.add(n1);
				
		}
		if (possible.size() > 1)
			System.err.println(possible);
		return possible.iterator().next();
	}
	
	public static class Transition
	{
		int from;
		int to;
		float p;

		public Transition(int from, int to, float p)
		{
			this.from = from;
			this.to = to;
			this.p = p;
		}

		public String toString()
		{
			return from + "  " + to  + " : " + p;
		}
	}
	
	public float evaluate(List<String> words)
	{
		float p = 0;
		Node n = startNode;
		
		return p;
	}


	public void addTransition(Transition t) 
	{
		Node fromNode = nodes.get(t.from);
		fromNode.transitions.add(t);
	}
}
