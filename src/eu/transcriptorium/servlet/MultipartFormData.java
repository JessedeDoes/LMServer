/**
 *
 */
package eu.transcriptorium.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MultipartFormData {

	final private List<File> files = new LinkedList<File>();
	final private Map<String, String> fields = new HashMap<String, String>();

	public void addFile(File f) {
		files.add(f);
	}

	public void addField(String field, String value) {
		fields.put(field, value);
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public List<File> getFiles() {
		return files;
	}

	public void removeFile(File f) {
		files.remove(f);
	}

	public static String getFileContent(File f) {
		//System.out.println("getfilecontent! " + f.getPath());
		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(f.getPath()));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				//System.out.println(currentLine);
				sb.append(currentLine);
			}
		} catch (FileNotFoundException e) {
			return "Unable to find file " + f.getPath() + ", " + e.getMessage();
		} catch (IOException e) {
			return "Unable to read file " + f.getPath() + ", " + e.getMessage();
		}

		return sb.toString();
	}
}
