package eu.transcriptorium.filestore;
import java.util.*;
import java.io.*;

public interface Repository
{
	int storeFile(InputStream s, Properties metadata); // returns id
	InputStream openFile(int id);
	Set<Integer> search(Properties metadata);
	boolean setTag(Collection<Integer> files, String tag);
	public Properties getMetadata(int id);
	boolean delete (int id);
}
