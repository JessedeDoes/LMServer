package eu.transcriptorium.repository;

import java.util.Properties;
import java.io.*;

import eu.transcriptorium.util.Counter;
import java.util.*;

public class DistanceTest implements Repository.ItemTest
{
    int referenceId = -1;
	
    public Map<String,Double> makeRelativeFrequencyList(InputStream i) throws IOException
    {
    	Counter<String> c = new Counter<String>();
    	BufferedReader r = new BufferedReader(new InputStreamReader(i));
    	String l;
    	int nTokens = 0;
    	while ((l = r.readLine()) != null)
    	{
    		// HM xml eraf slopen zo nodig?
    		for (String t: l.split("\\s+"))
    		{
    			c.increment(t);
    			nTokens++;
    		}
    	}
    	Map<String,Double> m = new HashMap<String,Double>();
    	for (String k: c.keyList())
    	{
    		double d = c.get(k);
    		m.put(k,d);
    	}
    	return m;
    }
    
    public static double cosineSimilarity( Map<String,Double> a,  Map<String,Double> b) 
	{
		double dotProduct = 0.0;
		double aMagnitude = 0.0;
		double bMagnitude = 0.0;
		
		for (String k: a.keySet()) 
		{
			double aValue = a.get(k);
			Double bValue = b.get(k);
			if (bValue == null)
				bValue = new Double(0);
			aMagnitude += aValue * aValue;
			// bMagnitude += bValue * bValue;
			dotProduct += aValue * bValue;
		}
		
		for (String k: b.keySet()) 
		{
			double bValue = b.get(k);
			//aMagnitude += aValue * aValue;
			bMagnitude += bValue * bValue;
		}
		
		aMagnitude = Math.sqrt(aMagnitude);
		bMagnitude = Math.sqrt(bMagnitude);
		return (aMagnitude == 0 || bMagnitude == 0)
				? 0: dotProduct / (aMagnitude * bMagnitude);
	}
    
    public double distance(InputStream a1, InputStream a2) throws IOException
    {
    	return 1 - Math.sqrt(cosineSimilarity(makeRelativeFrequencyList(a1), makeRelativeFrequencyList(a2)));
    }
    
	public DistanceTest(int i)
	{
		this.referenceId = i;
	}
	@Override
	public boolean test(Repository r, int id)
	{
		// TODO Auto-generated method stub
		Properties pRef = r.getMetadata(this.referenceId);
		Properties p = r.getMetadata(id);
		
		return false;
	}
}
