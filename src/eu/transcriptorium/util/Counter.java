package eu.transcriptorium.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*
 * Faster way:
 * Hash all (frequent) strings to integers
 * (or just the first 100000 encountered)
 * (with really concurrent hash)
 */

public class Counter<T> extends ConcurrentHashMap<T,Integer>
{
	@Override
	public Integer get(Object key)
	{
		Integer z = super.get(key);
		if (z == null)
			return 0;
	
		return z;
	}
	
	public synchronized void increment(T key)
	{
		Integer z = get(key);
		super.put(key, z+1);
	}
	
	public synchronized void increment(T key, int amount)
	{
		Integer z = get(key);
		super.put(key, z+amount);
	}
	
	public class CompareCounts implements Comparator<T> 
	{
		public int compare(T a, T b) 
		{
			if (a == null || b == null)
				throw new NullPointerException();
			int cA = get(a);
			int cB = get(b);
			return (int) Math.signum(cB-cA);
		}
	}
	
	public List<T> keyList()
	{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		List<T> l = new ArrayList<T>();
		l.addAll(this.keySet());
		CompareCounts v =  new CompareCounts();
		Collections.sort(l, v);
		return l;
	}
	
	public int sumOfCounts()
	{
		int n=0;
		for (Integer i: this.values()) n +=i;
		return n;
	}	
	
	public static Counter<String> readFromFile(String fileName)
	{
		Counter<String> c = new Counter<String>();
		InputStreamReader is = null;
		BufferedReader in = null;
		try 
		{ 
			is = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			in = new BufferedReader(is);
			String line=null;
			while ((line=in.readLine()) != null)
			{
				String[] tf = line.split("\t");
				int f = tf.length > 1?Integer.parseInt(tf[1]): 1;
				c.increment(tf[0], f);
			}
		} catch (Exception e) { e.printStackTrace();};
		return c;
	}
}