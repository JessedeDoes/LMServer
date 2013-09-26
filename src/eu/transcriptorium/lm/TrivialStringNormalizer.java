package eu.transcriptorium.lm;

public class TrivialStringNormalizer implements StringNormalizer 
{

	@Override
	public String getNormalizedForm(String lemma, String tag, String wordform) 
	{
		return wordform.trim();
	}
}
