package eu.transcriptorium.lm.charsets;

import java.util.ArrayList;
import java.util.List;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.util.StringUtils;

/**
 * Not quite clear this will work
 * @author jesse
 *
 */
public class ProcessingForCharacterLM extends AlejandrosNewBenthamTokenization
{
	/*
	 * unescape, split, escape.
	 */
	public String[] splitInCharacters(String s)
	{
		s = unescapeWord(s);
		String[] p = s.split("");
		
		for (int i=0; i < p.length; i++)
		{
			p[i] = escapeWord(p[i]);
		}
		return p;
	}
	
	public String cleanWord(String w)
	{
		tokenizer.tokenize(w);
		
		String pre = cleanOneToken(tokenizer.prePunctuation);
		String main = cleanOneToken(tokenizer.trimmedToken);
		String post =  cleanOneToken(tokenizer.postPunctuation);
		
		List<String> R = new ArrayList<String>();
		String r = "";
		
		if (pre != null && pre.length() > 0)
		{
			R.add(AlejandrosNewBenthamTokenization.hasInitialSpaceOnlyMarker + 
					StringUtils.join(splitInCharacters(pre), " ") ); 
		}
		
		if (main != null && main.length() > 0)
		{
			R.add(AlejandrosNewBenthamTokenization.hasInitialSpaceOnlyMarker + 
					StringUtils.join(splitInCharacters(main), " ") +   
					AlejandrosNewBenthamTokenization.hasFinalSpaceOnlyMarker); 
		}
		
		if (post != null && post.length() > 0)
		{
			R.add(StringUtils.join(splitInCharacters(post), " ") +   
					   AlejandrosNewBenthamTokenization.hasFinalSpaceOnlyMarker) ; 
		}
		return StringUtils.join(R, " ");
	}
	
	
	@Override
	public String[] wordToModelNames(String w)
	{
		// TODO Auto-generated method stub
		//System.err.println("to models: " + w);
		w = removeEscapes(w);
		
		//System.err.println("unescaped: " + w);
		char[] characters = w.toCharArray();
		List<String> l = new ArrayList<String>();
		String name;
		boolean normalWord = true;
		
		// System.err.println(w);
		//if (w.contains(">") || w.contains("<"))
		//{
			//System.err.println(w);
		//}
		
		if (w.startsWith(getInitialSpaceOnlyMarker()+""))
		{
			l.add(initialSpace);
			normalWord = false;
		}
	
		for (Character c: characters)
		{
			if (characterAccepted[c]) // 
			{
				l.add(c.toString());
			} else if ((name = characterModelNames.get(c)) != null) // escape characters should have names?
			{
				// System.err.println(name);
				l.add(name);
			} else if ((name = escapeMap.get(c)) != null)
			{
				l.add(name);
			}
		}
		
		if (w.endsWith(getFinalSpaceOnlyMarker()+""))
		{
			l.add(finalSpace);
			normalWord = false;
		}
		
		//if (!normalWord)
		//{
		//	System.err.println(w + "--->"  + l);
		// }
		
		if (normalWord)
		{
			/*// don't do all this...
			boolean isNumber = w.matches("^[0-9]+$");
			List<String> l1 = new ArrayList<String>();
			if (!isNumber) 
				l1.add(initialSpace);
			l1.addAll(l);
			if (!isNumber) 
				l1.add(finalSpace);
			l = l1;
			*/
		}
		
		String[] a = new String[l.size()];
		return l.toArray(a);
	}

	public static void main(String[] args)
	{
		CharacterSet dat = new ProcessingForCharacterLM();
		dat.setAcceptAll();
		
		String test = "Dogs are, indeed, 'remarkable' animals";
		for (String w: test.split("\\s+"))
		{
			String cleaned = dat.cleanWord(w);
			//System.err.println("cleaned " + cleaned);
			for (String tok: cleaned.split("\\s+"))
			{
				//System.err.println("Tok: " + tok);
				String norm = dat.normalize(tok); 
			    System.out.println("word:  " + w +  " cleaned:  " + cleaned + " token: {" + tok +  "} normalized:  " + 
			    		dat.normalize(tok) + " --> ["  + StringUtils.join(dat.wordToModelNames(tok), " ")  + "]");
			}
		}
	}
}
