package eu.transcriptorium.hyphen;

import java.util.List;

public class SimpleHyphenationDictionary implements HyphenationDictionary 
{
	double p = 0.03;
	@Override
	public double getHyphenationProbability(String w, String p1, String p2) 
	{
		// TODO Auto-generated method stub
		if (p1.endsWith("-"))
		{
			String P1 = p1.replaceAll("-$", "");
			if (w.equals(P1 + p2))
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
		return null;
	}
	@Override
	public String getSecondPartHyphenationRegex()
	{
		// TODO Auto-generated method stub
		return "^=";
	}
}
