package tobeintegrated.eu.transcriptorium.jafar.cotraining;

import java.io.IOException;

public class LanguageModelBuilding extends Thread
{
	public String Output;
	public String Cutoff;

	public void run()
	{
		LanguageModelingPrompt LM = new LanguageModelingPrompt();
		try
		{
			LM.RunLanguageModel(Output, Cutoff);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
