package eu.transcriptorium.page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.layout.PageLayout;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import  org.primaresearch.dla.page.*;

import java.util.*;
import java.io.*;

public class ExtractText
{
	public static void printText(String fileName)
	{
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			PageLayout l = page.getLayout();
			List<Region> regions = l.getRegionsSorted();
			for (Region r: regions)
			{
				ContentType type = r.getType();
				Class c = r.getClass();
				System.out.println(r.getId() + ":" + c.getName()  + ":" + r.getRegionCount());
				if (r instanceof TextRegion)
				{
					TextRegion tr = (TextRegion) r;
					//tr.ge
					//tr.get
					
					List <LowLevelTextObject> textObjects = tr.getTextObjectsSorted();
					for (LowLevelTextObject to: textObjects)
					{
						if (to instanceof TextLine)
						{
							System.out.println(to.getId() + ": " + to.getText());
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
}
