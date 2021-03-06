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
import eu.transcriptorium.page.TEITextDecoder.type;
import eu.transcriptorium.util.StringUtils;
import eu.transcriptorium.util.*;

import java.util.*;
import java.io.*;

public class ExtractText
{
	private CharacterSet  characterSet = new DutchArtesTokenization();
	private XMLTextDecoder xmlStripper = new TEITextDecoder(type.BOTH);
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

		String text = getXmlStripper().decodeXML(txt);

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

				for (String x: models) // TODO what happens with "." in the label file?
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

	/**
	 * 
	 * @param dirName (directory where the text lines extracted by page-tool are)
	 * @param trainingPartitionFilename (list of training pages)
	 * @param trainingLinesFile (output: list of lines in the training partition)
	 * @param textFilename (contains the same as the training lines, but in one text file)
	 * @param labelFilename (output:  HTK MLF label file for training HTR)
	 */
	public void printLabelFileFromDirectoryWithLineTranscriptions(String dirName, 
			String trainingPartitionFilename, String trainingLinesFile, 
			String textFilename, 
			String labelFilename)
	{
		try 
		{
			PrintWriter labelFileWriter = new PrintWriter(new FileWriter(labelFilename));
			PrintWriter textFileWriter = new PrintWriter(new FileWriter(textFilename));
			PrintWriter trainingLinesWriter = new PrintWriter(new FileWriter(trainingLinesFile));
			List<String> trainingPartition = null;
			if (trainingPartitionFilename != null) 
			{
				trainingPartition = StringUtils.readStringList(trainingPartitionFilename);
				System.err.println(trainingPartition);
			}
			File f = new File(dirName);
			String[] entries = f.list();
			Arrays.sort(entries);
			startLabelFile(labelFileWriter);
			for (String fn: entries)
			{
				boolean isPartOfSet = true;
				if (trainingPartition != null)
				{
					isPartOfSet = false;
					for (String s: trainingPartition)
						if (fn.contains(s))
						{
							isPartOfSet = true;
							trainingLinesWriter.println(fn);
						}
				}
				if (!isPartOfSet)
					continue;
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
			trainingLinesWriter.close();
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
				String t = getXmlStripper().decodeXML(s);
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
				String t = getCharacterSet().cleanLine(getXmlStripper().decodeXML(s));
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
								out.println(getXmlStripper().decodeXML(to.getText()));
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

	public void printHMMList(String fn, String charsetFileName, int numStates)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(fn));
			List<String> models = modelNameCounter.keyList();
			Collections.sort(models);
			models.add(characterSet.getSentenceStart());
			models.add(characterSet.getSentenceEnd());
			
			for (String m: models)
			{
				out.println(m + "\t" + numStates); // modelNameCounter.get(m));
			}
			out.close();

			if (charsetFileName != null)
			{
				out = new PrintWriter(new FileWriter(charsetFileName));
				Map<Character,String> m = characterSet.getSpecialCharacterModelNameMap();
				for (Character c: m.keySet())
				{
					String n = m.get(c);
					out.println(c + "\t" + n);
					modelNameCounter.remove(n);
				}
				models = modelNameCounter.keyList();
				Collections.sort(models);
				for (String x: models)
				{
					out.println(x); // modelNameCounter.get(m));
				}
				
				out.close();
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
		String dir = args[0].replaceAll("/[^/]*$", "");
		new ExtractText().printTextLinesFromDirectory(dir, "./Test/testLines");
		ExtractText et = new ExtractText();
		et.printLabelFileFromDirectory(dir, "./Test/test.mlf");
		et.printHMMList("./Test/test.stats",null, 6);
	}

	public CharacterSet getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(CharacterSet characterSet) {
		this.characterSet = characterSet;
	}

	public XMLTextDecoder getXmlStripper() {
		return xmlStripper;
	}

	public void setXmlStripper(XMLTextDecoder xmlStripper) {
		this.xmlStripper = xmlStripper;
	}
}
