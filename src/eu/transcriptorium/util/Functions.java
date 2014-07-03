package eu.transcriptorium.util;

import java.util.*;

public class Functions 
{
	public static double log2(double x)
	{
		return Math.log(x) / Math.log(2.0); 
	}
	
	static public String join(List<String> list, String conjunction)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list)
		{
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}
	
	public static Map<String,String> getAttributesFromLine(String l)
	{
		Map<String,String> m = new HashMap<String,String>();
		String[] parts = l.split("\\s+");
		for (String p: parts) // dit gaat mis voor == etc
		{
			int i = p.indexOf("=");
			if (i > 0)
			{
				String a = p.substring(0,i);
				String v = p.substring(i+1);
				m.put(a,v);
			}
		}
		return m;
	}
}
