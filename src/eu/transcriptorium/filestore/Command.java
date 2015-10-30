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
    	 
    	 public String toString()
    	 {
    		 return "name=" + name + "; class=" + className;
    	 }
    	 
    	 public Argument(String name, String className)
    	 {
    		 this.name = name;
    		 this.className = className;
    		 try {
				this.argumentClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	 
    	 public Argument()
    	 {
    		 
    	 }
     }
     
     public void addArgument(String name, String className)
     {
    	 this.arguments.add(new Argument(name, className));
     }
     
     static class FileArgument
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
