package eu.transcriptorium.lm;

public class SimpleStringNormalizer implements StringNormalizer 
{

	@Override
	public String getNormalizedForm(String lemma, String tag, String wordform) 
	{
		return wordform.toLowerCase().trim();
	}
}
