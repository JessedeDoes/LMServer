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
     
     static class FileArgument
     {
    	 String pathName;
    	 int repository_id;
     }
     
     List<FileArgument> inFiles;
     List<FileArgument> outFiles;
     
     
}
