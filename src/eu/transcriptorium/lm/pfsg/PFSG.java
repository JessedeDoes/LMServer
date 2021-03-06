package eu.transcriptorium.lm.pfsg;
import java.io.IOException;
import java.io.Serializable;
import eu.transcriptorium.util.*;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.BoundedList;
import eu.transcriptorium.lm.pfsg.PFSG.Transition;

/*
 * Probabilistic Finite State Grammar
 * Based on SRILM translation code
 * 
 * Purpose: use in Lattice Decoder for better decoding speed
 */

public class PFSG implements Serializable
{
	Node startNode;
	Node endNode;
	Node backoffNode;

	boolean checkWithLM = false;
	List<Node> nodes = new ArrayList<Node>();

	transient NgramLanguageModel<String> lm = null;

	static String bo_name = "__BACKOFF__";
	static String start_bo_name = bo_name + " __FROM_START__";
	static String nullWord = "!NULL";
	static String end_tag  = "</s>";
	static String start_tag = "<s>";

	static String stellingWerf_3 = "/home/jesse/TUTORIAL-HTR/EXP-RESOLUTIONS/TRAIN/LM/STELLINGWERF_3/languageModel.lm";
	float unknownPenalty = (float) Math.log(1e-8); // Float.NEGATIVE_INFINITY; // ahem...

	static class Node implements Serializable
	{
		String output;
		String fullName;
		int id;
		List<Transition> transitions = new ArrayList<Transition>();
		Map<String, Transition> transitionMap = null;
		List<Transition> nullTransitions = null;
		
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
		//if (!output.equals(fullName)) System.err.println(fullName);
		Node n = new Node(i,output);
		nodes.add(n);
		n.fullName = fullName; // for debugging purposes..

		if (fullName.equals(start_tag))
			this.startNode = n;
		if (fullName.equals(end_tag))
			this.endNode = n;
		if (fullName.equals(bo_name + " "))
			this.backoffNode = n;
		
		if (nodes.get(i) != n)
		{
			System.err.println("Tellingen niet in de haak....!");
			System.exit(1);
		}
	}

	// ToDo unknown words...
	// ToDo check null transitions, possibly recursively (??)
	
