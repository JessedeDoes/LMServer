package eu.transcriptorium.page;

import org.w3c.dom.*;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.*;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;

public class LineFromBaseLine implements SimpleInputOutputProcess
{
	String testFile = "N:/transcriptorium/NederlandseData/HattemC5_001_572/GroundTruthBasilis/156730078.xml";
	
	public void test()
	{
		addShapes(testFile);
	}
	
	public void addShapes(String fileName)
	{
		try
		{
			Document d = XML.parse(fileName);
			
			addLineShapes(d);
			//System.out.println(XML.documentToString(d));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void addLineShapes(Document d)
	{
		Element root = d.getDocumentElement();
	
		List<Double> ys = new ArrayList<Double>();

		for (Element e: XML.getElementsByTagname(root, "TextLine", false))
		{
			//<Baseline points="464,257 1475,255"/>
			Element b = XML.getElementByTagname(e, "Baseline");
			List<Point> l = getPoints(b);
			ys.add(averageY(l));
		}
		Collections.sort(ys);
		double lineSkip = averageDifference(ys);
		double descender = lineSkip / 4.0;
		System.err.println("line skip: " + lineSkip + " " + ys);
		for (Element e: XML.getElementsByTagname(root, "TextLine", false))
		{
			Element c = XML.getElementByTagname(e, "Coords");
			Element b = XML.getElementByTagname(e, "Baseline");
			
			List<Point> l = getPoints(b);
			List<Point> upperPoints = new ArrayList<Point>();
			List<Point> lowerPoints = new ArrayList<Point>();
			
			for (Point p: l)
			{
				Point upper = new Point();
				Point lower = new Point();
				upper.x = lower.x = p.x;
				upper.y = (int)  Math.round(p.y  - lineSkip + descender);
				lower.y = (int) Math.round(upper.y + lineSkip);
				upperPoints.add(upper);
				lowerPoints.add(lower);
			}
			
			List<Point> lineShape = new ArrayList<Point>();
			
			for (Point p: lowerPoints)
			{
				lineShape.add(p);
			}
			
			for (int i=upperPoints.size()-1; i >= 0; i--)
			{
				lineShape.add(upperPoints.get(i));
			}
			String z = lineShape.get(0).x + "," + lineShape.get(0).y;
			for (int i=1; i < lineShape.size(); i++)
			{
				z += " " +  lineShape.get(i).x + "," + lineShape.get(i).y;
			}
			System.err.println("line shape points:" + z);
			c.setAttribute("points",z);
			// lineShape.add(lowerPoints.get(0)); // ?
		}
	}

	List<Point> getPoints(Element b)
	{
		String pp = b.getAttribute("points");
		List<Point> l = new ArrayList<Point>();

		for (String z: pp.split("\\s+" ))
		{
			String [] xy = z.split(",");
			Point p = new Point();
			p.x = Integer.parseInt(xy[0]);
			p.y = Integer.parseInt(xy[1]);
			l.add(p);
		}
		return l;
	}
	
	double averageDifference(List<Double> l)
	{
		double d = 0;
		for (int i=1; i < l.size(); i++)
			d += l.get(i) - l.get(i-1);
		System.err.println("d=" + d);
		return d / (double) l .size();
	}

	double averageY(List<Point> l)
	{
		double y = 0;
		for (Point p: l)
			y += p.y;
		return y / (double) l.size();
	}
	
	public void handleFile(String in, String out) 
	{
		Document d = null;

			try 
			{
				d = XML.parse(in);
				addLineShapes(d);
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			//tagDocument(d);
			//postProcess(d);
			String doc = XML.documentToString(d);
			doc = doc.replaceAll("pagecontent.2010-03-19", "pagecontent/2013-07-15");
			pout.print(doc);
			pout.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void setProperties(Properties arg0)
	{
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args)
	{
		LineFromBaseLine lfbl = new LineFromBaseLine();
		//lfbl.test();
		DirectoryHandling.tagAllFilesInDirectory(lfbl, "N:/transcriptorium/NederlandseData/HattemC5_001_572/GroundTruthBasilis", "N:/transcriptorium/NederlandseData/HattemC5_001_572/GroundTruthWithLines");
	}
}
