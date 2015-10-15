package nl.inl.db.filestore;
import java.io.File;
import java.io.Reader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FileStore
{

  List <String> fileNames = new ArrayList<String>();
  List <String> tempFileNames = new ArrayList<String>();
  List <File> tempFiles = new ArrayList<File>();
  int nFiles = 0;
  int portionSize = 100;
  String tableName="Bestanden_02";
  SimpleDatabase database = new PostgresDatabase();

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

  public void storeFile(java.io.InputStream in, String fileName)
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
    return "'" + s + "'";
  }

  public void flushFiles()
  {
    if (fileNames.size() < 1)
      return;
    String q = "insert into "  + tableName + " (id,type,content) VALUES ";
    for (int i=0; i < fileNames.size(); i++)
    {
      q += "(" + quote(fileNames.get(i)) + ", " + quote(getType(fileNames.get(i))) + "," + database.loadFunction() + "(" + quote(tempFileNames.get(i)) + ")";
      if (i < fileNames.size() - 1)
      {
        q += "),";
      } else
      { 
        q += ");";
      }
    }
    System.err.println(q);
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
        tempFiles.get(i).delete();
      }
      tempFiles.clear();
      fileNames.clear();
      tempFileNames.clear();
    }
  } 
}
