package eu.transcriptorium.lattice;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lm.VariantLexicon;

public class ParameterTester
{
	private  Map<String, List<String>> data;
	private ParagraphDecoder decoder  = new ParagraphDecoder();
	NgramLanguageModel  lm  = null;
	VariantLexicon v = null;
	
	public void testParameters(double lmscalemin, double lmscalemax, double lmstep, 
			double wdpenaltymin, double wdpenaltymax, double wdstep)
	{
		for (double l=lmscalemin; l <= lmscalemax; l+=lmstep)
			for (double w=wdpenaltymin; w <= wdpenaltymax; w+= wdstep)
			{
				System.err.println("Testing with l=" + l +  " w = "  + w);
				decoder.setLmscale(l);
				decoder.setWdpenalty(w);
				PrintWriter out;
				try
				{
					out = new PrintWriter(new FileWriter("Temp/test_" + l + "_" + w + ".decoded"));
					decoder.decodePerParagraph(lm, v, out, data);
					out.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
	}
	
	public static void main(String[] args)
	{
		NgramLanguageModel<String> lm = null;
		String languageModel =  args[0];
		String dir = null;
		
		ParameterTester p = new ParameterTester();
		// languageModel = null;
		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}
		VariantLexicon v = null;
		if (args.length > 2)
		{
			v = new VariantLexicon();
			v.loadFromFile(args[1]);
			dir = args[2];
		} 	else
		{
			dir = args[1];
		};
		p.lm = lm;
		p.v = v;
		p.data = ParagraphDecoder.makeParagraphMap(dir);
		p.testParameters(5, 30, 1, -10, 10, 1); // zou goed multithreaded kunnen....
		//decodeFilesInFolder(dir,lm, v, new PrintWriter(System.out));
	}
}
