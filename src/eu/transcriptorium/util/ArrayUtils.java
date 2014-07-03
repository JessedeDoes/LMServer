package eu.transcriptorium.util;
import java.util.*;
public class ArrayUtils
{
	public static Object[] reverse(Object[] arr) 
	{
		List < Object > list = Arrays.asList(arr);
		Collections.reverse(list);
		return list.toArray();
	}
}

