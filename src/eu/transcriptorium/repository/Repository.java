package eu.transcriptorium.repository;
import java.util.*;
import java.io.*;

public interface Repository
{
	int storeFile(InputStream s, Properties metadata); // returns id
	int storeFile(InputStream s, String name, Properties metadata); 
	InputStream openFile(int id);
	Set<Integer> search(Properties metadata);
	Set<Integer> searchByName(String name);
	Set<Integer> list();
	boolean setTag(Collection<Integer> files, String tag);
	String getMetadata(int id, String key);
	public Properties getMetadata(int id);
	boolean delete (int id);
	void clear();
	void setMetadata(int id, Properties p);
	void setMetadata(int id, String key, String value);
}
