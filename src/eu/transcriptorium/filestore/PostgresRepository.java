package eu.transcriptorium.filestore;
import impact.ee.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PostgresRepository implements Repository
{

	List <String> fileNames = new ArrayList<String>();
	List <String> tempFileNames = new ArrayList<String>();
	List <File> tempFiles = new ArrayList<File>();
	int nFiles = 0;
	int portionSize = 100;

	// create table FileTable (id serial, fileName text, type text, content oid);

	static String createFileTable = "create table FileTable (id serial primary key, fileName text, type text, content bytea)";
	static String createMetadataTable = "create table metadata (id integer, key  text, value text)";
	static String createTagsTable = "create table tags (tag_id integer, tag text, file_id integer)";

	String tableName="filetable";

	SimpleDatabase database; //  = new PostgresDatabase();

	public PostgresRepository(Properties p)
	{
		database =  new PostgresDatabase(p);
	}

	public void createNew()
	{
		try
		{
			database.query("drop table if exists filetable");
			database.query("drop table if exists metadata");
			database.query("drop table if exists tags");
			database.query(createFileTable);
			database.query(createMetadataTable);
			database.query(createTagsTable);
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

	public int getLastId()
	{
		return getSerialCurrVal(tableName, "id");
	}

	public void clearFileTable()
	{
		try
		{
			database.query("delete from " + tableName);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int storeFile(String filename)
	{
		FileInputStream fi;
		try
		{
			fi = new FileInputStream(filename);
			return storeFile(fi, filename);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public int  storeFile(InputStream fi, String filename)
	{
		String query = "INSERT INTO " + tableName + " (filename, content) VALUES (?, ?);";
		try {
			PreparedStatement stmt = database.connection.prepareStatement(query);
			File file = new File(filename);
			// AHA dit klopt allerminst!...
			// FileInputStream fi = new FileInputStream(file);
			stmt.setString(1, filename);
			stmt.setBinaryStream(2, fi);
			//stmt.set
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

	public int storeFile(InputStream fi, Properties p)
	{
		int id = storeFile(fi,  "anonymous");
		storeMetadata(p, id);
		return id;
	}

	public int storeFile(String filename, Properties p)
	{
		int id = storeFile(filename);
		if (p != null)
		{
			p.put("filename", filename);
			if (id >= 0)
			{
				System.err.println(p.keySet());
				storeMetadata(p, id);
			}
		}
		return id;
	}

	public void storeMetadata(Properties p, int id)
	{
		for (Object  n: p.keySet())
		{
			String key = (String) n;
			String value  = p.getProperty(key);
			String query = "insert into metadata (id,key,value) values (?,?,?)";
			try 
			{
				PreparedStatement stmt = database.connection.prepareStatement(query);
				System.err.println(key + " = " + value);
				stmt.setInt(1, id);
				stmt.setString(2, key);
				stmt.setString(3, value);
				boolean res = stmt.execute();
				stmt.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
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

	public InputStream openFile(int id)
	{
		String q = " select content from filetable where id=? ";
		
		try
		{
			PreparedStatement stmt = database.connection.prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			
			while (rs.next()) // mis je nu de eerste??
			{
				InputStream s = rs.getBinaryStream(1); //  new String(rs.getBytes(1), "UTF-8");
				return s;
			}
		} catch (Exception e)
		{
			return null;
		}
		return null;
	}

	public Set<Integer> search(Properties p)
	{
		List<String> clauses = new ArrayList<String>();
		List<String> fillers = new ArrayList<String>();
		Set<Integer> result = new HashSet<Integer>();

		for (Object n: p.keySet())
		{
			String key = (String) n;
			String value = p.getProperty(key);
			String clause = " select distinct id from metadata where key=? and value=? ";
			clauses.add(clause);
			fillers.add(key);
			fillers.add(value);
		}

		String q = StringUtils.join(clauses, " INTERSECT ");

		System.err.println(q);
		try
		{
			PreparedStatement stmt = database.connection.prepareStatement(q);
			for (int i=0; i < fillers.size(); i++)
				stmt.setString(i+1, fillers.get(i));

			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			while (rs.next()) // mis je nu de eerste??
			{
				int  i = rs.getInt(1); //  new String(rs.getBytes(1), "UTF-8");
				result.add(i);
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean delete(int id)
	{
		// TODO Auto-generated method stub
		String q = " delete from filetable where id= " + id;
		try
		{
			database.query(q);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}



	@Override
	public Properties getMetadata(int id)
	{
		// TODO Auto-generated method stub
		String q = " select key,value from metadata where id=? ";
		Properties p = new Properties();
		try
		{
			PreparedStatement stmt = database.connection.prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			while (rs.next()) // mis je nu de eerste??
			{
				String k = rs.getString(1);
				String v = rs.getString(2);
				p.setProperty(k, v);
			}
		} catch (Exception e)
		{
			return null;
		}
		return p;
	}

	public static Properties getDefaultProperties()
	{
		Properties p = new Properties();

		p.put("dbHost", "svowdb02"); 
		p.put("dbPort", "5432");
		p.put("dbSchemaName", "lmserver");
		p.put("dbPasswd", "inl"); 
		p.put("dbUser", "postgres");
		
		return p;
	}
	
	@Override
	public boolean setTag(Collection<Integer> fileIds, String tag)
	{
		// TODO no duplicate check...
		for (int i : fileIds)
		{
			String q = "insert into tags (text, file_id) values (?,?)";
			try
			{
				PreparedStatement stmt = database.connection.prepareStatement(q);
				stmt.setString(1, tag);
				stmt.setInt(2, i);
				boolean res = stmt.execute();
				stmt.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void main(String [] args)
	{
		Properties p = new Properties();

		p.put("dbHost", "svowdb02"); 
		p.put("dbPort", "5432");
		p.put("dbSchemaName", "lmserver");
		p.put("dbPasswd", "inl"); 
		p.put("dbUser", "postgres");

		p = getDefaultProperties();
		PostgresRepository fs = new PostgresRepository(p);
		fs.createNew();
		//fs.testje();
		fs.storeFile("/tmp/hola",p);
		Set<Integer>  V = fs.search(p);
		for (int k: V)
		{
			System.out.println(fs.getMetadata(k));
		}
	}
}
