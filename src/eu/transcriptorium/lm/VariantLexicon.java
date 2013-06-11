package eu.transcriptorium.lm;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import eu.transcriptorium.util.*;

/**
 * 
 * @author does
 *<pre>
 *"AGULLA"    [agulla]  0.947368           a g u l l a @
 *"AGULLA"    [Agulla]  0.0526316           A g u l l a @
 * </pre>
 */
public class VariantLexicon 
{
	private HashMap<String,List<Variant>> variantMap = new  HashMap<String,List<Variant>>();
	private String unknownWordSymbol = "<unk>";

	public static class Variant
	{
		String variantForm;
		String normalForm;
		double probability; // relative frequency of variantForm in normalForm
		List<String> composingCharacters;
		public String toString()
		{
			return "{" + normalForm + "->" + variantForm + " (" + probability + ") " + composingCharacters + "}"; 
		}
	}

	public String getNormalizedWordform(String wordform)
	{
		List<Variant> v = variantMap.get(wordform);
		if (v != null && v.size() > 0) 
			return v.get(0).normalForm; 
		else 
			return unknownWordSymbol;
	}

	public Variant getFirstVariant(String wordform)
	{
		List<Variant> v = variantMap.get(wordform);
		if (v != null && v.size() > 0) 
			return v.get(0);
		return null;
	}
	public void loadFromFile(String fileName)
	{
		try
		{
			BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String s;
			while ( (s = b.readLine()) != null)
			{
				//System.err.println(s);
				String[] parts = s.split("\\s+");
				Variant v = new Variant();
				v.normalForm = parts[0];
				v.variantForm = parts[1];
				if (parts.length > 2)
				{
					v.probability = Double.parseDouble(parts[2]);
				} else
					v.probability = 1;
				if (parts.length > 3)
					v.composingCharacters = Arrays.asList(Arrays.copyOfRange(parts, 3, parts.length));	
				List<Variant> vList = variantMap.get(v.variantForm);
				if (vList == null)
					vList = new ArrayList<Variant>();
				vList.add(v);
				variantMap.put(v.variantForm, vList);
				System.err.println(v);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public double getLogRealizationProbability(String normalized, String actual)
	{
		Variant v = getFirstVariant(actual);
		if (v != null)
		{
			return Functions.log2(v.probability);
		}
		return 0.0;
	}

	public static void main(String[] args)
	{
		VariantLexicon nl = new VariantLexicon();
		nl.loadFromFile(args[0]);
	}

	public List<Variant> getVariantsWithWordform(String nextSymbol) 
	{
		// TODO Auto-generated method stub
		return variantMap.get(nextSymbol);
	}
}
