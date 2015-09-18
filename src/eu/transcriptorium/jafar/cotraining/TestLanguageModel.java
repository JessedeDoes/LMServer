package eu.transcriptorium.jafar.cotraining;

import java.io.IOException;

public class TestLanguageModel extends Thread
{
	public int firstIndex;
	public int lastIndex;
	public String[] FileList;
	public String Model;
	public String Output;
	public String OutputType;

	public void run()
	{
		LanguageModelingPrompt LM = new LanguageModelingPrompt();
		for (int i = firstIndex; i <= lastIndex; i++)
		{
			// Get filename of file or directory
			String filename = "/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized/"
					+ FileList[i];
			String outFile = Output + OutputType + FileList[i] + ".log";
			// System.out.println("outFile=== "+outFile);
			// System.out.println("Starting pointtttttttttttttttttttttt");
			try
			{
				LM.testPPLCode(Model, filename, outFile);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("Eindigeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		}

	}

}
