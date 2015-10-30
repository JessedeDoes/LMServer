package eu.transcriptorium.filestore;
import java.util.*;
import java.io.*;

public class Command
{
	public enum type
	{
		SHELL,
		JAVA
	} ;

	static public enum ioType
	{
		IN,
		OUT
	};

	static public enum referenceType
	{
		NAME,
		ID
	};

	type commandType;

	static class Argument
	{
		String name;
		String className;
		Class argumentClass;
		ioType ioType = Command.ioType.IN;
		referenceType referenceType;
		String flagName = null;
		
		public String toString()
		{
			return "name=" + name + "; class=" + className;
		}

		public Argument(String name, String className)
		{
			this.name = name;
			this.className = className;
			try 
			{
				this.argumentClass = Class.forName(className);
			} catch (ClassNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Argument()
		{

		}

		public Argument(Object[] x)
		{
			this((String) x[0], (String) x[1]);
			if (x.length > 2)
				this.ioType = (ioType) x[2];
			if (x.length > 3)
				this.referenceType = (referenceType) x[3];
		}

		public static List<Argument> makeArgumentList(Object[][] x)
		{
			List<Argument> l = new ArrayList<Argument>();
			for (Object [] y: x)
				l.add(new Argument(y));
			return l;
		}
	}

	public void addArgument(String name, String className)
	{
		this.arguments.add(new Argument(name, className));
	}

	public static class FileArgument
	{
		String pathName;
		int repositoryId;
	}

	List<Argument> arguments = null;

	public void invoke(Map<String, Object> arguments) throws IOException 
	{
		// TODO Auto-generated method stub

	}

	//List<FileArgument> inFiles;
	//List<FileArgument> outFiles;
}
