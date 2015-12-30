package eu.transcriptorium.lm.charsets;

import eu.transcriptorium.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.util.SimpleTokenizer;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class AlejandrosNewBenthamTokenization implements eu.transcriptorium.lm.CharacterSet
{
	public boolean splitNumbers = true;
	
	static String[][] escapes =
	{
				{"'", "\\'", "\\\\'"},
				{"\"", "\\\"", "\\\\\""},
				{"*", "\\*", "\\\\\\*"},
				{".", "\\.", "\\\\\\."},
				//{"\\", "\\\\", "\\\\\\\\"},
	};
	
	static Character[][] mappings = 
	{
		{'\u201d', '"'},
		{'\u2014', '-'},
		{ '\u201c', '"'},
		{ '\u2018', '\''},
		{ '\u2019', '\''},
	};
	
	SimpleTokenizer tokenizer = new SimpleTokenizer();
	Map<Character,String> characterModelNames = new HashMap<Character,String>();
	Map<Character,Character> characterMappings = new HashMap<Character,Character>();
	Map<Character,String> escapeMap = new HashMap<Character,String>();
	
	private static String sentenceStart = "<BS>";
	private static String sentenceEnd = "<ES>";
	static String initialSpace = "<is>";
	static String finalSpace = "<fs>";

	//static char hasInitialSpaceOnlyMarker =  '<'; // '前'; // problem if accepted by charset
	//static char  hasFinalSpaceOnlyMarker = '>'; // '后'; //  problem if accepted by charset
	
	static char hasInitialSpaceOnlyMarker =  '↳'; // problem if accepted by charset
	static char hasFinalSpaceOnlyMarker = '↵'; //  problem if accepted by charset
	
	public char getInitialSpaceOnlyMarker()
	{
		return hasInitialSpaceOnlyMarker;
	}
	
	public char getFinalSpaceOnlyMarker()
	{
		return hasFinalSpaceOnlyMarker;
	}
	
	Map<Character,Character> accentStripper = new HashMap<Character,Character>();
	boolean[] characterAccepted = new boolean[Character.MAX_CODE_POINT+1];

	public void init()
	{
		
		for (int j=0; j < characterAccepted.length; j++)
		{
			char i = (char) j;
			characterAccepted[i] = false;
			if (i < 3000)
			{
				Character c1 = deAccent(i);
				if (c1 != null && ((char) c1 != i))
				{
					//System.err.println(i + "!= "+ c1);
					accentStripper.put(i,c1);
				}
			}
		}
	
		for (String[] e: escapes)
		{
			escapeMap.put(e[0].charAt(0), e[1]);
		}
		for (Character[] e: mappings)
		{
			// System.err.println("mapping character:"  + e[0] + "-->" + e[1]);
			characterMappings.put(e[0], e[1]);
		}
	};
	
	public void postInit()
	{
		for (int j=0; j < characterAccepted.length; j++)
		{
			char i = (char) j;
		
			if (i < 3000)
			{
				Character c1 =  Character.toLowerCase(i);
				if (c1 != null && ((char) c1 != i) && !characterAccepted[i])
				{
					//System.err.println(i + "!= "+ c1);
					if (characterAccepted[c1] && !(characterAccepted[i]))
						accentStripper.put(i,c1);
					else
					{
						char c2 = deAccent(c1);
						if (characterAccepted[c2])
							accentStripper.put(i,c2);
					}
				}
			}
		}
		Set<Character> remove = new HashSet<Character>();
		for (Character c: escapeMap.keySet())
		{
			if (!characterAccepted[c] && characterModelNames.get(c) == null)
				remove.add(c);
		}
		for (Character c: remove)
			escapeMap.remove(c);
	}
	
	public AlejandrosNewBenthamTokenization()
	{
		init();
	}
	public static String deAccent(String str) 
	{
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
	public static Character deAccent(Character c) 
	{
	    String nfdNormalizedString = Normalizer.normalize(c+"", Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    String s1 =  pattern.matcher(nfdNormalizedString).replaceAll("");
	    if (s1.length() > 0)
	     return s1.charAt(0);
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
			}  else if (escapeMap.get(c) != null)// escape characters not in handled here?
			{
				l.add(c.toString());
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
			boolean isNumber = w.matches("^[0-9]+$");
			List<String> l1 = new ArrayList<String>();
			if (!isNumber || !splitNumbers) 
				l1.add(initialSpace);
			l1.addAll(l);
			if (!isNumber || !splitNumbers) 
				l1.add(finalSpace);
			l = l1;
		}
		
		String[] a = new String[l.size()];
		return l.toArray(a);
	}

	@Override
	public String cleanWord(String w)
	{
		// TODO Auto-generated method stub
		
		tokenizer.tokenize(w);
		boolean somethingHappened = !(tokenizer.trimmedToken.equals(w));

		List<String> parts = new ArrayList<String>();
		if (tokenizer.prePunctuation.length() > 0)
		{
			parts.add( getInitialSpaceOnlyMarker() + cleanOneToken(tokenizer.prePunctuation));
		}
		if (tokenizer.trimmedToken.length() > 0)
		{
			if (splitNumbers && tokenizer.trimmedToken.matches("^[0-9]+$")) // gedoe met getalletjes. lelijk.
			{
				char[] characters = tokenizer.trimmedToken.toCharArray();
				for (int i=0; i < characters.length; i++)
				{
					if (i==0)
					{
					   if (i==characters.length-1)
						   parts.add("" + getInitialSpaceOnlyMarker() + characters[i] + getFinalSpaceOnlyMarker()  );
					   else
						   parts.add("" + getInitialSpaceOnlyMarker() + characters[i]);
					} else
					{
						if (i==characters.length-1)
							parts.add("" + characters[i] + getFinalSpaceOnlyMarker()  );
						else
							parts.add( "" + characters[i]);
					}
				}
			}  else
				parts.add(cleanOneToken(tokenizer.trimmedToken));
		}
		if (tokenizer.postPunctuation.length() > 0)
		{
			parts.add( cleanOneToken(tokenizer.postPunctuation) + getFinalSpaceOnlyMarker() );
		}
        // if (somethingHappened) System.err.println(w + " ::: " + parts);
		return StringUtils.join(parts, " ");
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
			}  else if ((esc = escapeMap.get(c)) != null) // no -- do not escape here yet, only after tokenization.....
			{
				b.append(esc);
			} else if ((esc = characterModelNames.get(c)) != null)
			{
				//System.err.println("OK! " + esc);
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

	@Override
	public String normalize(String w)
	{
		// TODO Auto-generated method stub
		return w.toUpperCase();
	}

	@Override
	public void loadFromHMMList(String fileName)
	{
		// TODO Auto-generated method stub
		try
		{
			InputStream isCharSet = new FileInputStream(fileName);
			BufferedReader brCharSet = new BufferedReader(new InputStreamReader(isCharSet, "utf8"));
			String line;
			while ((line = brCharSet.readLine()) != null)
			{
				if (line.length() == 1)
				{
					characterAccepted[line.charAt(0)] = true;
				} else
				{
					String[] parts = line.split("\\t");
					if (parts.length > 1 && parts[0].length() > 0)
					{
						System.err.println("mapping:" + parts[0].charAt(0) + " -- "  + parts[1] );
						characterModelNames.put(parts[0].charAt(0), parts[1]);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		postInit();
	}

	/**
	 * Produces the variant in printable form
	 */
	@Override
	public String unescapeWord(String w) 
	{	
		w = w.replaceAll(getFinalSpaceOnlyMarker() +  "$", "");
		w = w.replaceAll("^" + getInitialSpaceOnlyMarker(), "");
		
		
		return removeEscapes(w);
	}
	
	// difference with unescape: the boundary symbols
	
	public String removeEscapes(String w) 
	{	
		if (!w.contains("\\"))
			return w;
		for (String[] e: escapes)
		{
			w = w.replaceAll(e[2], e[0]);
		}
		return w;
	}
	
	protected  String characterOrName(Character c)
	{
		String name;
		if (characterAccepted[c])
		{
			return c.toString();
		} else if ((name = characterModelNames.get(c)) != null)
		{
			// System.err.println(name);
			return name;
		} else  if ((name = escapeMap.get(c)) != null)
			return c.toString();
		//System.err.println("Nothing for + " + c);
		return null;
	}
	
	protected String oneCharacterEscaped(Character c)
	{
		Character c1 = null;
		String esc=null;
		if (characterAccepted[c])
		{
			return c.toString();
		} else if ((c1 = characterMappings.get(c)) != null)
		{ 
			c = c1;
		} 
		if ((esc = escapeMap.get(c)) != null) // no -- do not escape here yet, only after tokenization.....
		{
			return esc;
		} else if ((esc = characterModelNames.get(c)) != null)
		{
			// System.err.println("OK! " + esc);
		    return c.toString();
		} else 
		{
			Character c2 = accentStripper.get(c);
			if (c2 != null && characterAccepted[c2])
				return c2.toString();
		}
		{
			//System.err.println("Discarding:" + c);
		}
		return null;
	}
	
	@Override
	public void setAcceptAll()
	{
		// TODO Auto-generated method stub
		//System.err.println("Accept all characters!");
		for (int j=0; j < characterAccepted.length; j++)
		{
			char i = (char) j; // BUT do not accept the escapes?
			if ( // pas op nu niet meer voor de benoemde karakters, dus zorg dat die geaccepteerd worden
					!(escapeMap.get(i) != null) && 
					(i != '\\') && // HM.....
					(characterModelNames.get(i) == null) &&
			        (i != this.getFinalSpaceOnlyMarker() && 
			        i != this.getInitialSpaceOnlyMarker()))
			{
				//System.err.println("Allow: " + i);
				characterAccepted[i] = true;
			}
		}
	}
	
	public static void main(String[] args)
	{
		CharacterSet dat = new AlejandrosNewBenthamTokenization();
		dat.setAcceptAll();
		
		String test = "In 1724 the (local) Government, \"in Tunisia\"";
		for (String w: test.split("\\s+"))
		{
			String cleaned = dat.cleanWord(w);
			//System.err.println("cleaned " + cleaned);
			for (String tok: cleaned.split("\\s+"))
			{
				//System.err.println("Tok: " + tok);
				String norm = dat.normalize(tok); 
			    System.out.println(w + " " + tok +  " " + dat.normalize(tok) + " --> "  + StringUtils.join(dat.wordToModelNames(tok), " "));
			}
		}
	}

	@Override
	public String getLineStartSymbol() 
	{
		return getSentenceStart();
	}

	@Override
	public String getLineEndSymbol() 
	{
		return getSentenceEnd();
	}

	@Override
	public String cleanLine(String l) 
	{
		// TODO Auto-generated method stub
		List<String> L = new ArrayList<String>();
		for (String w: l.split("\\s+"))
		{
			L.add(cleanWord(w));
		}
		return StringUtils.join(L, " ");
	}

	@Override
	public String escapeWord(String w)
	{
		// TODO Auto-generated method stub
		StringBuffer b = new StringBuffer();
		String esc;
		for (char c: w.toCharArray())
		{
			if (characterAccepted[c])
			{
				b.append(c);
			} else if ((esc = escapeMap.get(c)) != null)
			{
				b.append(esc);
			} else // ??? TODO unclear
			{
				b.append(c);
			}
		}
		return b.toString();
	}

	@Override
	public Map<Character, String> getSpecialCharacterModelNameMap() 
	{
		// TODO Auto-generated method stub
		return  this.characterModelNames;
	}

	@Override
	public String getSentenceStart() {
		return sentenceStart;
	}

	@Override
	public String getSentenceEnd() {
		return sentenceEnd;
	}
	public static void setSentenceStart(String sentenceStart) {
		AlejandrosNewBenthamTokenization.sentenceStart = sentenceStart;
	}

	

	public static void setSentenceEnd(String sentenceEnd) {
		AlejandrosNewBenthamTokenization.sentenceEnd = sentenceEnd;
	}
}



/**
 * New Tokenization rules:

    For HMM and n-gram model training:

    - As you know, for training n-gram each transcript of line image is
      enclosed between <s> </s>. These two meta-symbols have now associated
      morphological models: <BS> (begin sentence) and <ES> (end sentence)
      respectively modelled with HMMs of 3 states.

    - In the HTK dictionary, for each word in addition to specify the
      constituent HMM characters, we add the morphological
      symbols: <is> (initial space) and <fs> (final space) also modelled
      with HMMs of 3 states.

   Example:
      ...
      "HOUSE"  [House]  1.0  <is> H o u s e <fs>
      ...

    - Numbers are tokenized by separating their digits by spaces and
      marking the start and end digits (using < and > respectively).

    - Moreover, quotation marks: " and ' (latter must not be part of
      contractions and possessives (e.g. it's)) are also marked the start
      and end (using < and > respectively).

    - Punctuation marks: , : ; . ( ) [ ] ! ?, are modelled in the
      dictionary adding only the <fs> (excepting for ( ) [ ]).

   Example:
      ...
      "?"  [?]  1.0  ? <fs>
      "."  [.]  1.0  . <fs>
      ";"  [;]  1.0  ; <fs>
      ","  [,]  1.0  , <fs>
      ...
      ")"  [)]  1.0  ) <fs>
      "("  [(]  1.0  <is> (
      ...

   Take note about the difference between "(" and ")". The same for: "["
   "]" and "{" and "}".


   A more illustrative example --> We have the following transcript

      In 1724 the (local) Government, "in Tunisia"

   for training the n-gram, the should be tokenized as follows:

      IN <1 7 2 4> THE ( LOCAL ) GOVERNMENT , <" IN TUNISIA ">

   and in the HTK dictionary:

      "IN"  [in]   xx  <is> i n <fs>
      "<1"   [1]   xx  <is> 1
      "7"    [7]   xx  7
      "2"    [2]   xx  2
      "4>"   [4]   xx  4 <fs>
      "THE"  [the] xx  <is> t h e <fs>
      "("    [(]   xx  <is> (
      "LOCAL" [local] xx <is> l o c a l <fs>
      ")"    [)]   xx  ) <fs>
      "GOVERNMENT" [Government] xx  <is> G o v e r n m e n t <fs>
      ","    [,]   xx  , <fs>
      "<\""  ["]   xx  <is> <dquote>
      "IN"   [in]  xx  <is> i n <fs>
      ...
      "\">"  ["]   xx  <dquote> <fs>



     HMMs: In general 6 states for all the characters and 128 Gaussians
           per state,
                          Exception
                      -----------------------
                       symbols      #States
                      -----------------------
                      . , : ; | ! '   3
                         ( )          4
                           i          5
                           M          7

                       <is> <fs>      3     word initial and final spaces
                       <BS> <ES>      3     Beginning and Ending Sentence
                      ----------------------
 */

