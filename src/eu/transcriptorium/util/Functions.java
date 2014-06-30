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
		for (String p: parts)
		{
			String[] av = p.split("=");
			if (av.length > 1)
				m.put(av[0], av[1]);
		}
		return m;
	}
}
