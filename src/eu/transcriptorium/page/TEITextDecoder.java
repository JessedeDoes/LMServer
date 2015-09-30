package eu.transcriptorium.page;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.*;
import eu.transcriptorium.util.XML;
import eu.transcriptorium.util.XML.NodeAction;

public class TEITextDecoder implements XMLTextDecoder
{
	String extractedText = "";

	enum type
	{
		EXPANSIONS,
		ABBREVIATIONS,
		BOTH
	}  ;

	type extractionType = type.BOTH;

	public boolean  extractText(Node n)
	{
		// TODO Auto-generated method stub
		if (n instanceof org.w3c.dom.Text)
		{
			Text t = (Text) n;
			extractedText += t.getTextContent();
		} else if  (n instanceof org.w3c.dom.Element)
		{
			Element e = (Element) n;
			String tagName = e.getLocalName();
			if (tagName.equals("choice") || tagName.equals("choose"))
			{
				String exp  = "";
				String abr = "" ;
				Element expan = XML.getElementByTagname(e, "expan");
				if (expan != null)
					exp = expan.getTextContent();
				Element abbr = XML.getElementByTagname(e, "abbrev");
				if (abbr != null)
					abr = abbr.getTextContent();
				else 
				{
					for (Node c: XML.getChildren(e))
					{
						if (c instanceof org.w3c.dom.Text)
							abr += c.getTextContent();
					}
				}
				switch (extractionType)
				{
				case BOTH:
					extractedText +=  "<" + abr + ":"  + exp + ">";
					break;
				case EXPANSIONS:
					extractedText += expan;
					break;
				case ABBREVIATIONS:
					extractedText += abr;
					break;
				}
			} else
			{
				for (Node c: XML.getChildren(n)) extractText(c);
			}
		}
		return false;
	}

	@Override
	public String decodeXML(String taggedString)
	{
		// TODO Auto-generated method stub
		extractedText = "";
		String plus = "<root>"  + taggedString + "</root>";
		try
		{
			Document d = XML.parseString(plus);
			extractText(d.getDocumentElement());
		} catch (Exception e)
		{
			System.err.println("error extracting text from " + taggedString);
			//e.printStackTrace();
		}
		return extractedText;
	}

	public static void main(String[] args)
	{
		TEITextDecoder ttd = new TEITextDecoder();
		String  s= "aap noot <choose><abbr>abr</abbr><expan>abbreviation</expan></choose> mies <choose>a<expan>b</expan></choose>";
		System.out.println(s + " ---> " + ttd.decodeXML(s));
	}
}
