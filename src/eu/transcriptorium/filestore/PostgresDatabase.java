package nl.inl.db.filestore;
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
    System.err.println("Mysql URL: " + dbURL);
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
    p.put("dbSchemaName", "impactlexicon");
    p.put("dbPasswd", "impact"); 
    p.put("dbUser", "impact");
    PostgresDatabase pg = new PostgresDatabase(p);
    Vector<Vector<String>> r = pg.SimpleSelect("select * from lemmata limit 100;");
    for (Vector<String> v: r)
     for (String s: v)
       System.out.println(s);
  }
}
