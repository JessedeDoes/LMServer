package eu.transcriptorium.util;

import java.util.List;

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
}
