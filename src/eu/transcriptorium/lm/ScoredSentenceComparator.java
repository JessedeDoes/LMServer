package eu.transcriptorium.lm;

import java.util.Comparator;

public class ScoredSentenceComparator implements Comparator<ScoredSentence>
{
	public int compare(ScoredSentence a, ScoredSentence b)
	{
		return new Float(a.difference).compareTo(b.difference);
	}
}