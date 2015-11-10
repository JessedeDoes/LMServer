package oldstuff.eu.transcriptorium.filestore.old;
import org.apache.tools.tar.*;

import java.io.*;
import java.util.zip.*;

public class Tar2Mysql
{
  String archiveFilename;
  TarInputStream tar;
  TarEntry current;
  int portionSize;
  FileStore f = new FileStore(null);
 
  public void doit(String archiveFilename)
  {
    f.clear();
    try
    {
      tar = new TarInputStream(new GZIPInputStream(new FileInputStream(archiveFilename))); 
      while ((current = tar.getNextEntry()) != null)
      {
        System.err.println("ok:"  + current.getName());
        long s = current.getSize();
        if (!current.isDirectory())
        {
          //System.err.println(current.getFile());
          f.prepareForBulkInsert(tar,current.getName());
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
    new Tar2Mysql().doit(args[0]);
  }
}
