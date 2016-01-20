package eu.transcriptorium.suggest;
import java.io.*;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.Counter;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lattice.ParagraphDecoder;
import eu.transcriptorium.lm.VariantLexicon;

/*
 * Eventually: use lucene/blacklab for faster search ?
 */


import javax.json.Json;
import javax.json.stream.*;

public class Suggest
{
	NgramLanguageModel<String> languageModel = null;
	VariantLexicon variantLexicon = null;
	boolean caseInsensitive = true;
	int maximumSuggestions = 10;
	
	Counter<String> vocabulary ;

	
	public static String counterToJSON(Counter<String> c, int max)
	{
		StringWriter  strw = new StringWriter();
		BufferedWriter sw = new BufferedWriter(strw);
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, false); // dit heeft dus geen invloed ...

		JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
		JsonGenerator jg = jgf.createGenerator(sw);

		jg = jg.writeStartObject().write("name", "suggestions");
		jg = jg.write("nMatches", c.size());
		jg = jg.writeStartArray("entries");
		int i=0;

		for (Map.Entry<String, Double> e: 	c.getEntriesSortedByDecreasingCount())
		{
			//System.err.println(sw.toString());
			String w = e.getKey();
			if (!w.matches("^\\p{L}+$"))
			{
				System.err.println("Discarding: " + w);
				continue;
			}
			w = w.toLowerCase();
			jg = jg.writeStartObject().write("word",w).write( "count",e.getValue()).writeEnd();

			if (i++ > max)
			{
				//System.err.println("bla! " + i);
				break;
			}
		}

		jg = jg.writeEnd();
		jg = jg.writeEnd();

		jg.close();

