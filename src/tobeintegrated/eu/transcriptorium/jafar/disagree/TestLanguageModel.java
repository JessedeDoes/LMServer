package tobeintegrated.eu.transcriptorium.jafar.disagree;
import java.io.IOException;


public class TestLanguageModel extends Thread {
	public int firstIndex;
	public int lastIndex;
	public String[] FileList;
	public String Model;
	public String Output;
	public String OutputType;
	public String ResourceFolder;
	public String Requiredprogramfolder;
	
	public void run(){
		languagemodelingprompt LM= new languagemodelingprompt();
		for (int i=firstIndex; i<=lastIndex; i++) {
            // Get filename of file or directory
            String filename = ResourceFolder+"/"+FileList[i];
      //      filename=filename.substring(0, filename.length()-1);
            
      //      System.out.println("testttttttttttttttttt"+ filename);
            String outFile=Output+OutputType+FileList[i]+".log";
  //          System.out.println("outFile=== "+outFile);
  //          System.out.println("Starting pointtttttttttttttttttttttt");
            try {
				LM.testPPLCode(Model,filename,outFile,Requiredprogramfolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   //         System.out.println("Eindigeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        }

	}

}
