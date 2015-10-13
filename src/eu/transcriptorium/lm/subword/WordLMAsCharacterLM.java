package eu.transcriptorium.lm.subword;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;

public class WordLMAsCharacterLM
{
	
	static NgramLanguageModel readLM(String fileName)
	{
		// languageModel = null;
		if (fileName != null)
		{
			if (!fileName.endsWith(".bin"))
				return  LmReaders.readArrayEncodedLmFromArpa(fileName,false);
			else
				return LmReaders.readLmBinary(fileName);

		} else
			return null;
	}
	
	private NgramLanguageModel<String> wordLM = null;
	
	
	
	
}
