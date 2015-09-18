package eu.transcriptorium.jafar.cotraining;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class LanguageModeling {


	public static void main(String[] args) throws  IOException, InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Testtttttttttttttttt");
//      String[] cmd = { "/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64/bash","/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64/ngram-count","-order","2", "-kndiscount","-text","/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt", "-lm","/mnt/Projecten/transcriptorium/Tools/languagemodeling/javagenerate.lm" };
     
 //    String[] cmd = {"/mnt/Projecten/transcriptorium/Tools/mallet-2.0.7/bin/mallet","import-file","--input","/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt","--output", "/mnt/Projecten/transcriptorium/Tools/languagemodeling/malletdata.mallet","--keep-sequence"};

 //     String[] cmd = {"/mnt/Projecten/transcriptorium/Tools/SRILM/bin/i686-m64/ngram-count","-order","2", "-kndiscount","-text","/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt","-lm","/mnt/Projecten/transcriptorium/Tools/languagemodeling/javagenerate.lm" };
		String InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt",
				Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";

//		NgramCount(InputFile,Output);
		
		Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";
		InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt";
		//RunningWPocCode(InputFile,Output);
		
		InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/cleanedText.txt";
		Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";

//		RunNormalizedCode(InputFile,Output);
		
		String cutoff="1";
		InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/normalizedText.txt";
		Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";
//		RunWListCode(InputFile,Output,cutoff);

		
		String InputOutput="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";

		RunWDicCode(InputOutput);
		


		
	  //    System.out.println("outputttttttttttttttttttttttttttttttt== "+System.console());

	}

	public static void RunWDicCode(String wordList) throws IOException {
		// TODO Auto-generated method stub
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		String[] cmd = {"/usr/bin/java","-Dfile.encoding=utf8","-jar",FunctionSource+"WDic.jar",
				wordList+"csWordList.txt",wordList+"csSortedWordList.txt",wordList+"dictionary.txt"};
	      
	      Process p = Runtime.getRuntime().exec(cmd);
	    
	      InputStream stdin = p.getInputStream();
	      InputStreamReader isr = new InputStreamReader(stdin);
	      BufferedReader br = new BufferedReader(isr);
	      BufferedReader stdInput = new BufferedReader(new 
	              InputStreamReader(p.getInputStream()));

	         BufferedReader stdError = new BufferedReader(new 
	              InputStreamReader(p.getErrorStream()));

	         // read the output from the command
//	        System.out.println("Here is the standard output of the WList program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }
			
	}

	

	public static void RunWListCode(String inputFile, String output, String cutoff) throws IOException {
		// TODO Auto-generated method stub
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		String[] cmd = {"/usr/bin/java","-Dfile.encoding=utf8","-jar",FunctionSource+"WList.jar","-i",
				inputFile,"-o",output+"csWordList.txt","-n",cutoff,"-s",output+"csSortedWordList.txt"};
	      
	      Process p = Runtime.getRuntime().exec(cmd);
	    
	      InputStream stdin = p.getInputStream();
	      InputStreamReader isr = new InputStreamReader(stdin);
	      BufferedReader br = new BufferedReader(isr);
	      BufferedReader stdInput = new BufferedReader(new 
	              InputStreamReader(p.getInputStream()));

	         BufferedReader stdError = new BufferedReader(new 
	              InputStreamReader(p.getErrorStream()));

	         // read the output from the command
//	         System.out.println("Here is the standard output of the WList program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }
			
	}

	public static void RunNormalizedCode(String inputFile, String output) throws IOException {
		// TODO Auto-generated method stub
		
		
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
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

	public static void RunningWPocCode(String inputFile, String output) throws IOException {
		// TODO Auto-generated method stub
	
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		String[] cmd = {"/usr/bin/java","-Dfile.encoding=utf8","-jar",FunctionSource+"WProc.jar",
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
