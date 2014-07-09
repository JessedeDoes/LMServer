package eu.transcriptorium.util;
import java.util.*;
public class ArrayUtils<T>
{
	public static <T>  void reverse(T[] arr, int N) 
	{
		for (int i=0; i < N / 2; i++)
		{
			T swap;
			swap = arr[i];
			arr[i] = arr[N -i -1];
			arr[N -i -1] = swap;
		}
	}
}

