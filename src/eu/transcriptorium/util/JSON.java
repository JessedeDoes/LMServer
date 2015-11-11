package eu.transcriptorium.util;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.util.*;

import org.apache.tools.ant.filters.StringInputStream;

public class JSON
{
	
	// this parser is too strict, properties need to be quoted...
	static public JsonObject fromString(String s)
	{
		if (s == null  || s.trim().length() == 0)
			return null;
		
		JsonReader reader = Json.createReader(new StringReader(s));
        
        JsonObject object  = reader.readObject();
       
        reader.close();
         return object;
	}
	
	static public Map<String,String>  toMap(JsonObject o)
	{
		if (o == null)
			return null;
		Map<String,String> m = new HashMap<String,String>();
		for (String s: o.keySet())
		{
		
			Object x = o.get(s);
			//System.err.println(s + " --> "  + x);
			m.put(s,x.toString());
		}
		return m;
	}
	
	
	static public Properties toProperties(JsonObject o)
	{
		if (o == null)
			return null;
		
		Properties m = new Properties();
		for (String s: o.keySet())
		{
			Object x = o.get(s);
			m.put(s,x.toString());
		}
		return m;
	}
	
	public static void main(String[] args)
	{
		 String s = 
		            "  {" +
		            "   \"name\": \"Jack\", " +
		            "   \"age\" : 13, " +
		            "   \"isMarried\" : false, " +
		            "   \"address\": { " +
		            "     \"street\": \"#1234, Main Street\", " +
		            "     \"zipCode\": \"123456\" " +
		            "   }, " +
		            "   \"phoneNumbers\": [\"011-111-1111\", \"11-111-1111\"] " +
		            " }";
		  System.err.println(toMap(fromString(s)));
	}
}
