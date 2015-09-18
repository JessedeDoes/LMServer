package eu.transcriptorium.jafar.cotraining;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class SampleSelection {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
		String InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/",Model = null;
		
		
		
		File dir = new File("/mnt/Projecten/transcriptorium/Corpora/ECCO/LPL_For_Each_Doc");
		LanguageModeling Processing = new LanguageModeling();
        String[] children = dir.list();
        
        String OutputNormalizedFiles="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized";
      //  CleanedAndNormalizedText(children,OutputNormalizedFiles,Processing);
        
        
        dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized");
        children=dir.list();
        
        if (children == null) {
               System.out.println("The directory does not exist!" );   
        } else {
             System.out.println("\n\n \t \t Text prepocessing of the original text is started! and It takes a few seconds!");
       //      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("d:/eccomerged.txt")));
            Model="/mnt/Projecten/transcriptorium/Tools/languagemodeling/InBentham";
            for (int i=0; i<children.length; i++) {
                // Get filename of file or directory
                String filename = "/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized/"+children[i];
    //            Processing.RunningWPocCode(inputFile, output);
                String outFile=Output+"ppl_in_"+children[i]+".log";
                System.out.println("outFile=== "+outFile);
                System.out.println("Starting pointtttttttttttttttttttttt");
                testPPLCode(filename,outFile,Model);
                System.out.println("Eindigeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            }

        }
         
        
		
		
	}

	private static void CleanedAndNormalizedText(String[] children,
			String outputNormalizedFiles, LanguageModeling processing) throws IOException {
		// TODO Auto-generated method stub
       String OutputCleanedFiles="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText/";
       File dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText");

       if (children == null) {
            System.out.println("The directory does not exist!" );   
       } else {
    	 for (int i=0; i<children.length; i++) {
             // Get filename of file or directory
             String filename = "/mnt/Projecten/transcriptorium/Corpora/ECCO/LPL_For_Each_Doc/"+children[i];
             String outFile=OutputCleanedFiles+children[i]+".cl";
//             System.out.println("outFile============= "+outFile);
             LanguageModeling.RunningWPocCode(filename, outFile);
    	 }
       }
      
       //Normalizing the cleaned Text
      children = dir.list();
      OutputCleanedFiles="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized/";
      if (children == null) {
          System.out.println("The directory does not exist!" );   
      } 
      else {
    	 for (int i=0; i<children.length; i++) {
             // Get filename of file or directory
             String filename = "/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText/"+children[i];
             String outFile=OutputCleanedFiles+children[i]+".norm";
//             System.out.println("outFile============= "+outFile);
             LanguageModeling.RunNormalizedCode(filename, outFile);
    	 
    	 }
      }
        
	}

	public static void testPPLCode(String model, String inputFile ,
			String output) throws IOException {
		// TODO Auto-generated method stub
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		//System.out.println();
		String[] cmd = {"bash",FunctionSource+"testPPLJava.sh",model,inputFile,output};

	       
	    Process p = Runtime.getRuntime().exec(cmd);
	     
	  //    p.waitFor();
	      InputStream stdin = p.getInputStream();
	      InputStreamReader isr = new InputStreamReader(stdin);
	      BufferedReader br = new BufferedReader(isr);

	      BufferedReader stdInput = new BufferedReader(new 
	              InputStreamReader(p.getInputStream()));

	         BufferedReader stdError = new BufferedReader(new 
	              InputStreamReader(p.getErrorStream()));

	         // read the output from the command
//	         System.out.println("Here is the standard output of the NgramCount program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }

	         // read any errors from the attempted command
//	         System.out.println("Here is the standard error of the command (if any):\n");
	         while ((s = stdError.readLine()) != null) {
	             System.out.println(s);
	         }
	}

}
