package eu.transcriptorium.lm.charsets;
import java.util.*;

/**
 * This is rather messy
 * Normalized strings should only use expansions....
 * Normalization could use a lexicon
 * @author does
 *
 */
public class DutchArtesTokenization extends AlejandrosNewBenthamTokenization
{
	static char hasInitialSpaceOnlyMarker =  '前'; // problem if accepted by charset
	static char  hasFinalSpaceOnlyMarker = '后'; //  problem if accepted by charset
	static char startSpecial = '<';
	static char endSpecial= '>';
	static char separator=';';
	
	enum type
	{
		EXPANSIONS,
		ABBREVIATIONS,
		BOTH
	}  ;
	
	type abbreviationHandling = type.ABBREVIATIONS;
	
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
			normal,
			abbreviation
		};
		SymbolType type = SymbolType.normal;
		char character;
		String abbreviation; // should not be longer that 1?
		String expansion;
		Symbol(char c)
		{
			type=SymbolType.normal;
			character = c;
		}
		Symbol(String abbreviation, String expansion)
		{
			type=SymbolType.abbreviation;
			this.abbreviation=abbreviation;
			this.expansion=expansion;
		}
	}
	
	static enum State
	{
		normal,
		inAbbreviation,
		inExpansion
	};
	
	private List<Symbol> decompose(String w)
	{
		List<Symbol> l = new ArrayList<Symbol>();
		String a="";
		String e ="";
		char[] chars = w.toCharArray();
		State state = State.normal;
		for (int i=0; i < chars.length; i++)
		{
			char c = chars[i];
			switch(state)
			{
			case normal: 
				if (c == startSpecial)
					state = State.inAbbreviation;
				else l.add(new Symbol(c));
				break;
			case inAbbreviation:
				if (c==endSpecial)
				{
					l.add(new Symbol(a,e));
					a=e="";
				   state = State.normal;
				} else if (c==separator)
				{
					state = state.inExpansion;
				} else
				{
					a += c;
				}
				break;
			case inExpansion:
				if (c==endSpecial)
				{
					l.add(new Symbol(a,e));
					a=e="";
				   state = State.normal;
				} else
				{
					e += c;
				}
				break;
			};
		}
		return l;
	}
	
	public String cleanOneToken(String w)
	{
		char[] characters = w.toCharArray();
		StringBuffer b = new StringBuffer();
		String esc; Character c1;
		for (char c: characters)
		{
			if (characterAccepted[c])
			{
				b.append(c);
			} else if ((c1 = characterMappings.get(c)) != null)
			{ 
				c = c1;
			} 
			if ((esc = escapeMap.get(c)) != null) // no -- do not escape here yet, only after tokenization.....
			{
				b.append(esc);
			} else if ((esc = characterNames.get(c)) != null)
			{
				// System.err.println("OK! " + esc);
			    b.append(c);	
			} else 
			{
				Character c2 = accentStripper.get(c);
				if (c2 != null && characterAccepted[c2])
					b.append(c2);
			}
			{
				//System.err.println("Discarding:" + c);
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
	 */
	
	@Override
	public String normalize(String w)
	{
		// TODO Auto-generated method stub
		return w.toUpperCase();
	}
	
	public String[] wordToModelNames(String w)
	{
		// TODO Auto-generated method stub
		//System.err.println("to models: " + w);
		w = removeEscapes(w);
		List<Symbol> symbols = decompose(w);
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
			case normal: 
				Character c = s.character;
				String z = characterOrName(c);
				if (z != null)
					l.add(z);
				break;
			case abbreviation: // ahem....
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
}
