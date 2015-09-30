package eu.transcriptorium.page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.layout.PageLayout;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.ident.Id;
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

	public String getPageId(String fileName, Page page)
	{
		Id id = page.getGtsId();
		if (id != null)
			return id.toString();
		String imageFileName = page.getImageFilename();
		if (imageFileName != null)
			return imageFileName;
		return fileName;
	}
	
	public String getLineId(String pageId, String regionId, String lineId)
	{
		return pageId + ":" + lineId;
	}
	
	public  void printLabels(String fileName, PrintWriter out)
	{
		characterSet.setAcceptAll();
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			String pageId = getPageId(fileName,page);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted(); // by what???

			for (Region r: regions)
			{
				ContentType type = r.getType();
				Class c = r.getClass();

				

				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					String regionId = tr.getId().toString();

					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();

					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							String labelId = getLineId(pageId, regionId, to.getId().toString());
							out.println(to.getText());
							out.println("\"*/" + labelId + ".lab\"");
							out.println(characterSet.getLineStartSymbol());
							String text = xmlStripper.decodeXML(to.getText());
							for (String w: text.split("\\s+"))
							{
								String cleanedWord = characterSet.cleanWord(w);
								for (String tok: cleanedWord.split("\\s+"))
								{
									String normalizedWord = characterSet.normalize(tok);
									String[] models = characterSet.wordToModelNames(tok);

									//out.println(w + " | "  + tok + " | " + normalizedWord + " | " + StringUtils.join(models, " "));

									for (String x: models)
									{
										out.println(x);
									}

								}
							}
							out.println(characterSet.getLineEndSymbol());
							out.println(".");
							
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

	public void printTextLinesFromDirectory(String dirName, String toDirName)
	{
		File f = new File(dirName);
		String[] entries = f.list();
		for (String fn: entries)
		{
			printTextLines(dirName + "/" + fn, toDirName);
		}
	}
	
	public void printTextLines(String fileName, String directoryName)
	{
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			String pageId = getPageId(fileName,page);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted(); // by what???
			
			for (Region r: regions)
			{
				ContentType type = r.getType();

				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					String regionId = tr.getId().toString();
					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();
					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							String labelId = getLineId(pageId, regionId, to.getId().toString());
							File lineFile = new File(directoryName + "/" + labelId + ".txt");
							try
							{
								lineFile.createNewFile();
								PrintWriter out = new PrintWriter(new FileWriter(lineFile));
								out.println(xmlStripper.decodeXML(to.getText()));
								out.close();
							} catch (Exception e)
							{
								System.err.println(lineFile);
							}
						} else
						{
							System.err.println("This is not a line:  " + to.getId() + ": " + to.getText());
						}
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Deprecated
	public  void printText(String fileName, PrintWriter out) // no: print lines in separate directories ...
	{
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			String pageId = getPageId(fileName,page);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted(); // by what???

			for (Region r: regions)
			{
				ContentType type = r.getType();
				Class c = r.getClass();

				
				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					String regionId = tr.getId().toString();
					

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
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new ExtractText().printTextLinesFromDirectory(args[0].replaceAll("/[^/]*$", ""), "./Test/testLines");
	}
}
