package oldstuff.eu.transcriptorium.filestore.old;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import eu.transcriptorium.repository.FileUtils;
import eu.transcriptorium.repository.PostgresDatabase;
import eu.transcriptorium.repository.SimpleDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Deprecated
public class FileStore
{

	List <String> fileNames = new ArrayList<String>();
	List <String> tempFileNames = new ArrayList<String>();
	List <File> tempFiles = new ArrayList<File>();
	int nFiles = 0;
	int portionSize = 100;
	// create table FileTable (id serial, fileName text, type text, content oid);
	static String createFileTable = "create table FileTable (id serial primary key, fileName text, type text, content bytea)";
	static String createMetadataTable = "create table metadata (id integer, key  text, value text)";
	
	String tableName="filetable";
	
	SimpleDatabase database; //  = new PostgresDatabase();

	public FileStore(Properties p)
	{
		database =  new PostgresDatabase(p);
	}
	
	public void createNew()
	{
		try
		{
			database.query("drop table if exists filetable");
			database.query("drop table if exists metadata");
			
			database.query(createFileTable);
			database.query(createMetadataTable);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//  SELECT currval(pg_get_serial_sequence('persons','id'));
	
	public int getSerialCurrVal(String tableName, String fieldName)
	{
		String q = "select currval (pg_get_serial_sequence('" + tableName +"','" + fieldName + "'))";
		System.err.println(q);
		List<List<String>> r = database.SimpleSelect(q);
		String x = r.get(0).get(0);
		return Integer.parseInt(x);
	}
	
	public void testje()
	{
		String q = "select \\\\lo_import 's:/jesse/D422.pdf'";
		System.out.println(database.SimpleSelect(q));
	}
	
	public int getLastId()
	{
		return getSerialCurrVal(tableName, "id");
	}
	
	public void clear()
	{
		try
		{
			database.query("delete from " + tableName);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void StoreFile (java.io.InputStream in, String fileName)
	{
		prepareForBulkInsert(in, fileName);
		flushFiles();
		System.err.println( "Id"  + getLastId());
	}
	
	
	public void StoreFile(String fileName)
	{
		InputStream i;
		try
		{
			i = new FileInputStream(fileName);
			StoreFile(i,fileName);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int  StoreOneFileBytea(String filename)
	{
		String query = "INSERT INTO " + tableName + " (filename, content) VALUES (?, ?);";
		try {
		    PreparedStatement stmt = database.getConnection().prepareStatement(query);
		    File file = new File(filename);
		    FileInputStream fi = new FileInputStream(file);
		    stmt.setString(1, filename);
		    stmt.setBinaryStream(2, fi, (int) file.length());
		    boolean res = stmt.execute();
		    stmt.close();
		    fi.close();
		    int id = getLastId();
		    System.err.println("id of stored file: " + id);
		    return id;
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	public int StoreOneFileBytea(String filename, Properties p)
	{
		int id = StoreOneFileBytea(filename);
		p.put("filename", filename);
		if (id >= 0)
		{
			System.err.println(p.keySet());
			for (Object  n: p.keySet())
			{
				String key = (String) n;
				String value  = p.getProperty(key);
				String query = "insert into metadata (id,key,value) values (?,?,?)";
				try 
				{
				    PreparedStatement stmt = database.getConnection().prepareStatement(query);
				    System.err.println(key + " = " + value);
				    stmt.setInt(1, id);
				    stmt.setString(2, key);
				    stmt.setString(3, value);
				    boolean res = stmt.execute();
				    stmt.close();
				} catch (Exception e)
				{
					e.printStackTrace();
					return -1;
				}
			}
		}
		return id;
	}
	public void prepareForBulkInsert(java.io.InputStream in, String fileName)
	{
		try
		{
			File fTemp = File.createTempFile("hiep","hoi");
			System.err.println(nFiles + ": " + fileName);
			FileUtils.copyStream(in,fTemp);
			fileNames.add(fileName);
			tempFileNames.add(fTemp.getAbsolutePath());
			tempFiles.add(fTemp);
			nFiles++;
			if (nFiles % portionSize == 0)
			{
				flushFiles();
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
			flushFiles();
			System.exit(1);
		}
	}

	public String getType(String f)
	{
		return "text";
	} 

	public String quote(String s)
	{
		return "'" + s.replaceAll("\\\\",  "/") + "'";
	}

	public void flushFiles()
	{
		if (fileNames.size() < 1)
			return;
		String q = "insert into "  + tableName + " (filename,type,content) VALUES ";
		for (int i=0; i < fileNames.size(); i++)
		{
			q += "(" + quote(fileNames.get(i)) + ", " + quote(getType(fileNames.get(i))) + "," + 
					database.loadFunction() + "(" + quote(tempFileNames.get(i)) + ")";
			if (i < fileNames.size() - 1)
			{
				q += "),";
			} else
			{ 
				q += ");";
			}
		}
		System.err.println("Run insert query:  " + q);
		try 
		{
			database.query(q);
		} catch (Exception e)
		{
			e.printStackTrace(); 
			System.exit(1);
		} finally
		{
			for (int i=0; i < tempFiles.size(); i++)
			{
				System.err.print("Clearing "  + tempFiles.get(i));
				tempFiles.get(i).delete();
			}
			tempFiles.clear();
			fileNames.clear();
			tempFileNames.clear();
		}
	} 
	
	public static void main(String [] args)
	{
		Properties p = new Properties();
		
		p.put("dbHost", "svowdb02"); 
		p.put("dbPort", "5432");
		p.put("dbSchemaName", "lmserver");
		p.put("dbPasswd", "inl"); 
		p.put("dbUser", "postgres");
		
		FileStore fs = new FileStore(p);
		fs.createNew();
		//fs.testje();
		fs.StoreOneFileBytea("s:/jesse/D422.pdf",p);
	}
}
