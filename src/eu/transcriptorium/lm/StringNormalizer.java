package eu.transcriptorium.lm;

public interface StringNormalizer 
{
	public String getNormalizedForm(String lemma, String tag, String wordform);
}
