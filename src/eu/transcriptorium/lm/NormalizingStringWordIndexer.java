package eu.transcriptorium.lm;

import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.Indexer;

/**
 * Implementation of a WordIndexer in which words are represented as strings.
 * 
 * @author adampauls
 * 
 */
public class NormalizingStringWordIndexer implements WordIndexer<String>
{
	VariantLexicon lexicon;
	StringNormalizer normalizer = new TrivialStringNormalizer();
	/**
	 * 
	 */
	
	public NormalizingStringWordIndexer(VariantLexicon lexicon)
	{
		this.lexicon = lexicon;
		sparseIndexer = new Indexer<String>();
	}
	
	public String normalize(String s)
	{
		if (s.equals(this.getStartSymbol()))
			return this.getStartSymbol();
		if (s.equals(this.getEndSymbol()))
			return this.getEndSymbol();
		return normalizer.getNormalizedForm(null, null, s);
	}
	
	private static final long serialVersionUID = 1L;

	private final Indexer<String> sparseIndexer;

	private String startSymbol;

	private String endSymbol;

	private String unkSymbol;

	private int unkIndex = -1;

	public NormalizingStringWordIndexer() 
	{
		sparseIndexer = new Indexer<String>();
	}

	@Override
	public int getOrAddIndex(final String word) 
	{
		return sparseIndexer.getIndex(normalize(word));
	}

	@Override
	public String getWord(final int index) 
	{
		return sparseIndexer.getObject(index);
	}

	@Override
	public int numWords() 
	{
		return sparseIndexer.size();
	}

	@Override
	public String getStartSymbol() 
	{
		return startSymbol;
	}

	@Override
	public String getEndSymbol() {
		return endSymbol;
	}

	@Override
	public String getUnkSymbol() {
		return unkSymbol;
	}

	@Override
	public int getOrAddIndexFromString(final String word) 
	{
		return getOrAddIndex(word);
	}

	@Override
	public void setStartSymbol(final String sym) 
	{
		startSymbol = sym;
		sparseIndexer.add(sym);

	}

	@Override
	public void setEndSymbol(final String sym) 
	{
		endSymbol = sym;
		sparseIndexer.add(sym);
	}

	@Override
	public void setUnkSymbol(final String sym) 
	{
		unkSymbol = sym;
		unkIndex = sparseIndexer.getIndex(sym);
	}

	@Override
	public void trimAndLock() 
	{
		sparseIndexer.trim();
		sparseIndexer.lock();
	}
	
	@Override
	public int getIndexPossiblyUnk(final String word) 
	{
		final int id = sparseIndexer.indexOf(normalize(word));
		return id < 0 ? unkIndex : id;
	}
}
