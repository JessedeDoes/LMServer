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
		String[] p = splitChars(s);
		
		for (int i=0; i < p.length; i++)
		{
			p[i] = escapeWord(p[i]);
		}
		//System.err.println("p0:<" + p[0] + ">");
		return p;
	}
	
	public static String[] splitChars(String s)
	{
		char[] p = s.toCharArray();
		String[] r = new String[p.length];
		for (int i=0; i < p.length; i++)
			r[i]  = p[i] + "";
		return r;
	}
	
	// here we really NEED the markers -- or else words cannot be put together...
	
	@Override
	public String unescapeWord(String w) 
	{	
		// w = w.replaceAll(getFinalSpaceOnlyMarker() +  "$", "");
		//w = w.replaceAll("^" + getInitialSpaceOnlyMarker(), "");
		
		return removeEscapes(w);
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
			R.add(hasInitialSpaceOnlyMarker + 
					StringUtils.join(splitInCharacters(pre), " ") ); 
		}
		
		if (main != null && main.length() > 0)
		{
		
			String x = hasInitialSpaceOnlyMarker + 
					StringUtils.join(splitInCharacters(main), " ") +   
					hasFinalSpaceOnlyMarker;
			//System.err.println("<" + main+  "> " + x); 
			R.add(x); 
		}
		
		if (post != null && post.length() > 0)
		{
			R.add(StringUtils.join(splitInCharacters(post), " ") +   
					   hasFinalSpaceOnlyMarker) ; 
		}
		//System.err.println(R);
		return StringUtils.join(R, " ");
	}
	
	
	@Override
	public String[] wordToModelNames(String w)
	{
		
		w = removeEscapes(w);
		
		
		char[] characters = w.toCharArray();
		List<String> l = new ArrayList<String>();
		String name;
		boolean normalWord = true;
		
		
		
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
		
		
		String[] a = new String[l.size()];
		return l.toArray(a);
	}

	public static void main(String[] args)
	{
		CharacterSet dat = new ProcessingForCharacterLM();
		dat.setAcceptAll();
		
		String test = "Dogs are, indeed, 'remarkable' animals. Yes!";
		System.err.println(dat.cleanLine(test));
		
		if (false) for (String w: test.split("\\s+"))
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
