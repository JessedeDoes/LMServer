package eu.transcriptorium.filestore;
import org.apache.tools.tar.*;

import eu.transcriptorium.filestore.old.FileStore;

import java.io.*;
import java.util.zip.*;
import java.util.*;

public class BackupDirectory
{
  String dirName;
  File current;
  int portionSize;
  FileStore f = new FileStore(null);
 
  public void doit(String dirName)
  {
    f.clear();
    try
    {
      List<File> list = FileListing.getFileListing(new File(dirName));
      for (File current: list)
      {
        System.err.println("ok:"  + current.getName());
        //long s = current.getSize();
        if (!current.isDirectory())
        {
          System.err.println(current.getName());
          f.prepareForBulkInsert(new FileInputStream(current),current.getName());
        }
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    f.flushFiles();
  } 

  public static void main(String[] args)
  {
    new BackupDirectory().doit(args[0]);
  }
}
