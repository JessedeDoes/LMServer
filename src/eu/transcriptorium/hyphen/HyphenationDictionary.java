package eu.transcriptorium.hyphen;
import java.util.*;

public interface HyphenationDictionary 
{
	public double getHyphenationProbability(String w, String p1, String p2);
	public List<String> getProlongations(String prefix); // this is useless
}
