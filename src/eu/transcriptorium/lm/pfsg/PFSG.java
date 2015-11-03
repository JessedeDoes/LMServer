package eu.transcriptorium.lm.pfsg;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import eu.transcriptorium.lm.pfsg.PFSG.Transition;

// BTW what happens to OOV here??? back to startState ???

public class PFSG 
{
	Node startNode;
	Node endNode;
	Node backoffNode;
	
	List<Node> nodes = new ArrayList<Node>();
	
	transient NgramLanguageModel lm = null;
	
	static String bo_name = "__BACKOFF__";
	static String start_bo_name = bo_name + " __FROM_START__";
	static String nullWord = "!NULL";
	static String end_tag  = "</s>";
	static String start_tag = "<s>";
	
	float unknownPenalty = Float.NEGATIVE_INFINITY; // ahem...
	
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
		if (!output.equals(fullName)) System.err.println(fullName);
		Node n = new Node(i,output);
		nodes.add(n);
		n.fullName = fullName; // for debugging purposes..
		
		if (fullName.equals(start_tag))
			this.startNode = n;
		if (fullName.equals(end_tag))
			this.endNode = n;
		if (output.equals(bo_name))
			this.backoffNode = n;
		if (nodes.get(i) != n)
		{
			System.err.println("Tellingen niet in de haak....!");
			//System.exit(1);
		}
	}
	
	
	public Transition transition(Node n, String word)
	{
		Set<Transition> possible = new HashSet<Transition>();
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1 != null && n1.output.equals(word))
				possible.add(t);	
		}
		if (possible.size() > 1)
			System.err.println("MULTIPLE TRANSITIONS POSSIBLE FOR: " + possible);
		if (possible.size() > 0)
			return possible.iterator().next();
		else if (n != backoffNode)
		{
			System.err.println("backoff to start node..."); // but there should be a penalty for that ....sss
			// List<String> W = Arrays.asList(word.split(" "));
			// System.err.println("HM?" + lm.scoreSentence(W));
			// but also add transition from prev word to backoff node??
			return transition(backoffNode, word);
		} 
		return null;
	}
	
	public Transition transitionToEndNode(Node n)
	{
		Set<Transition> possible = new HashSet<Transition>();
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1 != null && n1 == endNode)
				possible.add(t);	
		}
		if (possible.size() > 1)
			System.err.println("MULTIPLE TRANSITIONS POSSIBLE FOR: " + possible);
		if (possible.size() > 0)
			return possible.iterator().next();
		else if (n != startNode)
		{
			return transitionToEndNode(startNode);
		} 
		return null;
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
		float ref = lm.scoreSentence(words);
		for (String w: words)
		{
			Transition t = transition(n,w);
			if (t != null)
			{
				Node n1 = nodes.get(t.to);
				
				System.err.println("from " + n + " to "  + n1);
				
				n = n1;
				p += t.p;
			} else // reset to start state???? // how to backoff ?? just set n to start state again??
			{
				System.err.println("No transition from " + n + " on  " + w);
			}
		}
		Transition tLast = transitionToEndNode(n);
		p += tLast.p;
		System.err.println("ref = " + ref + ", p=" + p + ", diff="  + (ref-p));
		return p;
	}


	public void addTransition(Transition t) 
	{
		Node fromNode = nodes.get(t.from);
		fromNode.transitions.add(t);
	}
	
	public static void main(String[] args)
	{
		LM2PFSG x = new LM2PFSG();
		String arg0;
		if (args.length == 0)
			arg0 ="./Test/languageModel.lm";
		else
			arg0 = args[0];
		PFSG pfsg = x.build(arg0);
		System.err.println(pfsg.backoffNode);
		System.err.println(pfsg.evaluate(Arrays.asList("GOOD IDEA OF".split("\\s+"))));
		
		//System.out.println(x.nodeNum);
		//System.out.println(x.transitions);
	}
}
