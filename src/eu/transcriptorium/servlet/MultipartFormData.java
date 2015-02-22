/**
 *
 */
package eu.transcriptorium.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 */

public class MultipartFormData 
{
	private static final int maxMemSize = 1000000000;
	private static final long maxFileSize = 1000000000;
	final private List<File> files = new LinkedList<File>();
	private Map <String, File> fileMap = new HashMap<String, File>();
	final private Map<String, String> fields = new HashMap<String, String>();
	private HttpServletRequest request;
	private List<FileItem> fileItems = null;
	private String directoryForProccessedResources = "/datalokaal/Corpus/LM/upload"; // get this from some properties file
    
	public List<String> getNamesOfUploadedfiles()
	{
		List<String> r  = new ArrayList<String>();
		for (File f: files)
		{
			try 
			{
				r.add(f.getCanonicalPath());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return r;
	}
	
	public void addFile(String name, File f) 
	{
		files.add(f);
		fileMap.put(name,f);
	}

	public void addField(String field, String value) 
	{
		fields.put(field, value);
	}

	public Map<String, String> getFields() 
	{
		return fields;
	}

	public List<File> getFiles() {
		return files;
	}

	public void removeFile(File f) 
	{
		files.remove(f);
	}


	public static String getFileContent(File f) 
	{
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
	
	private List<FileItem> getMultipartFileItems(HttpServletRequest request) 
	{
		if(ServletFileUpload.isMultipartContent(request) && fileItems == null) 
		{
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// maximum size that will be stored in memory
			factory.setSizeThreshold(maxMemSize);
			// Location to save data that is larger than maxMemSize.
			factory.setRepository(new File(this.directoryForProccessedResources));

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// maximum file size to be uploaded.
			upload.setSizeMax(maxFileSize);
			upload.setHeaderEncoding("UTF-8");

			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
		return fileItems;
	}
	
   public MultipartFormData(HttpServletRequest request) 
   {
		//MultipartFormData mpfd = this;
		//this.servlet = request.getS

		List<FileItem> fileItems = getMultipartFileItems(request);

		// Process the uploaded file items
		Iterator<FileItem> i = fileItems.iterator();

		while(i.hasNext()) 
		{
			FileItem fi = i.next();

			if (fi.isFormField()) 
			{
				String fieldname = fi.getFieldName();
				String fieldvalue = fi.getString();

				addField(fieldname, fieldvalue);
			} else 
			{
				// Get the uploaded file parameters
				try	
				{
					// create a temporary file to store the uploaded file in
					File file = File.createTempFile("upload", "tmp");
					// write uploaded file to disk
					fi.write(file);
					addFile(fi.getFieldName(), file);
				}  catch (Exception e) 
				{
					System.err.println("Error processing: " + fi);
					e.printStackTrace();
				}
			}
		}
	}
}
