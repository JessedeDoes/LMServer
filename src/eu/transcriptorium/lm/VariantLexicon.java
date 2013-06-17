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
	private HashMap<String,List<Variant>> variantFormF2VariantsMap = new  HashMap<String,List<Variant>>();
	private HashMap<String,List<Variant>> normalFormF2VariantsMap= new  HashMap<String,List<Variant>>();
	
	private Map<Variant,Variant> contents = new HashMap<Variant,Variant>();
	
	private String unknownWordSymbol = "<unk>";

	public Set<Variant> variantSet()
	{
		return this.contents.keySet();
	}
	
	public Set<String> getNormalizedForms()
	{
		return normalFormF2VariantsMap.keySet();
	}
	
	public List<Variant> getVariantsFromNormalForm(String n)
	{
		return this.normalFormF2VariantsMap.get(n);
	}
	
	public static class Variant
	{
		String variantForm;
		String normalForm;
		double probability = 0; // relative frequency of variantForm in normalForm
		int absoluteFrequency=0;
		
		List<String> composingCharacters;
		
		public Variant(String variantForm, String normalForm) 
		{
			this.variantForm = variantForm;
			this.normalForm = normalForm;
			String[] parts = variantForm.split("");
			composingCharacters = new ArrayList<String>();
			for (String p: parts)
			{
				if (p.length() > 0)
					composingCharacters.add(p);
			}
			composingCharacters.add("@");
			// TODO Auto-generated constructor stub
		}
		
		public Variant() 
		{
			// TODO Auto-generated constructor stub
		}

		public String toString()
		{
			return "{" + normalForm + "/" + variantForm + " : " + probability + "}"; 
		}
		/**This looks as follows:
		 * *<pre>
 *"AGULLA"    [agulla]  0.947368           a g u l l a @
 *"AGULLA"    [Agulla]  0.0526316           A g u l l a @
 * </pre>
		 * @return
		 */
		public String toStringUPV()
		{
			return String.format("\"%s\"\t[%s]\t%f\t%s", normalForm,variantForm,probability, Functions.join(composingCharacters," "));
		}
		
		public boolean equals(Object o)
		{
			Variant v1 = (Variant) o;
			return v1.normalForm.equals(this.normalForm) && v1.variantForm.equals(this.variantForm);
		}
		
		public int hashCode()
		{
			return this.variantForm.hashCode() * this.normalForm.hashCode();
		}
	}

	public Variant lookupVariant(String variantForm, String normalForm)
	{ 
		Variant v = new Variant(variantForm,normalForm);
		return contents.get(v);
	}

	public Variant lookupOrCreateVariant(String variantForm, String normalForm)
	{ 
		Variant v = new Variant(variantForm,normalForm);
		Variant v1 = contents.get(v);
		if (v1 != null)
			return v1;
		else
		{
			addVariant(v);
			return v;
		}
	}
	
	public String getNormalizedWordform(String wordform)
	{
		List<Variant> v = variantFormF2VariantsMap.get(wordform);
		if (v != null && v.size() > 0) 
			return v.get(0).normalForm; 
		else 
			return unknownWordSymbol;
	}

	public Variant getFirstVariant(String wordform)
	{
		List<Variant> v = variantFormF2VariantsMap.get(wordform);
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
				
				Variant v = new Variant(parts[1],parts[0]);
				
				if (parts.length > 2)
				{
					v.probability = Double.parseDouble(parts[2]);
				} else
					v.probability = 1;
				if (parts.length > 3)
					v.composingCharacters = Arrays.asList(Arrays.copyOfRange(parts, 3, parts.length));	
				addVariant(v);
				//System.err.println(v);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void addVariant(Variant v) 
	{
		List<Variant> vList = variantFormF2VariantsMap.get(v.variantForm);
		if (vList == null)
			vList = new ArrayList<Variant>();
		vList.add(v);
		variantFormF2VariantsMap.put(v.variantForm, vList);
		
		vList = normalFormF2VariantsMap.get(v.normalForm);
		if (vList == null)
			vList = new ArrayList<Variant>();
		vList.add(v);
		normalFormF2VariantsMap.put(v.normalForm, vList);
		contents.put(v, v);
	}

	public void computeRelativeProbabilities()
	{
		for (String n: this.normalFormF2VariantsMap.keySet())
		{
			List<Variant> l = normalFormF2VariantsMap.get(n);
			double T = 0;
			for (Variant v: l) 
				T+= v.absoluteFrequency;
			for (Variant v: l) 
				v.probability = v.absoluteFrequency / T; 
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
		return variantFormF2VariantsMap.get(nextSymbol);
	}
}
