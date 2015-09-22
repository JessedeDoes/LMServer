package eu.transcriptorium.lm.charsets;

import eu.transcriptorium.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import eu.transcriptorium.util.SimpleTokenizer;

public class AlejandrosNewBenthamTokenization implements eu.transcriptorium.lm.CharacterSet
{
	static String[][] escapes =
	{
				{"'", "\\'", "\\\\'"},
				{"\"", "\\\"", "\\\\\""},
				{"*", "\\*", "\\\\\\*"},
	};

	SimpleTokenizer tokenizer = new SimpleTokenizer();
	Map<Character,String> characterNames = new HashMap<Character,String>();
	static Map<Character,String> escapeMap = new HashMap<Character,String>();
	
	static String sentenceStart = "<BS>";
	static String sentenceEnd = "<ES>";
	static String initialSpace = "<is>";
	static String finalSpace = "<fs>";

	static char hasInitialSpaceOnlyMarker =  '<'; // '前'; // problem if accepted by charset
	static char  hasFinalSpaceOnlyMarker = '>'; // '后'; //  problem if accepted by charset
	
	static boolean[] characterAccepted = new boolean[Character.MAX_CODE_POINT+1];

	static
	{
		for (int i=0; i < characterAccepted.length; i++)
			characterAccepted[i] = false;
		for (String[] e: escapes)
		{
			escapeMap.put(e[0].charAt(0), e[1]);
		}
	};
	
	@Override
	public String[] wordToModelNames(String w)
	{
		// TODO Auto-generated method stub
		char[] characters = w.toCharArray();
		List<String> l = new ArrayList<String>();
		String name;
		boolean normalWord = true;
		
		if (w.startsWith(hasInitialSpaceOnlyMarker+""))
		{
			l.add(initialSpace);
			normalWord = false;
		}
	
		for (Character c: characters)
		{
			if (characterAccepted[c])
			{
				l.add(c.toString());
			} else if ((name = characterNames.get(c)) != null)
			{
				// System.err.println(name);
				l.add(name);
			}
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
			if (!isNumber) l1.add(initialSpace);
			l1.addAll(l);
			if (!isNumber) l1.add(finalSpace);
			l = l1;
		}
		String[] a = new String[l.size()];
		return l.toArray(a);
	}

	@Override
	public String cleanWord(String w)
	{
		// TODO Auto-generated method stub
		System.err.println(w);
		tokenizer.tokenize(w);
		List<String> parts = new ArrayList<String>();
		if (tokenizer.prePunctuation.length() > 0)
		{
			parts.add( hasInitialSpaceOnlyMarker + cleanOneToken(tokenizer.prePunctuation));
		}
		if (tokenizer.trimmedToken.length() > 0)
		{
			if (tokenizer.trimmedToken.matches("^[0-9]+$")) // gedoe met getalletjes. lelijk.
			{
				char[] characters = w.toCharArray();
				for (int i=0; i < characters.length; i++)
				{
					if (i==0)
					{
					   if (i==characters.length-1)
						   parts.add("" + hasInitialSpaceOnlyMarker + characters[i] + hasFinalSpaceOnlyMarker  );
					   else
						   parts.add("" + hasInitialSpaceOnlyMarker + characters[i]);
					} else
					{
						if (i==characters.length-1)
							parts.add("" + characters[i] + hasFinalSpaceOnlyMarker  );
						else
							parts.add( "" + characters[i]);
					}
				}
			}  else
				parts.add(cleanOneToken(tokenizer.trimmedToken));
		}
		if (tokenizer.postPunctuation.length() > 0)
		{
			parts.add( cleanOneToken(tokenizer.postPunctuation) + hasFinalSpaceOnlyMarker );
		}
		return StringUtils.join(parts, " ");
	}
	
	public String cleanOneToken(String w)
	{
		char[] characters = w.toCharArray();
		StringBuffer b = new StringBuffer();
		String esc;
		for (char c: characters)
		{
			if (characterAccepted[c])
			{
				b.append(c);
			} else if ((esc = escapeMap.get(c)) != null) // no -- do not escape here yet, only after tokenization.....
			{
				b.append(esc);
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
						characterNames.put(parts[0].charAt(0), parts[1]);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String unescapeWord(String w) 
	{
		// TODO Auto-generated method stub
		
		w = w.replaceAll(hasFinalSpaceOnlyMarker +  "", w);
		w = w.replaceAll("" + hasInitialSpaceOnlyMarker, w);
		
		if (!w.contains("\\"))
			return w;
		for (String[] e: escapes)
		{
			w = w.replaceAll(e[2], e[0]);
		}
		
	
		
		System.err.println("UNESCAPED: " + w);
		return w;
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

