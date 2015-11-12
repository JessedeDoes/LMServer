package eu.transcriptorium.repository;
import java.util.*;
import java.io.*;

public interface Repository
{
	//int storeFile(InputStream s, Properties metadata); // returns id
	static class FileInfo
	{
		int id;
		String fileName;
		int contentLength;
		public String toString()
		{
			return String.format("(id=%d,name=%s,size=%d)", id, fileName, contentLength);
		}
	};
	int storeFile(InputStream s, String name, Properties metadata); 
	InputStream openFile(int id);
	Set<Integer> search(Properties metadata);
	Set<Integer> searchByName(String name);
	List<FileInfo> list();
	boolean setTag(Collection<Integer> files, String tag);
	String getMetadataProperty(int id, String key);
	public Properties getMetadata(int id);
	boolean delete (int id);
	void clear();
	void setMetadata(int id, Properties p);
	void setMetadataProperty(int id, String key, String value);
}
