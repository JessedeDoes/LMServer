package eu.transcriptorium.lm;

import eu.transcriptorium.util.StringUtils;

import java.util.List;
import java.util.Random;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.suggest.Suggest;

public class Sampler 
{
	public static void main(String[] args)
	{
		String languageModel = args[0];
		NgramLanguageModel lm = null;

		// languageModel = null;

		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
		}
		while(true)
		{
		 List<String> s = NgramLanguageModel.StaticMethods.sample(new Random(), lm);
		 System.out.println(StringUtils.join(s, " ").toLowerCase().replaceAll("\\\\",""));
		}
	}
}
