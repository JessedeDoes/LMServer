package eu.transcriptorium.repository;
import java.io.*;
import java.nio.channels.*;

public class FileUtils
{

	public static void copyStream(InputStream fis, File out) throws Exception 
	{
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) 
			{
				fos.write(buf, 0, i);
			}
		} 
		catch (Exception e) {
			throw e;
		}
		finally {
			//if (fis != null) fis.close();
			if (fos != null) fos.close();
		}
	}

	public static void copyStream(InputStream fis, OutputStream fos) throws Exception 
	{
		//FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) 
			{
				fos.write(buf, 0, i);
			}
		} 
		catch (Exception e) {
			throw e;
		}
		finally 
		{
			//if (fis != null) fis.close();
			if (fos != null) fos.close();
		}
	}

	public static void copyFile(File in, File out) 
			throws IOException 
	{
		FileChannel inChannel = new
				FileInputStream(in).getChannel();
		FileChannel outChannel = new
				FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(),
					outChannel);
		} 
		catch (IOException e) {
			throw e;
		}
		finally {
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}

	public static void deleteRecursively( File file )
	{
		if (!file.exists())
		{
			return;
		}

		if ( file.isDirectory() )
		{
			for ( File child : file.listFiles() )
			{
				deleteRecursively( child );
			}
		}
		if ( !file.delete() )
		{
			throw new RuntimeException(
					"Couldn't delete file. Offending file:" + file );
		}
	}

	public static void main(String args[]) throws IOException{
		FileUtils.copyFile(new File(args[0]),new File(args[1]));
	}
}
