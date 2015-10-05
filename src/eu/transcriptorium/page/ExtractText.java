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
import eu.transcriptorium.util.*;

import java.util.*;
import java.io.*;

public class ExtractText
{
	private CharacterSet  characterSet = new DutchArtesTokenization();
	XMLTextDecoder xmlStripper = new TEITextDecoder();
	Counter<String> modelNameCounter = new Counter<String>();

	public  void printText(String fileName)
	{
		PrintWriter sout = new PrintWriter(new OutputStreamWriter(System.out));
		printText(fileName, sout);
		sout.close();
	}

	public  void printLabels(String fileName)
	{
		PrintWriter sout = new PrintWriter(new OutputStreamWriter(System.out));
		printLabelsFromPAGEXML(fileName, sout);
		sout.close();
	}

	public String getPageId(String fileName, Page page)
	{
		Id id = page.getGtsId();
		if (id != null)
			return id.toString();
		String imageFileName = page.getImageFilename();
		if (imageFileName != null)
		{
			imageFileName = imageFileName.replaceAll("\\.(jpg|JPG|png|PNG|tif|TIF|tiff|TIFF)$", "");
			return imageFileName;
		}
		return fileName;
	}

	public String getLineId(String pageId, String regionId, String lineId)
	{
		return pageId + "." + lineId;
	}

	public void startLabelFile(PrintWriter out)
	{
		out.println("#!MLF!#");
	}

	// this prints from XML
	public  void printLabelsFromPAGEXML(String fileName, PrintWriter out)
	{
		getCharacterSet().setAcceptAll();
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
							//out.println(to.getText());
							String txt = to.getText();
							
							printLabelsForLine(null, out, labelId, txt);

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

	private void printLabelsForLine(PrintWriter textFileWriter, PrintWriter labelFileWriter, String labelId, String txt) {
		labelFileWriter.println("\"*/" + labelId + ".lab\"");
		labelFileWriter.println(getCharacterSet().getLineStartSymbol());

		String text = xmlStripper.decodeXML(txt);
		
		if (textFileWriter != null)
		{
			textFileWriter.println(text);
		}
		
		for (String w: text.split("\\s+"))
		{
			String cleanedWord = getCharacterSet().cleanWord(w);
			for (String tok: cleanedWord.split("\\s+"))
			{
				String normalizedWord = getCharacterSet().normalize(tok);
				String[] models = getCharacterSet().wordToModelNames(tok);

				// out.println(w + " | "  + tok + " | " + normalizedWord + " | " + StringUtils.join(models, " "));

				for (String x: models)
				{
					labelFileWriter.println(x);
					modelNameCounter.increment(x);
				}

			}
		}
		labelFileWriter.println(getCharacterSet().getLineEndSymbol());
		labelFileWriter.println(".");
	}

	public void printLabelFileFromDirectory(String dirName, PrintWriter out)
	{
		File f = new File(dirName);
		String[] entries = f.list();
		startLabelFile(out);
		for (String fn: entries)
		{
			printLabelsFromPAGEXML(dirName + "/" + fn, out);
		}
	}

	public void printLabelFileFromDirectory(String dirName, String outFile)
	{
		try 
		{
			PrintWriter out = new PrintWriter(new FileWriter(outFile));
			printLabelFileFromDirectory(dirName, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printLabelFileFromDirectoryWithLineTranscriptions(String dirName, String textFilename, 
			String labelFilename)
	{
		try 
		{
			PrintWriter labelFileWriter = new PrintWriter(new FileWriter(labelFilename));
			PrintWriter textFileWriter = new PrintWriter(new FileWriter(textFilename));
			File f = new File(dirName);
			String[] entries = f.list();
			startLabelFile(labelFileWriter);
			for (String fn: entries)
			{
				BufferedReader b = new BufferedReader(new FileReader(dirName + "/"  + fn));
				String l;
				fn = fn.replaceAll(".txt$", "");
				while ((l = b.readLine()) != null)
				{
					this.printLabelsForLine(textFileWriter, labelFileWriter, fn, l);
				}
			}
			labelFileWriter.close();
			textFileWriter.close();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stripXMLFromTextLines(String inDirName, String outDirName) throws IOException
	{
		File f = new File(inDirName);
		String[] entries = f.list();
		for (String fn: entries)
		{
			BufferedReader r = new BufferedReader(new FileReader(inDirName + "/"  + fn));
			PrintWriter out = new PrintWriter(new FileWriter(outDirName + "/" + fn));
			String s;
			while ((s = r.readLine()) != null)
			{
				String t = xmlStripper.decodeXML(s);
				out.println(t);
			}
			out.close();
			//printTextLines(dirName + "/" + fn, toDirName);
		}
	}
	
	public void stripXMLFromTextLinesAndClean(String inDirName, String outDirName) throws IOException
	{
		File f = new File(inDirName);
		String[] entries = f.list();
		for (String fn: entries)
		{
			BufferedReader r = new BufferedReader(new FileReader(inDirName + "/"  + fn));
			PrintWriter out = new PrintWriter(new FileWriter(outDirName + "/" + fn));
			String s;
			while ((s = r.readLine()) != null)
			{
				String t = getCharacterSet().cleanLine(xmlStripper.decodeXML(s));
				out.println(t);
			}
			out.close();
			//printTextLines(dirName + "/" + fn, toDirName);
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

	public void printStatistics(String fn)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(fn));
			List<String> models = modelNameCounter.keyList();
			Collections.sort(models);
			for (String m: models)
			{
				out.println(m + "\t" + modelNameCounter.get(m));
			}
			out.close();
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
		String dir = args[0].replaceAll("/[^/]*$", "");
		new ExtractText().printTextLinesFromDirectory(dir, "./Test/testLines");
		ExtractText et = new ExtractText();
		et.printLabelFileFromDirectory(dir, "./Test/test.mlf");
		et.printStatistics("./Test/test.stats");
	}

	public CharacterSet getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(CharacterSet characterSet) {
		this.characterSet = characterSet;
	}
}
