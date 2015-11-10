package tobeintegrated.eu.transcriptorium.jafar.disagree;
import java.io.IOException;


public class LanguageBuliding extends Thread {
	public String Output;
	public String Cutoff;
	public String Requiredprogramfolder;
	
	
	public void run(){
		languagemodelingprompt LM= new languagemodelingprompt();
		try {
			LM.RunLanguageModel(Output, Cutoff,Requiredprogramfolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