	public Transition plainTransition(Node n, String word)
	{
		if (n.transitionMap != null)
			return n.transitionMap.get(word);
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1 != null && n1.output.equals(word))
				return t;	
		}
		return null;
	}
	
	public Transition transition(Node n, String word)
	{
		
		Transition t0 = plainTransition(n,word);
		if (t0 != null)
			return t0;
		
		
		for (Transition t: n.nullTransitions)
		{
			Node dest = nodes.get(t.to);
			Transition t1 = plainTransition(dest, word); // TODO hierbinnen hoef je geen null trans meer te checken
			if (t1 != null)
			{
				// System.err.println("Ha: via de nul:" + t1);
				Transition t2 = new Transition();
				t2.p = t.p + t1.p;
				t2.to = t1.to;
				t2.from = n.id;
				return t2;
			}
		}
		
		// OOV situation....
		// it is a bit strange this does not depend on 
		// the backoff weight from n
		
		Transition t = new Transition();
		t.from = n.id;
		t.to = backoffNode.id;
		t.p = unknownPenalty;
		System.err.println("returning on OOV " + word + ": "+ t + " from " + n);
		return t;
	}

	
	// hier ook via NULL zoeken....
	
	public Transition transitionToEndNode(Node n)
	{
		for (Transition t: n.nullTransitions)
		{
			Node dest = nodes.get(t.to);
			if (dest == endNode)
				return t;
		}
		return null;	
		// this is silly -- transition to end nod IS a null transition
	}

	public static class Transition  implements Serializable
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
			return "{" + from + " -> " + to  + " : " + p + "}";
		}
		
		@Override
		public int hashCode()
		{
			int h = 23;
			h = 31 * h + to;
			h = 31 * h + from;
			return h;
		}
		
		@Override
		public boolean equals(Object o)
		{
			try
			{
				Transition o1 = (Transition) o;
				return (o1.from == from && o1.to == to); 
			} catch (Exception e) { return false;} 
		}
	}


	public void addTransition(Transition t) 
	{
		Node fromNode = nodes.get(t.from);
		fromNode.transitions.add(t);
	}

	// there might be loops?! beware....
	
	public List<Transition> collectNullTransitions(Node n)
	{
		if (n.nullTransitions != null)
			return n.nullTransitions;
		
		List<Transition> l0 = new ArrayList<Transition>();
		
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			if (n1.output.equals(PFSG.nullWord))
				l0.add(t);
		}
		
		List<Transition> ll = new ArrayList<Transition>();
		
		for (Transition t: l0)
		{
			Node n1 = nodes.get(t.to);
			List<Transition> l1 = collectNullTransitions(n1);
			for (Transition t1: l1)
			{
				Transition t2 = new Transition();
				t2.p = t.p + t1.p;
				t2.from = n.id;
				t2.to = t1.to;
				ll.add(t2);
			}
		}
		for (Transition t: l0)
		{
			n.transitions.remove(t);
		}
		
		
		l0.addAll(ll);
		
		// make unique (no double null transitions allowed)
		
		Map<Transition,Transition> S = new HashMap<Transition, Transition>();
		for (Transition t: l0)
		{
			Transition t1 = S.get(t);
			if (t1 != null) //  &&
			{
				if (t.p > t1.p)
				{
					//System.err.println("remove " + t1 + " and add " + t);
					S.remove(t1);
					S.put(t,t);
				}
			} else
				S.put(t, t);
		}
		
		
		if (S.size() < l0.size()) // you have to choose the best now.....
		{
			//System.err.println("Double transitions in " + l0 + " --> " + S.values());
			l0.clear();
			l0.addAll(S.values());
		}
		//todo: check double
		
		n.nullTransitions = l0;
		return l0;
	}
	
	public void collectNullTransitions()
	{
		for (Node n: nodes)
		{
			List<Transition> l = collectNullTransitions(n);
			checkDoubleTransitions(n);
			//System.err.println(n.id + " " + l.size());
		}
	}
	
	public void checkDoubleTransitions(Node n)
	{
		Map<String, Transition> m = new HashMap<String, Transition>();
		for (Transition t: n.transitions)
		{
			Node n1 = nodes.get(t.to);
			Transition t1 = m.get(n1.output);
			if (t1 != null)
			{
				System.err.println("Double transition from  " + n + " on " + n1.output);
			} else
			{
				m.put(n1.output, t);
			}
		}
		n.transitionMap = m;
	}
	

	public float evaluate(final List<String> words)
	{
		float p = 0;
		Node n = startNode;
		lm.setOovWordLogProb(this.unknownPenalty);
		float ref = lm.scoreSentence(words);
		int lmOrder = lm.getLmOrder();

		final List<String> sentenceWithBounds =
				new BoundedList<String>(words, lm.getWordIndexer().getStartSymbol(), 
						lm.getWordIndexer().getEndSymbol());

		for (int i=0; i < words.size(); i++)
		{
			String w = words.get(i);
			final List<String>  ngram = 
					(i+1 < lmOrder-1)?  sentenceWithBounds.subList(-1, i+1): 
					sentenceWithBounds.subList(i+1 - lmOrder, i+1);

			float pLM = lm.getLogProb(ngram);
			Transition t = transition(n,w);
			if (t != null)
			{
				Node n1 = nodes.get(t.to);
				
				p += t.p;
				System.err.println("from <" + n + "> to <"  + n1 + ">, p: " + t.p + " pLM " + pLM);
				n = n1;
			} else // reset to start state???? // how to backoff ?? just set n to start state again??
			{
				System.err.println("No transition from " + n + " on  " + w);
				Node n1 = this.backoffNode;
				p += this.unknownPenalty;
				System.err.println("OOV: from <" + n + "> to <"  + n1 + ">, p: " + this.unknownPenalty + " pLM " + pLM);
				n = n1;
			}
		}
		Transition tLast =  transitionToEndNode(n);
		if (tLast != null)
		p += tLast.p;
		System.err.println("ref = " + ref + ", p=" + p + ", diff="  + (ref-p));
		return p;
	}

	public static PFSG readFromFile(String fileName)
	{
		return new Serialize<PFSG>().loadFromFile(fileName);
	}
	
	public void saveToFile(String fileName)
	{
		try {
			Serialize.saveObject(this, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		LM2PFSG x = new LM2PFSG();
		String arg0;
		if (args.length == 0)
			arg0 ="./Test/languageModel.lm";
		else
			arg0 = args[0];
		
		arg0 = stellingWerf_3;
		PFSG pfsg = x.build(arg0);
		//pfsg.collectNullTransitions();
		
		System.err.println(pfsg.backoffNode);
		System.err.println("#transitions(startNode):" +  pfsg.startNode.transitions.size());
		System.err.println("#transitions(backoffNode):" +  pfsg.backoffNode.transitions.size());
		
		// missing: transitions TO empty history backoff??
		
		String s = "DELFFLANDT BIER ÉÉN HIHIHIAAAZ GULDEN";
		//s = "WHAT DO YOU BRILLIANT THINK";
		
		System.err.println(pfsg.evaluate(Arrays.asList(s.toUpperCase().split("\\s+"))));

		//System.out.println(x.nodeNum);
		//System.out.println(x.transitions);
	}
}
