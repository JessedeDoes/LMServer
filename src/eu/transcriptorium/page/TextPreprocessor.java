package eu.transcriptorium.page;

import java.io.IOException;

public class TextPreprocessor 
{
	public static void main(String[] args) throws IOException
	{
		ExtractText t = new ExtractText();
		t.characterSet.setAcceptAll();
		t.stripXMLFromTextLinesAndClean(args[0], args[1]);
	}
}
