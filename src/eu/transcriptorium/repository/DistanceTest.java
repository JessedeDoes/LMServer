package eu.transcriptorium.repository;

import java.util.Properties;
import java.io.*;

import eu.transcriptorium.repository.Repository.ItemProperty;
import eu.transcriptorium.util.Counter;
import java.util.*;

public class DistanceTest implements Repository.ItemTest, ItemProperty
{
    int referenceId = -1;
    Map<String,Double> reference = null;
    Repository repository;
    
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
    
    public static double cosineSimilarity(Map<String,Double> a,  Map<String,Double> b) 
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
    
    public double distance(Map<String,Double> a,  Map<String,Double> b) throws IOException
    {
    	double c = cosineSimilarity(a, b);
    	if (c > 1) c = 1;
    	return Math.sqrt(1 - c*c);
    }
    
	public DistanceTest(Repository r, Integer i)
	{
		this.repository = r;
		this.referenceId = i;
	}
	
	@Override
	
	public String getPropertyValue(Repository r, int id)
	{
		if (this.reference == null)
		{
			try {
				reference = makeRelativeFrequencyList(r.openFile(this.referenceId));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Double d = distance(reference, makeRelativeFrequencyList(r.openFile(id)));
			return d.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
