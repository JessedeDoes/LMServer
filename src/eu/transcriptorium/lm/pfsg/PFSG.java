package eu.transcriptorium.lm.pfsg;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.BoundedList;
import eu.transcriptorium.lm.pfsg.PFSG.Transition;

// BTW what happens to OOV here??? back to startState ???

public class PFSG 
{
	Node startNode;
	Node endNode;
	Node backoffNode;
	
	List<Node> nodes = new ArrayList<Node>();
	
	transient NgramLanguageModel<String> lm = null;
	
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
	
	// ToDo unknown words...
	
	public Transition transition(Node n, String word)
	{
		Set<Transition> possible = new HashSet<Transition>();
		Transition toBO = null;
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1 != null && n1.output.equals(word))
				possible.add(t);	
			if (n1.equals(backoffNode))
				toBO = t;
		}
		if (possible.size() > 1)
			System.err.println("MULTIPLE TRANSITIONS POSSIBLE FOR: " + possible);
		if (possible.size() > 0)
			return possible.iterator().next();
		else if (n != backoffNode)
		{
			System.err.println("backoff to start node from "  + n  +  " on " + word); // but there should be a penalty for that ....sss
			List<String> W  = new ArrayList<String>();
			W.add(word);
			float p = lm.getLogProb(W);
			System.err.println("p0:" + p);
			if (toBO != null)
				System.err.println("p0 + bow:" + (p+toBO.p));
			// but also add transition from prev word to backoff node??
			Transition t0 =  transition(backoffNode, word);
			if (t0 != null && toBO != null)
			{
				Transition t1 = new Transition();
				t1.p = t0.p + toBO.p;
				t1.from = n.id;
				t1.to = t0.to;
				return t1;
			}
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

		public Transition() {
			// TODO Auto-generated constructor stub
		}

		public String toString()
		{
			return from + "  " + to  + " : " + p;
		}
	}
	
	public float evaluate(final List<String> words)
	{
		float p = 0;
		Node n = startNode;
		float ref = lm.scoreSentence(words);
		int lmOrder = lm.getLmOrder();
		
		final List<String> sentenceWithBounds =
				new BoundedList<String>(words, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
		
		for (int i=0; i < words.size(); i++)
		{
			String w = words.get(i);
			final List<String>  ngram = (i+1 < lmOrder-1)? 
					sentenceWithBounds.subList(-1, i+1): sentenceWithBounds.subList(i+1 - lmOrder, i+1);
					final float scoreNgram = lm.getLogProb(ngram);
			float pLM = lm.getLogProb(ngram);
			Transition t = transition(n,w);
			if (t != null)
			{
				Node n1 = nodes.get(t.to);
				
				
				
				n = n1;
				p += t.p;
				System.err.println("from <" + n + "> to <"  + n1 + ">, p: " + t.p + " pLM " + pLM);
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
		
		String s = "I MAY HARDLY BE CONSIDERATION A COMMON DOG";
		System.err.println(pfsg.evaluate(Arrays.asList(s.split("\\s+"))));
		
		//System.out.println(x.nodeNum);
		//System.out.println(x.transitions);
	}
}
