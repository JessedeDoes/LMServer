package eu.transcriptorium.repository;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class PostgresDatabase extends SimpleDatabase
{

	public PostgresDatabase()
	{
		rdbms="postgresql";
		dbPort="5432"; 
		dbHost="svowdb02";
		dbSchemaName="INLModernCorpus";
		dbUser = "jesse";
		dbPasswd= "does";
		dbURL = "jdbc:"  + getRDBMS() + "://" + dbHost + ":" + dbPort + "/" + dbSchemaName;
		init();
	}

	public PostgresDatabase(Properties props)
	{
		rdbms="postgresql";
		dbHost = props.getProperty("dbHost");
		dbPort = props.getProperty("dbPort");
		dbSchemaName = props.getProperty("dbSchemaName");
		dbUser = props.getProperty("dbUser");
		dbPasswd = props.getProperty("dbPasswd");
		dbURL = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbSchemaName;
		System.err.println("DB URL: " + dbURL);
		init();
	}

	String getRDBMS()
	{
		return rdbms;
	}

	public String loadFunction()
	{
		return "lo_import";
	}

	public static void main(String[] args)
	{
		Properties p = new Properties();
		
		p.put("dbHost", "svowdb02"); 
		p.put("dbPort", "5432");
		p.put("dbSchemaName", "gigant_molex_devintern_tweedepoging");
		p.put("dbPasswd", "inl"); 
		p.put("dbUser", "postgres");
		
		PostgresDatabase pg = new PostgresDatabase(p);
		List<List<String>> r = pg.SimpleSelect("select modern_lemma, lemma_gigpos from lemmata limit 100;");
		
		for (List<String> v: r)
			System.out.println(v);
	}
}
