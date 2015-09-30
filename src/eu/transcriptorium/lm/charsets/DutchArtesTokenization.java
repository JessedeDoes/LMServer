package eu.transcriptorium.lm.charsets;
import java.util.*;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.util.StringUtils;

/**
 * This is rather messy
 * Normalized strings should only use expansions....
 * Normalization could use a lexicon
 * @author does
 *
 */
public class DutchArtesTokenization extends AlejandrosNewBenthamTokenization
{
	// ↵ ↳
	static char hasInitialSpaceOnlyMarker =  '↳'; // problem if accepted by charset
	static char hasFinalSpaceOnlyMarker = '↵'; //  problem if accepted by charset
	static char startSpecial = '<';
	static char endSpecial= '>';
	static char separator=':';

	enum type
	{
		EXPANSIONS,
		ABBREVIATIONS,
		BOTH
	}  ;

	type abbreviationHandling = type.EXPANSIONS;

	public char getInitialSpaceOnlyMarker()
	{
		return hasInitialSpaceOnlyMarker;
	}

	public char getFinalSpaceOnlyMarker()
	{
		return hasFinalSpaceOnlyMarker;
	}

	static class Symbol
	{
		enum SymbolType
		{
			NORMAL,
			ABBREVIATION
		};
		SymbolType type = SymbolType.NORMAL;
		char character;
		String abbreviation; // should not be longer that 1?
		String expansion;

		Symbol(char c)
		{
			type=SymbolType.NORMAL;
			character = c;
		}

		Symbol(String abbreviation, String expansion)
		{
			type=SymbolType.ABBREVIATION;
			this.abbreviation=abbreviation;
			this.expansion=expansion;
		}

		public String toString()
		{
			if (type == SymbolType.NORMAL)
				return character + "";
			return "(" + abbreviation + ","  + expansion + ")";
		}
	}

	static enum State
	{
		START,
		IN_ABBREVIATION,
		IN_EXPANSION
	};

	private List<Symbol> decompose(String w)
	{
		List<Symbol> l = new ArrayList<Symbol>();
		String a="";
		String e ="";
		char[] chars = w.toCharArray();
		State state = State.START;
		for (int i=0; i < chars.length; i++)
		{
			char c = chars[i];
			switch(state)
			{
			case START: 
				if (c == startSpecial)
					state = State.IN_ABBREVIATION;
				else l.add(new Symbol(c));
				break;
			case IN_ABBREVIATION:
				if (c==endSpecial)
				{
					l.add(new Symbol(a,e));
					a=e="";
					state = State.START;
				} else if (c==separator)
				{
					state = state.IN_EXPANSION;
				} else
				{
					a += c;
				}
				break;
			case IN_EXPANSION:
				if (c==endSpecial)
				{
					l.add(new Symbol(a,e));
					a=e="";
					state = State.START;
				} else
				{
					e += c;
				}
				break;
			};
		}
		return l;
	}

	/** this should also use the symbol decomposition
	 * BUT: if normalized is a function of cleaned
	 * we always need to keep BOTH...
	 * so cleaning is just removal of unrecognizable characters.
	 */
	public String cleanOneToken(String w)
	{
		StringBuffer b = new StringBuffer();
		List<Symbol> symbols = decompose(w); // unescape first? mormalized has escapes...

		for (Symbol s: symbols)
		{
			switch (s.type)
			{
			case NORMAL: 
				Character c = s.character;
				String z = oneCharacterEscaped(c);
				//System.err.println(z);
				if (z != null)
					b.append(z);
				break;
			case ABBREVIATION: // ahem....
				Character a0 = null;
				String e = s.expansion;

				if (s.abbreviation.length() > 0)
				{
					a0 = s.abbreviation.charAt(0);
				}

				b.append(startSpecial);

				if (a0 != null)
				{
					z = oneCharacterEscaped(a0);
					if (z != null)
						b.append(z);
				}
				b.append(separator);
				char[] expchars = e.toCharArray();
				for (char ce: expchars)
				{
					String x = oneCharacterEscaped(ce);
					if (x != null)
						b.append(x);
				}

				b.append(endSpecial);
			}		
		}



		if (b.length() == 0)
		{
			//System.err.println("no result for " + w);
		} else if (b.length() < w.length())
		{
			//System.err.println("word " + w  + " truncated to " + b);
		}
		return b.toString();
	}

