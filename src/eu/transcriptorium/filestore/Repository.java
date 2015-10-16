package eu.transcriptorium.filestore;
import java.util.*;
import java.io.*;

public interface Repository
{
	int store(InputStream s, Properties metadata); // returns id
	InputStream openFile(int id);
	Set<Integer> search(Properties metadata);
}
