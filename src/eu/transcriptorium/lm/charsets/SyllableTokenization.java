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
public class SyllableTokenization extends ProcessingForCharacterLM
{
	public static String splitMark = "_"; // should be added to character set....
	
	@Override
	public String[] splitInCharacters(String s)
	{
		s = unescapeWord(s);
		String[] p = s.split(splitMark);
		
		for (int i=0; i < p.length; i++)
		{
			p[i] = escapeWord(p[i]);
		}
		return p;
	}
	
	@Override
	public void postInit()
	{
		super.postInit();
		characterAccepted['_'] = true;
	}
	
	public static void main(String[] args)
	{
		CharacterSet dat = new SyllableTokenization();
		dat.setAcceptAll();
		
		String test = "Dogs are, in_deed, re_mar_ka_ble ani_mals";
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
