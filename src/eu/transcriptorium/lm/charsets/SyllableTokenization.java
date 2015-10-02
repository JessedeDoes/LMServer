package eu.transcriptorium.lm.charsets;

import java.util.ArrayList;
import java.util.List;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.util.StringUtils;

/**
 * Good syllable splitting does not really matter here..
 * Ideally - split in such a way that LM is best....
 * Or just preprocess corpus with syllable splitter
 * @author jesse
 *
 */
public class SyllableTokenization extends AlejandrosNewBenthamTokenization
{
	public String cleanWord(String w)
	{
		String s = super.cleanWord(w);
		if (s == null)
			return s;
		String individualCharacters = StringUtils.join(s.split(""), " "); 
		
		return AlejandrosNewBenthamTokenization.hasInitialSpaceOnlyMarker + 
			individualCharacters + 
			AlejandrosNewBenthamTokenization.hasFinalSpaceOnlyMarker;
	}
	
	
	public List<String> syllabify(String w)
	{
		return null;
	}
	
	@Override
	public String[] wordToModelNames(String w)
	{
		// TODO Auto-generated method stub
		//System.err.println("to models: " + w);
		w = removeEscapes(w);
		
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
			if (characterAccepted[c])
			{
				l.add(c.toString());
			} else if ((name = characterModelNames.get(c)) != null)
			{
				// System.err.println(name);
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
		CharacterSet dat = new SyllableTokenization();
		dat.setAcceptAll();
		
		String test = "Dogs are, indeed, remarkable animals";
		for (String w: test.split("\\s+"))
		{
			String cleaned = dat.cleanWord(w);
			//System.err.println("cleaned " + cleaned);
			for (String tok: cleaned.split("\\s+"))
			{
				//System.err.println("Tok: " + tok);
				String norm = dat.normalize(tok); 
			    System.out.println(w + " " + tok +  " " + 
			    		dat.normalize(tok) + " --> "  + StringUtils.join(dat.wordToModelNames(tok), " "));
			}
		}
	}
}
