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
	
	public  void printLabels(String fileName, PrintWriter out)
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
					
					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();
					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							String text = xmlStripper.decodeXML(to.getText());
							for (String w: text.split("\\s+"))
							{
								
							}
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
		new ExtractText().printText(args[0]);
	}
}
