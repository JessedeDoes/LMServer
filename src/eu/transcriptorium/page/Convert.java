package eu.transcriptorium.page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.layout.PageLayout;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import  org.primaresearch.dla.page.*;

/**
 * Dit werkt niet! (althans niet voor abbyy xml)
 * @author does
 *
 */
public class Convert
{
	public static void convert(String fileName, String out)
	{
		try
		{
			Page page = PageXmlInputOutput.readPage(fileName);
			System.err.println(page.getImageFilename());
			PageXmlInputOutput.writePage(page, out); // does nothing????
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	} 
	
	public static void main(String[] args)
	{
		Convert.convert(args[0], args[1]);
	}
}
