package eu.transcriptorium.repository;
import java.io.File;
import java.io.Reader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/*
  maak van ieder tekstbestand een '.vert' bestand
  met voor ieder token een tokennummer, de offsets 
  (voor PDF of image zou het een rectangle kunnen zijn)
 */

/*
  drop table if exists text;
  create table text (id varchar(1000), text LONGTEXT);
 */

class ConnectorSimple
{
	//static private Logger logger = Logger.getLogger(DatabaseConnectorSimple.class);

	public Connection connect(String url, String user, String password) throws Exception
	{
		try
		{
			// Register the JDBC driver for MySQL.
			if (url.contains("jdbc:mysql"))
				Class.forName("com.mysql.jdbc.Driver");
			if (url.contains("jdbc:postgres"))
				Class.forName("org.postgresql.Driver");
			// Get a connection to the database
			
			System.err.println("Connecting to: " + url);
			Connection connection = DriverManager.getConnection(url, user, password);

			// Grote packets toestaan
			if (url.contains("mysql"))
			{
				Statement stmt = connection.createStatement();
				stmt.execute("SET SESSION `max_allowed_packet`= 1000000000;");
				stmt.close();
			}

			return connection;
		}
		catch (ClassNotFoundException e)
		{
			//logger.error("Database driver niet gevonden", e);
			throw e;
		}
		catch (SQLException e)
		{
			//logger.error("Kan geen verbinding met database maken", e);
			System.err.println("SQL exception: " + e);
			throw e;
		}
	}
}

public class SimpleDatabase
{
	private Connection connection = null;
	static boolean PREPEND_COLUMN_NAMES = true;
	String rdbms = "mysql";
	String dbHost = "localhost";
	String dbPort = "3306"; // 5432 for postgresql
	String dbSchemaName = "SONAR";
	String dbURL = "jdbc:"  + getRDBMS() + "://" + dbHost + ":" + dbPort + "/" + dbSchemaName;
	//+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true";

	String dbUser = "jesse";
	String dbPasswd = "d2d4d7d5";
	String storageTable = "Bestanden_01";  

	String getRDBMS()
	{
		return rdbms;
	} 

	public SimpleDatabase(Properties props)
	{
		dbHost = props.getProperty("dbHost");
		dbPort = props.getProperty("dbPort");
		dbSchemaName = props.getProperty("dbSchemaName");
		dbUser = props.getProperty("dbUser");
		dbPasswd = props.getProperty("dbPasswd");
		dbURL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbSchemaName;
		System.err.println("Mysql URL: " + dbURL);
		init();
	}

	public SimpleDatabase()
	{
		//init();
	} 

	public String getURL()
	{
		return  "jdbc:"  + getRDBMS() + "://" + dbHost + ":" + dbPort + "/" + dbSchemaName;
	}

	public void init()
	{
		dbURL = getURL();
		try 
		{
			this.setConnection((new ConnectorSimple()).connect(dbURL, dbUser, dbPasswd));
		} catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}

	public void close()
	{
		try 
		{
			this.getConnection().close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean query(String s) throws Exception
	{
		System.err.println(s);
		Statement stmt = null;
		try
		{
			stmt = this.getConnection().createStatement();
			return stmt.execute(s);
		}
		catch (Exception e)
		{
			//logger.error("Fout bij uitvoeren query", e);
			e.printStackTrace();
			throw e;
		}
		finally
		{
			try
			{ 
				if (stmt != null) stmt.close();
			} catch (SQLException e)
			{
				//logger.error("Fout bij sluiten db statement", e);
				throw e;
			}
		}
	}


	public List<List<String>> SimpleSelect(String query) 
	{
		PreparedStatement stmt = null;
		List<List<String>> types = new ArrayList<List<String>>();
		try
		{
			stmt = this.getConnection().prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			int nofcolumns = rs.getMetaData().getColumnCount();
			while (rs.next()) // mis je nu de eerste??
			{
				Vector<String> row = new Vector<String>();
				for (int i=1; i <= nofcolumns; i++)
				{
					try
					{
						String s = new String(rs.getBytes(i), "UTF-8");
						row.addElement(s);
					} catch (Exception e)
					{
						row.addElement("NULL");
					}
				}
				types.add(row);
			}
		} catch (Exception e)
		{
			System.err.println(query);
			e.printStackTrace();
		}
		return types;
	}




	public	List<String> getColumn(Vector<Vector<String>> matrix, int k)
	{
		List<String> column = new ArrayList<String>();
		for (int i=0; i < matrix.size(); i++)
			column.add( (matrix.get(i)).get(k));
		return column; 
	}


	public String setValues(String table_name, String key_fieldname, String key_fieldvalue, String field_name, String field_value)
	{
		try
		{
			String q = String.format("update %s set %s = ? where %s=?",table_name, field_name, key_fieldname);
			PreparedStatement stmt = null;
			stmt = this.getConnection().prepareStatement(q);
			stmt.setBytes(1, field_value.getBytes("UTF-8"));
			stmt.setBytes(2, key_fieldvalue.getBytes("UTF-8"));
			System.err.println(stmt);
			int u = stmt.executeUpdate();
			System.err.println("hihi " + u);
			stmt.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public int nofRows (String table_name) throws Exception
	{
		try
		{
			PreparedStatement stmt = null;
			stmt = this.getConnection().prepareStatement("select count(*) from " + table_name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				int  n = Integer.parseInt(new String(rs.getBytes(1), "UTF-8"));
				return n;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public String loadFunction()
	{
		return "LOAD_FILE";
	}

	public static void main(String[] args) throws Exception
	{
		SimpleDatabase l = new SimpleDatabase();
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
