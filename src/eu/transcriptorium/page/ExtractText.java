package eu.transcriptorium.page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.layout.PageLayout;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import  org.primaresearch.dla.page.*;

import eu.transcriptorium.lm.CharacterSet;
import eu.transcriptorium.lm.charsets.DutchArtesTokenization;
import eu.transcriptorium.util.StringUtils;

import java.util.*;
import java.io.*;

public class ExtractText
{
	CharacterSet  characterSet = new DutchArtesTokenization();
	XMLTextDecoder xmlStripper = new TEITextDecoder();
	
	public  void printText(String fileName)
	{
		PrintWriter sout = new PrintWriter(new OutputStreamWriter(System.out));
		printText(fileName, sout);
		sout.close();
	}
	
	public  void printLabels(String fileName)
	{
		PrintWriter sout = new PrintWriter(new OutputStreamWriter(System.out));
		printLabels(fileName, sout);
		sout.close();
	}
	public  void printLabels(String fileName, PrintWriter out)
	{
		characterSet.setAcceptAll();
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted(); // by what???
			
			for (Region r: regions)
			{
				ContentType type = r.getType();
				Class c = r.getClass();
				
				//out.println(r.getId() + ":" + c.getName()  + ":" + r.getRegionCount());
			
				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					String regionId = tr.getId().toString();
					
					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();
					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							String labelId = to.getId().toString();
							out.println("\"*/" + labelId + ".lab\"");
							out.println(characterSet.getLineStartSymbol());
							String text = xmlStripper.decodeXML(to.getText());
							for (String w: text.split("\\s+"))
							{
								String cleanedWord = characterSet.cleanWord(w);
								String normalizedWord = characterSet.normalize(cleanedWord);
								String[] models = characterSet.wordToModelNames(cleanedWord);
								
								for (String x: models)
								{
									out.println(x);
								}
								//System.err.println(w + " | "  + cleanedWord + " | " + normalizedWord + " | " + StringUtils.join(models, " "));
							}
							out.println(characterSet.getLineEndSymbol());
							out.println(".");
							//System.err.println(to.getText());
							//out.println(to.getId() + ": " + text);
						} else
						{
							System.err.println("This is not a line:  " + to.getId() + ": " + to.getText());
						}
					}
				}
				//if (type == ContentType.)
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public  void printText(String fileName, PrintWriter out)
	{
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted(); // by what???
			
			for (Region r: regions)
			{
				ContentType type = r.getType();
				Class c = r.getClass();
				
				//out.println(r.getId() + ":" + c.getName()  + ":" + r.getRegionCount());
			
				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					String regionId = tr.getId().toString();
					//tr.ge
					//tr.get
					
					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();
					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							out.println(to.getId() + ": " + to.getText());
						} else
						{
							System.err.println("This is not a line:  " + to.getId() + ": " + to.getText());
						}
					}
				}
				//if (type == ContentType.)
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new ExtractText().printLabels(args[0]);
	}
}
