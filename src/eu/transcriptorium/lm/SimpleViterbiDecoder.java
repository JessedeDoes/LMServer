package eu.transcriptorium.lm;
import java.util.*;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.BoundedList;
import eu.transcriptorium.lm.VariantLexicon.Variant;
import eu.transcriptorium.util.Functions;

/**
 * This selects, assuming an LM defined on sequences of "normalized" forms and a weighted variant lexicon,
 * the most probable sequence of normalized forms.
 * To select the most probable sequence of witnessed forms, one should add probability along paths instead of maximizing.
 * This implementation is completely inefficient.
 * @author does
 *
 */
public class SimpleViterbiDecoder 
{
	VariantLexicon lexicon;
	NgramLanguageModel<String> lm;
	
	static class State
	{
		List<String> ngram;
		double probability;
		State predecessor;
	}
	
	static List<String> shift(List<String> prev, String nextSymbol)
	{
		List<String> n = new ArrayList<String>();
		for (int i=1; i < prev.size(); i++)
			n.add(prev.get(i));
		n.add(nextSymbol);
		return n;
	}
	
	private List<Variant> handleSentenceBounds(String symbol)
	{
		List<Variant> l = new ArrayList<Variant>();
		Variant v = new Variant();
		v.normalForm = symbol; v.variantForm = symbol;
		v.probability = 1;
		l.add(v);
		return l;
	}
	
	private List<Variant> handleUnknown(String symbol)
	{
		List<Variant> l = new ArrayList<Variant>();
		Variant v = new Variant();
		v.normalForm = lm.getWordIndexer().getUnkSymbol(); 
		v.variantForm = symbol;
		v.probability = 1;
		l.add(v);
		return l;
	}
	
	private Collection<State> moveNext(Collection<State> previous, String nextSymbol)
	{
		List<Variant> variants;
		//System.err.println("next:" + nextSymbol);
		if (nextSymbol.equals(lm.getWordIndexer().getStartSymbol()) || nextSymbol.equals(lm.getWordIndexer().getEndSymbol()))
			variants = handleSentenceBounds(nextSymbol);
		else
		   variants = lexicon.getVariantsWithWordform(nextSymbol);
		Map<List<String>, State> stateMap = new HashMap<List<String>, State>();
		if (variants == null)
			variants = handleUnknown(nextSymbol);
		if (variants != null)
		{
			for (Variant v: variants)
			{
				String normal = v.normalForm;
				System.err.println("next:" + nextSymbol + " normal: " + v);
				for (State s: previous)
				{
					List<String> shifted = shift(s.ngram, normal); // quite inefficient
					double lp = lm.getLogProb(shifted);
					System.err.println(shifted + " : " + lp);
					State next = stateMap.get(shifted);
					if (next == null)
					{
						next = new State();
						next.ngram = shifted;
						next.predecessor = s;
						next.probability = 
								s.probability + lm.getLogProb(shifted) + Functions.log2(v.probability);
						stateMap.put(shifted, next);
					} else
					{
						double thisScore =
						 s.probability + lm.getLogProb(shifted) + Functions.log2(v.probability);
						if (thisScore > next.probability)
						{
							next.probability = thisScore;
							next.predecessor = s;
						}
					}
				}
			}
		}
		return stateMap.values();
	}
	
	/**
	 * Simple dynamic programming search
	 * @param sentence (Sentence to be processed)
	 * @param lm (Language model)
	 * @param lexicon (Variant lexicon)
	 */
	synchronized void scoreSentence(final List<String> sentence, final NgramLanguageModel<String> lm, VariantLexicon lexicon)
	{
		this.lm = lm;
		this.lexicon = lexicon;
		
		final List<String> sentenceWithBounds = new BoundedList<String>(sentence, lm.getWordIndexer().getStartSymbol(), lm.getWordIndexer().getEndSymbol());
		List<Collection<State>> trellis = new ArrayList<Collection<State>>();
		// add a start state
		List<String> dummyNgram = new ArrayList<String>();
		for (int i=0; i < lm.getLmOrder(); i++)
		{
			dummyNgram.add(lm.getWordIndexer().getStartSymbol());
		}
		
		State startState = new State();
			startState.ngram = dummyNgram;
			startState.probability = 0;
			startState.predecessor = null;
		
		Set<State> startList = new HashSet<State>();
		startList.add(startState);
		trellis.add(startList);
		int lmOrder = lm.getLmOrder();
		
		for (int i = 1; i < lmOrder - 1 && i <= sentenceWithBounds.size() + 1; ++i) 
		{
			Collection<State> next = moveNext(trellis.get(i-1), sentenceWithBounds.get(i));
			trellis.add(next);
		}
		
		int p = Math.min(2, lmOrder-1);
		for (int i = lmOrder - 1; i < sentenceWithBounds.size() + p; ++i)
		{
			Collection<State> next = moveNext(trellis.get(i-1), sentenceWithBounds.get(i));
			trellis.add(next);
		}
		// trace back ...
		Collection<State> finalStates = trellis.get(trellis.size()-1);
		State bestState=null;
		double best = Double.NEGATIVE_INFINITY;
		Stack<State> states = new Stack<State>();
		for (State s: finalStates)
		{
			if (s.probability > best)
			{
				best = s.probability;
				bestState = s;
			}
		}
		for (State s = bestState; s != null; s = s.predecessor)
		{
			states.push(s);
		}
		List<State> statePath = new ArrayList<State>();
		List<String> normalizedSentence = new ArrayList<String>();
		while (!states.isEmpty())
		{
			State s = states.pop();
			statePath.add(s);
			normalizedSentence.add(s.ngram.get(s.ngram.size()-1));
		}
		System.err.println(normalizedSentence); // one end symbol too long ...
	}
}
