package eu.transcriptorium.repository;
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
	static boolean uniqueNames = true;

	// create table FileTable (id serial, fileName text, type text, content oid);

	static String createFileTable =uniqueNames? "create table FileTable "
			+ "(id serial primary key, "
			+ "filename text not null UNIQUE, language text, "
			+ "year_from integer, year_to integer, type text references types (type), content bytea)":

				"create table FileTable (id serial primary key, filename text, type text, content bytea)";
	static String createMetadataTable = "create table metadata (id integer references filetable(id) on delete cascade, key  text, value text, constraint munq unique (id, key, value))";
	static String createTagsTable = "create table tags (tag_id integer, tag text, file_id integer)";

	static String createUsers="CREATE TABLE users (username         VARCHAR(15) NOT NULL PRIMARY KEY, password         CHAR(32) NOT NULL);";
	static String createRoles = "CREATE TABLE user_roles (username         VARCHAR(15) NOT NULL, role             VARCHAR(15) NOT NULL, "
			+ "PRIMARY KEY    (username, role), "
			+  " FOREIGN KEY (username) REFERENCES users(username));";

	// uniqueness constraint does not work with NULL!
	static String createCollectionsTable = "create table collections (collection_id integer references filetable(id) on delete cascade, item_id integer references filetable(id) on delete cascade, constraint unq unique(collection_id, item_id))";
	static String createTypesTable = "create table types (type text primary key)";

	static String[] predefinedTypes = 
		{ "lm", "dictionary", "corpus_plaintext", "collection", "word_list",
				"page_xml", "tei_xml", "doc", "docx", "character_set"};

	String tableName="filetable";

	SimpleDatabase database; //  = new PostgresDatabase();

	public PostgresRepository(Properties p)
	{
		database =  new PostgresDatabase(p);
		System.err.println("connection established...");
	}

	public void clear()
	{
		createNew();
	}

	public void createNew()
	{
		try
		{
			database.query("drop table if exists filetable cascade");
			database.query("drop table if exists metadata");
			database.query("drop table if exists tags");
			database.query("drop table if exists collections");
			database.query("drop table if exists types");

			// do not drop users....
			//database.query("drop table if exists users");
			//database.query("drop table if exists user_roles");
			database.query(createTypesTable);
			String q = "insert into types (type) VALUES (?)";
			String qm="";

			for (String s: predefinedTypes)
			{
				PreparedStatement stmt = database.getConnection().prepareStatement(q);
				stmt.setString(1, s);
				stmt.execute();
			}

			database.query(createFileTable);
			database.query(createMetadataTable);
			//database.query(createTagsTable);
			database.query(createCollectionsTable);
			database.query(createUsers);
			database.query(createRoles);
			//database.query("insert into users (name,password) VALUES()";
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//  SELECT currval(pg_get_serial_sequence('persons','id'));

	private int getSerialCurrVal(String tableName, String fieldName)
	{
		String q = "select currval (pg_get_serial_sequence('" + tableName +"','" + fieldName + "'))";
		System.err.println(q);
		List<List<String>> r = database.SimpleSelect(q);
		String x = r.get(0).get(0);
		return Integer.parseInt(x);
	}

	private int getLastId()
	{
		return getSerialCurrVal(tableName, "id");
	}

	private void clearFileTable()
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

	/**
	 * TODO: uniqueness constraints...
	 * @param fi
	 * @param filename
	 * @return
	 */
	public int  storeFile(InputStream fi, String filename)
	{
		String query = "INSERT INTO " + tableName + " (filename, content) VALUES (?, ?);";
		boolean isUpdate = false;
		int updateId = -1;
		if (uniqueNames)
		{
			Set<Integer> V = searchByName(filename);
			if (V != null && V.size() > 0)
			{
				isUpdate = true;
				updateId = V.iterator().next();
				query = "update " + tableName + " set content=?  where id=? ";
			}
		}
		System.err.println(query);
		try 
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(query);
			File file = new File(filename);
			// AHA dit klopt allerminst!...
			// FileInputStream fi = new FileInputStream(file);
			if (isUpdate)
			{
				System.err.println("update for:"  + filename  + ": "  + query);
				stmt.setBinaryStream(1, fi);
				stmt.setInt(2, updateId);
			} else
			{
				stmt.setString(1, filename);
				stmt.setBinaryStream(2, fi);
			}
			//stmt.set
			boolean res = stmt.execute();
			stmt.close();
			fi.close();
			int id = isUpdate?updateId:getLastId();
			System.err.println("id of stored file: " + id);
			return id;
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public int storeFile(InputStream fi, String filename,Properties p)
	{
		int id = storeFile(fi,  filename);
		if (p == null)
			p = new Properties();
		addDefaultProperties(p);
		if (p.getProperty("filename") == null)
			p.put("filename", filename);
		setMetadata(id, p);
		return id;
	}

	public int storeFile(InputStream fi, Properties p)
	{
		int id = storeFile(fi,  "anonymous");
		setMetadata(id, p);
		return id;
	}

	private void addDefaultProperties(Properties p)
	{
		if (p.getProperty("createdBy") == null)
			p.put("createdBy", "STORE_FILE");
		p.put("createdAt", new Date(System.currentTimeMillis()).toString());
	}

	public int storeFile(String filename, Properties p)
	{
		int id = storeFile(filename);
		if (p == null)
		{
			p = new Properties();
		}
		if (p != null)
		{
			if (p.getProperty("filename") == null)
				p.put("filename", filename);
			if (id >= 0)
			{
				System.err.println(p.keySet());
				setMetadata(id, p);
			}
		}
		return id;
	}

	@Override
	public void setMetadata(int id, Properties p)
	{
		if (p == null)
			return;
		for (Object  n: p.keySet())
		{
			String key = (String) n;
			String value  = p.getProperty(key);
			setMetadataProperty(id, key, value);

		}
	}

	private void  transferMetadataToMainTable(int id, String key, String value)
	{
		String q = "update filetable set " + key + "=? where id=?";
		boolean transfer = true;
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(2, id);
			switch (key.toLowerCase())
			{
			case "type": case "filename": case "language": stmt.setString(1, value); break;
			case "year_from": case "year_to": stmt.setInt(1, Integer.parseInt(value)); break;
			default: transfer = false;
			}
			if (transfer)
			{
				stmt.execute();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setMetadataProperty(int id, String key, String value) 
	{
		String oldVal = getMetadataProperty(id,key);
		String query = "insert into metadata (id,key,value) values (?,?,?)";
		if (oldVal != null)
		{
			query = "update metadata set value=? where key=? and id=?";
		}

		try 
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(query);
			System.err.println(key + " = " + value);
			if (oldVal == null)
			{
				stmt.setInt(1, id);
				stmt.setString(2, key);
				stmt.setString(3, value);
			} else
			{
				stmt.setString(2, key);
				stmt.setString(1, value);
				stmt.setInt(3, id);
				System.err.println("metadata update:" + stmt);
			}
			boolean res = stmt.execute();
			stmt.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		transferMetadataToMainTable(id, key, value);
	}

	private String getTypeFromFilename(String f)
	{
		return "text";
	} 

	/**
	public String quote(String s)
	{
		return "'" + s.replaceAll("\\\\",  "/") + "'";
	}
	 */
	public InputStream openFile(int id)
	{
		String q = " select content from filetable where id=? ";

		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();

			while (rs.next()) 
			{
				InputStream s = rs.getBinaryStream(1); //  new String(rs.getBytes(1), "UTF-8");
				return s;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public Set<Integer> searchByName(String name)
	{
		Set<Integer> result = new HashSet<Integer>();
		boolean regex = false;
		String operator = "=";
		if (name.startsWith("<"))
		{
			operator = "<";
			name = name.substring(1);
		} else if (name.startsWith(">"))
		{
			operator = ">";
			name = name.substring(1);
		} else
			if (name.startsWith("~"))
			{
				regex = true;
				name = name.substring(1);
				operator = "~";
			}



		String q = " select distinct id from filetable where filename"  + operator +  "? ";

		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setString(1,name);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				int  i = rs.getInt(1); 
				result.add(i);
			}
		} catch (Exception e)
		{

			e.printStackTrace();
		}
		return result;
	}

	public int search(String name)
	{
		Set<Integer> V = searchByName(name);
		if (V.size() > 0)
			return V.iterator().next();
		return -1;
	}

	public Set<Integer> search(Properties p)
	{
		List<String> clauses = new ArrayList<String>();
		List<String> fillers = new ArrayList<String>();
		Set<Integer> result = new HashSet<Integer>();

		int nClauses = 0;

		List<PropertyCheck> propertiesToCheck = new ArrayList<PropertyCheck>();

		for (Object n: p.keySet())
		{
			String key = (String) n;
			String value  = null;
			ItemProperty it = null;
			value = p.getProperty(key);


			String operator = "=";
			if (value.startsWith("<"))
			{
				operator = "<";
				value = value.substring(1);
			} else if (value.startsWith(">"))
			{
				operator = ">";
				value = value.substring(1);
			} else if (value.startsWith("~"))
			{
				//regex = true;
				value = value.substring(1);
				operator = "~";
			}

			boolean regex = false;

			if (value.startsWith("~"))
			{
				regex = true;
				value = value.substring(1);
			}

			if (key.contains("("))
			{
				int pos = key.indexOf("(");
				String nm = key.substring(0,pos);
				String paramList = key.substring(pos);
				paramList = paramList.replaceAll("[()]", "");
				String[] t = paramList.split(",");

				Class[] paramClasses = new Class[t.length+1];
				Object[] params = new Object[t.length+1];
				params[0] = this;
				paramClasses[0] = Repository.class;
				for (int i=0; i < t.length; i++)
				{
					params[i+1] = Integer.parseInt(t[i]);
					paramClasses[i+1] = Integer.class;
				}

				try
				{
					Class c = Class.forName(nm);
					Object o = c.getDeclaredConstructor(paramClasses).newInstance(params);
					if (o instanceof ItemProperty)
					{
						it = (ItemProperty) o; // this requires quite a lot of ugly reflection stuff
						propertiesToCheck.add(new PropertyCheck(it, operator, value));
						//value = it.getPropertyValue(this, id); // Neen....
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} else
			{
				System.err.println("Value=" + value);




				String clause = " select distinct id from metadata where key=? and value" +  operator + "? ";
				clauses.add(clause);
				fillers.add(key);
				fillers.add(value);
			}
		}

		String q = StringUtils.join(clauses, " INTERSECT ");

		// System.err.println(q);
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			for (int i=0; i < fillers.size(); i++)
				stmt.setString(i+1, fillers.get(i));
			System.err.println("Search query:" + stmt);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				int  i = rs.getInt(1); //  new String(rs.getBytes(1), "UTF-8");
				boolean add = true;
				for (ItemTest t: propertiesToCheck)
				{
					add &= t.test(this, i);
				}
				if (add) result.add(i);
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


	private int getContentLength(int id)
	{
		String q = " select length(content) from filetable where id=? ";
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				int l = rs.getInt(1);
				return l;
			}
		} catch (Exception e)
		{
			return -1;
		}
		return -1;
	}

	@Override
	public Properties getMetadata(int id)
	{
		// TODO Auto-generated method stub
		String q = " select key,value from metadata where id=? ";
		Properties p = new Properties();
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				String k = rs.getString(1);
				String v = rs.getString(2);
				p.setProperty(k, v);
			}
		} catch (Exception e)
		{
			return null;
		}
		p.put("content-length", this.getContentLength(id) + "");
		return p;
	}

	@Override
	public String getMetadataProperty(int id, String key)
	{
		// TODO Auto-generated method stub
		String q = " select value from metadata where id=? and key=? ";
		Properties p = new Properties();
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, id);
			stmt.setString(2, key);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			while (rs.next()) 
			{
				String k = rs.getString(1);
				return k;
			}
		} catch (Exception e)
		{
			return null;
		}
		return null;
	}

	public static Properties getDefaultConnectionProperties()
	{
		Properties p = new Properties();

		p.put("dbHost", "svowdb02"); 
		p.put("dbPort", "5432");
		p.put("dbSchemaName", "lmserver");
		p.put("dbPasswd", "inl"); 
		p.put("dbUser", "postgres");

		return p;
	}

	//@Override
	public boolean setTag(Collection<Integer> fileIds, String tag)
	{
		// TODO no duplicate check...
		for (int i : fileIds)
		{
			String q = "insert into tags (text, file_id) values (?,?)";
			try
			{
				PreparedStatement stmt = database.getConnection().prepareStatement(q);
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


	@Override
	public List<FileInfo> list() 
	{
		// TODO Auto-generated method stub
		List<FileInfo> V = new ArrayList<FileInfo>();
		String q = "select id,filename,length(content) from filetable order by id";

		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				FileInfo fi = new FileInfo();
				fi.id = rs.getInt(1);
				fi.fileName = rs.getString(2);
				fi.contentLength = rs.getInt(3);
				V.add(fi);
			}
		} catch (Exception e)
		{
			return null;
		}
		return V;
	}



	@Override
	public Set<Integer> getCollectionItems(int collection_id) 
	{
		// TODO Auto-generated method stub
		String q = "select item_id from collections where collection_id=?";
		Set<Integer> V = new HashSet<Integer>();
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, collection_id);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				V.add(rs.getInt(1));
			}
		} catch (Exception e)
		{
			return null;
		}
		return V;
	}

	@Override
	public void addToCollection(int collection_id, int item_id) 
	{

		String query = "insert into collections (collection_id, item_id) VALUES (?,?)";
		try 
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(query);
			stmt.setInt(1,collection_id);
			stmt.setInt(2,item_id);
			boolean res = stmt.execute();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void removeFromCollection(int collection_id, int item_id) 
	{
		// TODO Auto-generated method stub
		String query = "delete from collections where collection_id=? and item_id=?";
		try 
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(query);
			stmt.setInt(1,collection_id);
			stmt.setInt(2,item_id);
			boolean res = stmt.execute();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int createCollection(String name, Properties metadata) 
	{
		String query = "INSERT INTO " + tableName + " (filename) VALUES (?);";
		boolean isUpdate = false;
		int updateId = -1;
		if (uniqueNames)
		{
			Set<Integer> V = searchByName(name);
			if (V != null && V.size() > 0)
			{
				isUpdate = true;
				updateId = V.iterator().next();
				query = "update " + tableName + " set content=?  where id=? ";
			}
		}
		try 
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(query);

			// FileInputStream fi = new FileInputStream(file);
			if (isUpdate)
			{

			} else
			{
				stmt.setString(1, name);

			}
			//stmt.set
			boolean res = stmt.execute();
			stmt.close();

			int id = isUpdate?updateId:getLastId();
			System.err.println("id of stored file: " + id);
			return id;
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public String getName(int id) 
	{
		String q = " select filename from filetable where id=? ";

		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				String s = rs.getString(1); //  new String(rs.getBytes(1), "UTF-8");
				return s;
			}
		} catch (Exception e)
		{
			return null;
		}
		return null;
	}


	public static void main(String [] args)
	{
		Properties p = new Properties();



		p = getDefaultConnectionProperties();
		p.put("year_from", "300");
		p.put("language", "dutch");

		PostgresRepository fs = new PostgresRepository(p);
		fs.createNew();

		//fs.testje();

		fs.createCollection("bentham", null);
		fs.addToCollection(fs.search("bentham"), fs.search("bentham"));

		fs.setMetadata(fs.search("bentham"), p);

		//fs.removeFromCollection(fs.search("bentham"), fs.search("bentham"));
		//fs.storeFile("s:/Jesse/bred001kluc04_01.xml",p);

		Set<Integer>  V = fs.search(p);
		for (int k: V)
		{
			System.out.println(fs.getMetadata(k));
		}
	}

	@Override
	public Set<String> getRolesForUser(Map<String, String> userCredentials) 
	{
		// TODO Auto-generated method stub
		String q = " select role from users,user_roles where users.username=user_roles.username and users.username=? and users.password=? ";
		Set<String> roles = new HashSet<String>();
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q);
			stmt.setString(1, userCredentials.get("username"));
			stmt.setString(2, userCredentials.get("password"));

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) 
			{
				String r = rs.getString(1); //  new String(rs.getBytes(1), "UTF-8");
				roles.add(r);
			}
			return roles;
		} catch (Exception e)
		{
			return roles;
		}
	}

	@Override
	public boolean addUser(Map<String, String> userCredentials) 
	{
		// TODO Auto-generated method stub

		String rule = userCredentials.get("role");

		String q0 = "insert into users (username,password) VALUES (?,?)";
		String q1 = "insert into user_roles (username,role) VALUES (?,?)";
		try
		{
			PreparedStatement stmt = database.getConnection().prepareStatement(q0);
			stmt.setString(1, userCredentials.get("username"));
			stmt.setString(2, userCredentials.get("password"));
			stmt.execute();
			stmt = database.getConnection().prepareStatement(q1);
			stmt.setString(1, userCredentials.get("username"));
			stmt.setString(2, userCredentials.get("role"));
			stmt.execute();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Set<Integer> search(ItemTest test)
	{
		// TODO Auto-generated method stub
		Set<Integer> V = new HashSet<Integer>();
		List<FileInfo> l = this.list();
		for (FileInfo f: l)
		{
			if (test.test(this,f.id))
			{
				V.add(f.id);
			}
		}
		return V;
	}
}
