package tobeintegrated.eu.transcriptorium.jafar.disagree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class languagemodelingprompt {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		String InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt",
				Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/";
//		NgramCount(InputFile,Output);
		
		
		InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/ban.txt";
		Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection";
		String cutoff="1";
	//	RunLanguageModel(InputFile,Output,cutoff);
		
			
		InputFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection";

		RunHBuildCode(InputFile);
		
		


	}

	public static void NgramCount(String inputFile, String Output) throws IOException {
		// TODO Auto-generated method stub
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/SRILM/bin";
		String[] cmd = {FunctionSource+"/i686-m64/ngram-count","-sort","-order","2", "-kndiscount","-text",inputFile,"-write",Output+"count.txt.gz" };

	       
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
	         System.out.println("Here is the standard output of the NgramCount program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }

	         // read any errors from the attempted command
	         System.out.println("Here is the standard error of the command (if any):\n");
	         while ((s = stdError.readLine()) != null) {
	             System.out.println(s);
	         }
	      System.out.println("outputttttttttttttttttttttttttttttttt== "+System.console());

		
	}

	public static void RunHBuildCode(String inputFile) throws IOException {
		// TODO Auto-generated method stub
		
		
		String FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		String[] cmd = {"bash",FunctionSource+"hbuildLM.sh",inputFile};

	       
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
	         System.out.println("Here is the standard output of the NgramCount program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }

	         // read any errors from the attempted command
	         System.out.println("Here is the standard error of the command (if any):\n");
	         while ((s = stdError.readLine()) != null) {
	             System.out.println(s);
	         }
	      System.out.println("outputttttttttttttttttttttttttttttttt== "+System.console());

	}

	public static void RunLanguageModel(String inputFile,  String cutoff,String FunctionSource) throws IOException {
		// TODO Auto-generated method stub
		FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
		String[] cmd = {"bash",FunctionSource+"trainingLMJava.sh",inputFile,cutoff};

	       
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
	 //        System.out.println("Here is the standard output of the NgramCount program:\n");
	        String s;
			while ((s  = stdInput.readLine()) != null) {
	             System.out.println(s);
	         }

	         // read any errors from the attempted command
	         System.out.println("Here is the standard error of the command (if any):\n");
	         while ((s = stdError.readLine()) != null) {
	             System.out.println(s);
	         }
	      System.out.println("output== "+System.console());

	}
	public static void testPPLCode(String model, String inputFile ,
			String output, String FunctionSource) throws IOException {
		// TODO Auto-generated method stub
		FunctionSource="/mnt/Projecten/transcriptorium/Tools/languagemodeling/";
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
