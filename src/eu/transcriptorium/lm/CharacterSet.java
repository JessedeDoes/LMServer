package eu.transcriptorium.lm;

/**
 * 
 * @author does
 *A character set should contain all the info needed to build LM's for a collection
 *- how to handle white space
 *- how to produce label files for text, for training HTR
 *- how to output dictionary form of words
 *- how to normalize for the dictionary
 *- what  is the start of line character/model
 *- what is the end of line model 
 *- settings for tokenization
 */
public interface  CharacterSet
{
	public String[] wordToModelNames(String w); // possibly null if word totally refused
	public String cleanWord(String w); // may omit or replace some characters or return null
	public String unescapeWord(String w); // remove slashes etc added before language modeling
	public String normalize(String w); // often just uppercasing. mostly AFTER cleaning! (but not in plantas...)
	public void loadFromHMMList(String fileName);
	public void setAcceptAll();
}