	/**
	 * Normalization always uses the expansions only....
	 * This ensures that different abbreviation conventions still
	 * can use common language models
	 */

	@Override
	public String normalize(String w)
	{
		// TODO Auto-generated method stub
		String r="";
		List<Symbol> symbols = decompose(w);
		for (Symbol s: symbols)
		{
			switch (s.type)
			{
			case NORMAL: r += s.character; break;
			case ABBREVIATION: r += s.expansion; break; 
			}
		}
		r = r.replaceAll("v", "u");
		r = r.replaceAll("gh", "g");
		r = r.replaceAll("ae", "aa");
		r = r.replaceAll("ca", "ka");
		r = r.replaceAll("co", "ko");
		r = r.replaceAll("cu", "ku");
		r = r.replaceAll("c$", "k");
		r = r.replaceAll("ck", "k");
		return r.toUpperCase();
	}

	@Override
	public String[] wordToModelNames(String w)
	{
		// TODO Auto-generated method stub
		//System.err.println("expand to models: " + w);
		w = removeEscapes(w);

		List<Symbol> symbols = decompose(w);
		//System.err.println(symbols);
		char[] characters = w.toCharArray();
		List<String> l = new ArrayList<String>();
		String name;
		boolean normalWord = true;


		if (w.startsWith(hasInitialSpaceOnlyMarker+""))
		{
			l.add(initialSpace);
			normalWord = false;
		}

		for (Symbol s: symbols)
		{
			switch (s.type)
			{
			case NORMAL: 
				Character c = s.character;
				String z = characterOrName(c);
				if (z != null)
					l.add(z);
				break;
			case ABBREVIATION: // ahem....
				Character a0 = null;
				String e = s.expansion;
				if (s.abbreviation.length() > 0)
				{
					a0 = s.abbreviation.charAt(0);
				}
				switch (this.abbreviationHandling)
				{
				case ABBREVIATIONS:
					if (a0 != null)
					{
						z = characterOrName(a0);
						if (z != null)
							l.add(z);
					}
					break;
				case EXPANSIONS:
					if (e != null)
					{
						char[] expchars = e.toCharArray();
						for (char ce: expchars)
						{
							String x = characterOrName(ce);
							if (x != null)
								l.add(x);
						}
					}
					break;
				case BOTH:

				} 
			};
		}

		if (w.endsWith(hasFinalSpaceOnlyMarker+""))
		{
			l.add(finalSpace);
			normalWord = false;
		}

		if (normalWord)
		{
			boolean isNumber = w.matches("^[0-9]+$");
			List<String> l1 = new ArrayList<String>();
			if (!isNumber) 
				l1.add(initialSpace);
			l1.addAll(l);
			if (!isNumber) 
				l1.add(finalSpace);
			l = l1;
		}

		String[] a = new String[l.size()];
		return l.toArray(a);
	}

	public static void main(String[] args)
	{
		CharacterSet dat = new DutchArtesTokenization();
		dat.setAcceptAll();

		String test = "hallo! <ẽ:ende> ic gheloof, dat i 'het' niet en can 123";
		for (String w: test.split("\\s+"))
		{
			String cleaned = dat.cleanWord(w);
			for (String tok: cleaned.split("\\s+"))
			{
				String norm = dat.normalize(tok); 
				System.out.println(w + " " + tok +  " " + dat.normalize(tok) + " --> "  + StringUtils.join(dat.wordToModelNames(tok), " "));
			}
		}
	}
}

/**
 * Denk ook aan:
 *         $text =~ s/$pilcrow/<ESP>/g;
        $text =~ s/§/<PARA>/g;
        $text =~ s/\/\//<HYPHEN>/g;
        $text =~ s/\//<SLASH>/g;
 */
