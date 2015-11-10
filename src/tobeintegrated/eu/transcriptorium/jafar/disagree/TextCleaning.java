package tobeintegrated.eu.transcriptorium.jafar.disagree;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TextCleaning {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

//"/mnt/Projecten/transcriptorium/Corpora/ECCO/LPL_For_Each_Doc"
		
		if(args==null)
		{
			System.out.println("Please identify the parameters!");
			System.out.println("Use TClean.jar InputFolder CharacterSet.txt Output_CleanedTextFolder " +
					"Output_NormalizedAndCleanedTextFolder RequiredProgramsFolder \n" );
			System.out.println("where \n" +
					" \n InputFolder: is the folder of resources." +
					" \n CharacterSet: is a text file that shows the applicable chacatersets for preprocessing" +
					"\n Output_CleanedTextFolder: is the folder for cleaned text " +
					"\n OutDominF: is the folder of Out domain LM and Data." +
					"\n Output_NormalizedAndCleanedTextFolder: is the folder for Normlaizd and cleaned resources." +
					"\n RequiredProgramsFolder: is the folder of required .jar files for preprocessing like WProc.jar and WNorm.jar." +
					" Note that java program is in /usr/bin/java directory"					 );
		}
		else if(args.length<5){
			System.out.println("You have used wrong parameters!");
			System.out.println("Use TClean.jar InputFolder CharacterSet.txt Output_CleanedTextFolder " +
					"Output_NormalizedAndCleanedTextFolder RequiredProgramsFolder \n");
			System.out.println("where \n" +
					" \n InputFolder: is the folder of resources." +
					" \n CharacterSet: is a text file that shows the applicable chacatersets for preprocessing" +
					"\n Output_CleanedTextFolder: is the folder for cleaned text " +
					"\n OutDominF: is the folder of Out domain LM and Data." +
					"\n Output_NormalizedAndCleanedTextFolder: is the folder for Normlaizd and cleaned resources." +
					"\n RequiredProgramsFolder: is the folder of required .jar files for preprocessing like WProc.jar and WNorm.jar. \n" +
					" Note that java program is in /usr/bin/java directory"					 );

		}
		else if(args.length==5){
			File dir = new File(args[0]);
	        String[] children = dir.list();
	        
	        File dirCleaned= new File(args[2]);
	        String[] childrenCleaned=dirCleaned.list();
	        
	        File dirNormalized= new File(args[3]);
	        String[] childrenNormalized=dirCleaned.list();
	        
	        if((children==null)|| (childrenCleaned==null) || (childrenNormalized==null)){
	        	System.out.println("The folder does not exist");
	        	System.out.println("dir="+dir.getPath()+"\n cleaned=="+dirCleaned.getPath()+"\n dirNormalized==="+dirNormalized.getPath());
	        	
	        }
	        else{
	        
	             String OutputNormalizedFiles=dirNormalized.getPath();//args[3]
	             String OutputCleanedFiles=dirCleaned.getPath(); //args[2]
	             
	             CleanedAndNormalizedText(children,args[1],OutputCleanedFiles,OutputNormalizedFiles,args[0],args[4]);
	        
	        }
	        
	        System.out.println("           ********                 End of the process!!!                     ***********");
	        			
		}
		else {
			System.out.println("Wrong  number of parameters!");
			System.out.println("Use TClean.jar InputFolder CharacterSet.txt Output_CleanedTextFolder " +
					"Output_NormalizedAndCleanedTextFolder RequiredProgramsFolder \n");
			System.out.println("where \n" +
					" \n InputFolder: is the folder of resources." +
					" \n CharacterSet: is a text file that shows the applicable chacatersets for preprocessing" +
					"\n Output_CleanedTextFolder: is the folder for cleaned text " +
					"\n OutDominF: is the folder of Out domain LM and Data." +
					"\n Output_NormalizedAndCleanedTextFolder: is the folder for Normlaizd and cleaned resources." +
					"\n RequiredProgramsFolder: is the folder of required .jar files for preprocessing like WProc.jar and WNorm.jar." +
					" Note that java program is in /usr/bin/java directory"					 );


		}
				
	}

	private static void CleanedAndNormalizedText(String[] children,
			String Characterset, String DirectoryPathCleaned, String DirectoryPathNormalized, String DirectoryPath, String WPNPath) throws IOException {
		// TODO Auto-generated method stub
		
       String OutputCleanedFiles=DirectoryPathCleaned+"/";
       File dir = new File(DirectoryPathCleaned);

       for (int i=0; i<children.length; i++) {
             // Get filename of file or directory
             String filename = DirectoryPath+children[i];
             String outFile=OutputCleanedFiles+children[i]+".cl";
//             System.out.println("outFile============= "+outFile);
             RunningWPocCode(filename, Characterset,outFile,WPNPath);
    	 }
       
      
       //Normalizing the cleaned Text
      children = dir.list();
      String OutputNormalizedFiles=DirectoryPathNormalized+"/";
      if (children == null) {
          System.out.println("The directory does not exist!" );   
      } 
      else {
    	 for (int i=0; i<children.length; i++) {
             // Get filename of file or directory
             String filename = OutputCleanedFiles+children[i];
             String outFile=OutputNormalizedFiles+children[i]+".norm";
//             System.out.println("outFile============= "+outFile);
             RunNormalizedCode(filename, outFile,WPNPath);
    	 
    	 }
      }
        
	}

	public static void RunNormalizedCode(String inputFile, String output, String wPNPath) throws IOException {
		// TODO Auto-generated method stub
		
		
		String FunctionSource=wPNPath;
		String[] cmd = {"/usr/bin/java","-Dfile.encoding=utf8","-jar",FunctionSource+"WNorm.jar",
				inputFile,output };

	       
	      Process p = Runtime.getRuntime().exec(cmd);
	     
	      InputStream stdin = p.getInputStream();
	      InputStreamReader isr = new InputStreamReader(stdin);
	      BufferedReader br = new BufferedReader(isr);
	      BufferedReader stdInput = new BufferedReader(new 
	              InputStreamReader(p.getInputStream()));

	         BufferedReader stdError = new BufferedReader(new 
	              InputStreamReader(p.getErrorStream()));

	         // read the output from the command
	   //      System.out.println("Here is the standard output of the WNorm program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }
	}

	public static void RunningWPocCode(String inputFile, String CharacterSet, String outFile, String wPNPath) throws IOException {
		// TODO Auto-generated method stub
	
		String FunctionSource=wPNPath;
		String[] cmd = {"/usr/bin/java","-Dfile.encoding=utf8","-jar",FunctionSource+"WProc.jar",
				inputFile,CharacterSet,outFile };

	       
	      Process p = Runtime.getRuntime().exec(cmd);
	     
	      InputStream stdin = p.getInputStream();
	      InputStreamReader isr = new InputStreamReader(stdin);
	      BufferedReader br = new BufferedReader(isr);
	      BufferedReader stdInput = new BufferedReader(new 
	              InputStreamReader(p.getInputStream()));

	         BufferedReader stdError = new BufferedReader(new 
	              InputStreamReader(p.getErrorStream()));

	         // read the output from the command
	 //        System.out.println("Here is the standard output of the WProc program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }

	         // read any errors from the attempted command
	//         System.out.println("Here is the standard error of the command (if any):\n");
	         while ((s = stdError.readLine()) != null) {
	             System.out.println(s);
	         }
		
	}

}
