package eu.transcriptorium.lm.subword;

import impact.ee.util.StringUtils;

import java.util.*;

import eu.transcriptorium.lm.charsets.AlejandrosNewBenthamTokenization;
import eu.transcriptorium.lm.charsets.ProcessingForCharacterLM;

public class MultiLevelText
{
	int nLevels = 2;
	List<List<String>> levels = new ArrayList<List<String>>();
	List<Node> base = new ArrayList<Node>();
	
	class Node
	{
		int level;
		String word;
		List<Integer> children = new ArrayList<Integer>();
		double logProb;
		double oovProb;
		double combiProb;
		
		public String toString()
		{
			List<String> parts = new ArrayList<String>();
			for (int i: children)
			{
				parts.add(i + ":"  + levels.get(level+1).get(i));
			}
			return word + " {" + StringUtils.join(parts, ", ") + "}";
		}
		
		public Node(int level)
		{
			this.level = level;
		}
	}
	
	public MultiLevelText(int nLevels)
	{
		this.nLevels = nLevels;
		for (int i=0; i < nLevels; i++)
		{
			levels.add(new ArrayList<String>());
		}
	}
	
	public void parseFromString(String sentence)
	{
		String[] parts = sentence.split("\\s+");
		List<String> segments = Arrays.asList(parts);
		List<String> words = levels.get(0);
		this.levels.set(1, segments);
		String currentWord = "";
		AlejandrosNewBenthamTokenization t = new AlejandrosNewBenthamTokenization();
		Node currentNode = null;
		
		for (int i=0; i < segments.size(); i++)
		{
			String s = segments.get(i);
			
			if (s.startsWith(t.getInitialSpaceOnlyMarker()+""))
			{
				if (currentWord.length() > 0)
				{
					base.add(currentNode);
					currentNode.word = currentWord;
					currentWord = "" ;
					currentNode = new Node(0);
				}	
			} 
			if (s.endsWith(t.getFinalSpaceOnlyMarker()+""))
			{
				currentWord += s;
				if (currentNode == null)
					currentNode = new Node(0);
				currentNode.word = currentWord;
				currentNode.children.add(i);
				base.add(currentNode);
				currentNode = null;
				currentWord = "" ;
			} else // normal
			{
				currentWord += s;
				if (currentNode == null)
					currentNode = new Node(0);
				currentNode.word = currentWord;
				currentNode.children.add(i);
			}
		}
		if (currentNode != null)
			base.add(currentNode);
		
	}
	
	public static void main(String[] args)
	{
	    MultiLevelText t = new MultiLevelText(2);
	    ProcessingForCharacterLM p= new ProcessingForCharacterLM();
	    p.setAcceptAll();
	    String s = p.normalize(p.cleanLine("De hond, die is een ????uitermate opmerkelijk dier"));
	    t.parseFromString(s);
	    System.err.println(t.base);
	}
}
