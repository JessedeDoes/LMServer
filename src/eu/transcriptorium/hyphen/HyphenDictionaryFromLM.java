package eu.transcriptorium.hyphen;

import java.util.List;

import edu.berkeley.nlp.lm.NgramLanguageModel;

public class HyphenDictionaryFromLM implements HyphenationDictionary 
{
	NgramLanguageModel lm = null;
	double p = 0.03;
	
	
	public HyphenDictionaryFromLM(NgramLanguageModel lm)
	{
		this.lm = lm;
	}
	@Override
	
	
	public double getHyphenationProbability(String w, String p1, String p2) 
	{
		// TODO Auto-generated method stub
		int unkIndex = this.lm.getWordIndexer().getIndexPossiblyUnk(this.lm.getWordIndexer().getUnkSymbol());
		boolean wordInLM = this.lm.getWordIndexer().getIndexPossiblyUnk(w) != unkIndex;
		
		if (!wordInLM)
			return 0;
		if (p1.endsWith("-"))
		{
			String P1 = p1.replaceAll("-$", "");
			if (P1.length() > 0 && p2.length() > 0 && w.equals(P1 + p2))
				return p;
		}
		return 0;
	}

	@Override
	public List<String> getProlongations(String prefix) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}
