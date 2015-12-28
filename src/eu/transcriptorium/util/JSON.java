package eu.transcriptorium.util;
import java.io.StringReader;
//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.JsonValue;
import java.util.*;
import java.util.Map.Entry;

import org.apache.tools.ant.filters.StringInputStream;

import com.google.gson.*;

public class JSON
{

	// this parser is too strict, properties need to be quoted...
	static public com.google.gson.JsonObject fromString(String s)
	{
		if (s == null  || s.trim().length() == 0)
			return null;

		com.google.gson.JsonParser parser = new 	com.google.gson.JsonParser();
		JsonElement e = parser.parse(s);
		return e.getAsJsonObject();

		/**
		JsonReader reader = Json.createReader(new StringReader(s));

        JsonObject object  = reader.readObject();

        reader.close();
         return object;
		 **/

	}

	static public JsonObject propertiesToJson(Properties p)
	{
		JsonObject o = new JsonObject();
		for (Object k: p.keySet())
		{
			String key = (String) k;
			o.addProperty(key, p.getProperty(key));
		}
		return o;
	}
	
	static public JsonObject mapToJson(Map<String,String > p)
	{
		JsonObject o = new JsonObject();
		for (Object k: p.keySet())
		{
			String key = (String) k;
			o.addProperty(key, p.get(key));
		}
		return o;
	}
	
	static public Map<String,Object>  toMap(JsonObject o)
	{
		if (o == null)
			return null;
		Map<String,Object> m = new HashMap<String,Object>();
		for (Entry<String, JsonElement> e: o.entrySet())
		{

			//Object x = o.get(s);
			//System.err.println(s + " --> "  + x);
			String v = e.getValue().getAsString();
			//System.err.println("Value toString: " + v);
			m.put(e.getKey(),v);
		}
		return m;
	}


	static public Properties toProperties(JsonObject o)
	{
		if (o == null)
			return null;

		Properties m = new Properties();
		for (Entry<String, JsonElement> e: o.entrySet())
		{

			//Object x = o.get(s);
			//System.err.println(s + " --> "  + x);
			String v = e.getValue().getAsString();
			//System.err.println("Value toString: " + v);
			m.put(e.getKey(),v);
		}
		return m;
	}

	public static void main(String[] args)
	{
		String s = 
				"  {" +
						"   name: Jack, " +
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
