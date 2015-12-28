package eu.transcriptorium.repository;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.*;

public interface Repository
{
	//int storeFile(InputStream s, Properties metadata); // returns id
	static class FileInfo
	{
		public int id;
		String fileName;
		int contentLength;
		
		public String toString()
		{
			return String.format("(id=%d,name=%s,size=%d)", id, fileName, contentLength);
		}
		
		public JsonObject toJsonObject()
		{
			JsonObject o = new JsonObject();
			o.addProperty("id", id);
			o.addProperty("fileName", fileName);
			o.addProperty("contentLength", contentLength);
			return o;
		}
		
		public String toJsonString()
		{
			return toJsonObject().getAsString();
		}
	};
	
	public static class Static
	{
		public static JsonArray list(Repository r)
		{
			JsonArray l = new JsonArray();
			for (FileInfo f: r.list())
			{
				JsonObject o = f.toJsonObject();
				l.add(o);
			}
			return l;
		}
		
		public static JsonObject getMetadata(Repository r, int id)
		{
			Properties p = r.getMetadata(id);
			return eu.transcriptorium.util.JSON.propertiesToJson(p);
		}
		
		public static JsonArray search(Repository r, Properties p)
		{
			JsonArray l = new JsonArray();
			Set<Integer> V0 = r.search(p);
			for (int i: V0)
			{
				JsonElement j = new JsonPrimitive(i);
				l.add(j);
			}
			return l;
		}
		
		public static JsonArray searchByName(Repository r, String name)
		{
			JsonArray l = new JsonArray();
			Set<Integer> V0 = r.searchByName(name);
			for (int i: V0)
			{
				JsonElement j = new JsonPrimitive(i);
				l.add(j);
			}
			return l;
		}
	};
	
	int storeFile(InputStream s, String name, Properties metadata); 
	InputStream openFile(int id);
	
	Set<Integer> search(Properties metadata);
	Set<Integer> searchByName(String name);
	int search(String name);
	// collection stuff
	
	Set<Integer> getCollectionItems(int collection_id);
	void addToCollection(int collection_id, int item_id);
	void removeFromCollection(int collection_id, int item_id);
	int createCollection(String name, Properties metadata);
	
	List<FileInfo> list();
	//boolean setTag(Collection<Integer> files, String tag);
	String getMetadataProperty(int id, String key);
	public Properties getMetadata(int id);
	boolean delete (int id);
	void clear();
	void setMetadata(int id, Properties p);
	void setMetadataProperty(int id, String key, String value);
	String getName(int id);
}
