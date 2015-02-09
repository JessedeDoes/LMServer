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
		if (p1.matches(".*" + getHyphenationRegex()))
		{
			String P1 = p1.replaceAll(getHyphenationRegex(), "");
			String P2 = p2.replaceAll(getSecondPartHyphenationRegex(), "");
			
			//System.err.println("check hyph P1 " + P1 + " p1 " + p1 +  " P2 "  +  P2 +  " w "  + w);
		
			if (P1.length() > 0 && p2.length() > 0 && P1.matches(".*[A-Za-z][A-Za-z].*")  
					&& p2.matches(".*[A-Za-z][A-Za-z].*") && w.equals(P1 + P2))
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
	@Override
	public String getHyphenationRegex()
	{
		// TODO Auto-generated method stub
		return "[=-]$";
	}
	@Override
	public String getSecondPartHyphenationRegex()
	{
		// TODO Auto-generated method stub
		return "^[=-]";
	}
}
