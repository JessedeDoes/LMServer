package eu.transcriptorium.page;

import eu.transcriptorium.util.XML;
import eu.transcriptorium.util.XML.NodeAction;

import org.w3c.dom.*;

import java.util.*;

public class OffsetTagging
{
	// ahem? is this useful at all?
	String taggedLine;
	Set<Tag> tags  = new HashSet<Tag>();
	
	private NodeAction action = new NodeAction () 
	{
		@Override
		public boolean action(Node n)
		{
			// TODO Auto-generated method stub
			return false;
		} };

		static class Tag
		{
			int offset;
			int length;
			String name;
			
			Map<String,String> properties = new HashMap<String,String>() ;

			public Tag()
			{

			}
		}

		public void convertToOffsetTagging(String taggedString)
		{
			String plus = "<root>"  + taggedString + "</root>";
			Document d = XML.parseString(taggedString);
			XML.preorder(d, action );
		}
}
