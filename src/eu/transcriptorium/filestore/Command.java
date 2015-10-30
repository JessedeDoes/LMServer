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
     
     public enum ioType
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
    	 ioType ioType;
    	 referenceType referenceType;
     }
     
     static class FileArgument
     {
    	 String pathName;
    	 int repositoryId;
     }
     
     List<Argument> arguments;

	public void invoke(Map<String, Object> arguments) throws IOException 
	{
		// TODO Auto-generated method stub
		
	}
     
     //List<FileArgument> inFiles;
     //List<FileArgument> outFiles;
}