		return strw.getBuffer().toString();
	}

	public static class WordAndScore
	{
		String word;
		String score;
	}

	public Suggest(NgramLanguageModel lm)
	{
		this.languageModel = lm;
		List<String> fakeHistory = new ArrayList<String>();
		vocabulary = NgramLanguageModel.StaticMethods.getDistributionOverNextWords(lm, fakeHistory);
		//System.out.println(counterToJSON(vocabulary,10));
		//System.out.flush();
	}

	public void dumpVocabulary()
	{
		for (Map.Entry<String,Double> sug: 	vocabulary.getEntriesSortedByDecreasingCount())
		{
			System.out.println(sug);
		}
	}

	/**
	 * Dit mag wel weg?
	 */
	public List<WordAndScore> suggest(List<String> history,  String pattern, int max) 
	{
		List<WordAndScore> list = new ArrayList<WordAndScore>();
		return list;
	}

	public   Counter<String> getDistributionOverNextWords(List<String> context, Collection<String> candidates)
	{
		long  s = System.currentTimeMillis();
		Counter<String> r =  getDistributionOverNextWords(this.languageModel, context, candidates);
		long e = System.currentTimeMillis();
		System.err.println("LM time: " + (e-s));
		return r;
	}

	public   Counter<String> getDistributionOverNextWords(List<String> context, String pattern)
	{
		long  s = System.currentTimeMillis();
		Counter<String> r =  getDistributionOverNextWords(this.languageModel, context, matchingWords(pattern));
		long e = System.currentTimeMillis();
		System.err.println("LM time: (including matching) " + (e-s));
		return r;
	}

	public Counter<String> getDistributionOverContextWords(List<String> leftContext, List<String> rightContext, String pattern)
	{
		Set<String> candidates  = matchingWords(pattern);

		List<String> sentence = new ArrayList<String>();

		int i = leftContext.size();
		sentence.addAll(leftContext);
		sentence.add("?");
		sentence.addAll(rightContext);

		Counter<String> c = new Counter<String>();
		for (String w: candidates)
		{
			sentence.set(i, w);
			double logProb = this.languageModel.scoreSentence(sentence);
			double p = Math.exp(logProb) * Math.log(10);
			c.setCount(w, p);
		}
		return c;
	}

	public static <W> Counter<W> getDistributionOverNextWords(final NgramLanguageModel<W> lm,  List<W> context, Collection<W> candidates) 
	{
		List<W> ngram = new ArrayList<W>();
		for (int i = 0; i < lm.getLmOrder() - 1 && i < context.size(); ++i) 
		{
			ngram.add(context.get(context.size() - i - 1));
		}
		if (ngram.size() < lm.getLmOrder() - 1) 
			ngram.add(lm.getWordIndexer().getStartSymbol());
		Collections.reverse(ngram);
		ngram.add(null);
		Counter<W> c = new Counter<W>();

		for (W word: candidates) 
		{
			if  (word.equals(lm.getWordIndexer().getStartSymbol())) continue;
			ngram.set(ngram.size() - 1, word);
			c.setCount(word, Math.exp(lm.getLogProb(ngram) * Math.log(10)));
		}
		return c;
	}

	/**
	 * This is slow and should be improved by adding some matching engine (lucene?)
	 * @param pattern
	 * @return
	 */

	public Set<String> matchingWords(String pattern)
	{
		if (pattern == null || pattern.length() == 0 ||  pattern.equals(".*"))
			return this.vocabulary.keySet();
		
		if (caseInsensitive)
			pattern = pattern.toUpperCase();
		Set<String> S = new HashSet<String>();
		for (String x: this.vocabulary.keySet())
		{
			if (x.matches(pattern))
				S.add(x);
		}
		System.err.println("matches: " + S.size());
		return S;
	}

	public void test()
	{
		System.err.println("model loaded ... ");
		String s;
		int rejects=0;
		int max=20;
		try
		{
			BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
			float total=0;
			int k=0;

			while ((s = b.readLine()) != null)
			{
				String[] parts = s.split("\t");
				String leftContext;
				String pattern;
				Counter<String> suggestions = null;
				List<String> history = new ArrayList<String>();
				List<String> future = new ArrayList<String>();

				if (parts.length > 1)
				{
					leftContext = parts[0];
					pattern = parts[1];

					for (String w: leftContext.split("\\s+"))
					{
						if (w.length() > 0)
							history.add(w.toUpperCase());
					}
					if (parts.length > 2)
					{
						String rightContext = parts[2];
						for (String w: rightContext.split("\\s+"))
						{
							if (w.length() > 0)
								future.add(w.toUpperCase());
						}
					}
				} else
				{
					pattern = parts[0];
				}
				if (future.size() > 0)
				{
					suggestions = this.getDistributionOverContextWords(history, future, pattern);
				} else
				{
					suggestions = getDistributionOverNextWords(history, matchingWords(pattern.toUpperCase()));
				}

				int l=0;

				for (Map.Entry<String, Double> sug: 	suggestions.getEntriesSortedByDecreasingCount())
				{
					System.out.println(sug.getKey().toLowerCase()  + "\t " + sug.getValue());
					if (l++ > max)
						break;
				}
			}

		} catch (Exception e)
		{
		}
	}

	public static void main(String[] args)
	{
		String languageModel = args[0];
		NgramLanguageModel lm = null;

		// languageModel = null;

		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}

		Suggest s = new Suggest(lm);
		s.test();
	}

	public Counter<String> getDistributionOverContextWords(String left, String right, String pattern)
	{
		if (caseInsensitive)
		{
			if (left != null) left = left.toUpperCase();
			if (right != null) right = right.toUpperCase();
		}
		if (pattern == null || pattern.equals(""))
		{
			pattern = ".*";
		}
		List<String> ll  = new ArrayList<String>();
		if (left != null) 
		{
			String[] l = left.split("\\s+");
			ll = Arrays.asList(l);
		}
		if (right == null || right.length() == 0)
		{
			return this.getDistributionOverNextWords(ll, pattern);
		} else
		{

			String[] r = right.split("\\s+");

			List<String> rr = Arrays.asList(r);
			return this.getDistributionOverContextWords(ll, rr, pattern);
			// TODO Auto-generated method stub
		}
	}
}
