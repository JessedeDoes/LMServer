package tobeintegrated.eu.transcriptorium.jafar.cotraining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is the main class for jafar's TClean.jar
 * @author jafar
 *
 */
public class TextCleaning
{

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub

		// "/mnt/Projecten/transcriptorium/Corpora/ECCO/LPL_For_Each_Doc"

		if (args == null)
		{
			System.out.println("Please identify the parameters!");
			System.out
					.println("Use AgreeCo.jar SelectionPercent NumberIterations Confidence InDomainF OutDominF \n"
							+ " ECCONormalizedDomainF ECCOCleanedDomainF OutputInF OutputOutF NameofSelectedSamples.txt TypeOfCriterion");
			System.out
					.println("where SelectionPersent: is the percentage that the algorithm selects at each iteration."
							+ " \n NumberIterations: is the number of Iterations."
							+ " \n Confidence: is a thershold for confidence. "
							+ "\n InDomainF: is the folder of In domain LM and Data. "
							+ "\n OutDominF: is the folder of Out domain LM and Data."
							+ "\n ECCONormalizedDomainF: is the folder of ECCO Normlaizd data."
							+ "\n ECCOCleanedDomainF: is the folder of ECCO Cleaned data. "
							+ "\n OutputInF: is the output of in-domain LM. "
							+ "\n OutputOutF: is the output of out-domain LM. "
							+ "\n NameofSelectedSamples: the resultin sample selection file."
							+ "\n TypeOfCriterion: Select the type of criterion 1- for adding PPL + OOVs, 2- PPL*OOVs, 3- (1/PPL)*(1-OOVs).");
		} else if (args.length < 5)
		{
			System.out.println("You have used wrong parameters!");
			System.out
					.println("Use AgreeCo.jar SelectionPersent NumberIterations Confidence InDomainF OutDominF \n "
							+ "ECCODomainF OutputInF OutputOutF NameofSelectedSamples.txt TypeOfCriterion");

		} else if (args.length == 5)
		{
			File dir = new File(args[0]);
			String[] children = dir.list();

			File dirCleaned = new File(args[2]);
			String[] childrenCleaned = dirCleaned.list();

			File dirNormalized = new File(args[3]);
			String[] childrenNormalized = dirCleaned.list();

			if ((children == null) || (childrenCleaned == null)
					|| (childrenNormalized == null))
			{
				System.out.println("The folder does not exist");
				System.out.println("dir=" + dir.getPath() + "\n cleaned=="
						+ dirCleaned.getPath() + "\n dirNormalized==="
						+ dirNormalized.getPath());

			} else
			{

				String OutputNormalizedFiles = dirNormalized.getPath();// args[3]
				String OutputCleanedFiles = dirCleaned.getPath(); // args[2]

				CleanedAndNormalizedText(children, args[1], OutputCleanedFiles,
						OutputNormalizedFiles, args[0], args[4]);

			}

			System.out
					.println("           ********                 End of the process!!!                     ***********");

		} else
		{
			System.out.println("Wrong  number of parameters!");
		}

	}

	private static void CleanedAndNormalizedText(String[] children,
			String Characterset, String DirectoryPathCleaned,
			String DirectoryPathNormalized, String DirectoryPath, String WPNPath)
			throws IOException
	{
		// TODO Auto-generated method stub

		String OutputCleanedFiles = DirectoryPathCleaned + "/";
		File dir = new File(DirectoryPathCleaned);

		for (int i = 0; i < children.length; i++)
		{
			// Get filename of file or directory
			String filename = DirectoryPath + children[i];
			String outFile = OutputCleanedFiles + children[i] + ".cl";
			// System.out.println("outFile============= "+outFile);
			RunningWPocCode(filename, Characterset, outFile, WPNPath);
		}

		// Normalizing the cleaned Text
		children = dir.list();
		String OutputNormalizedFiles = DirectoryPathNormalized + "/";
		if (children == null)
		{
			System.out.println("The directory does not exist!");
		} else
		{
			for (int i = 0; i < children.length; i++)
			{
				// Get filename of file or directory
				String filename = OutputCleanedFiles + children[i];
				String outFile = OutputNormalizedFiles + children[i] + ".norm";
				// System.out.println("outFile============= "+outFile);
				RunNormalizedCode(filename, outFile, WPNPath);

			}
		}

	}

	public static void RunNormalizedCode(String inputFile, String output,
			String wPNPath) throws IOException
	{
		// TODO Auto-generated method stub

		String FunctionSource = wPNPath;
		String[] cmd = { "/usr/bin/java", "-Dfile.encoding=utf8", "-jar",
				FunctionSource + "WNorm.jar", inputFile, output };

		Process p = Runtime.getRuntime().exec(cmd);

		InputStream stdin = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		// read the output from the command
		// System.out.println("Here is the standard output of the WNorm program:\n");
		String s;
		while ((s = stdInput.readLine()) != null)
		{
			System.out.println(s);
		}
	}

	public static void RunningWPocCode(String inputFile, String CharacterSet,
			String outFile, String wPNPath) throws IOException
	{
		// TODO Auto-generated method stub

		String FunctionSource = wPNPath;
		String[] cmd = { "/usr/bin/java", "-Dfile.encoding=utf8", "-jar",
				FunctionSource + "WProc.jar", inputFile, CharacterSet, outFile };

		Process p = Runtime.getRuntime().exec(cmd);

		InputStream stdin = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		// read the output from the command
		// System.out.println("Here is the standard output of the WProc program:\n");
		String s;
		while ((s = stdInput.readLine()) != null)
		{
			System.out.println(s);
		}

		// read any errors from the attempted command
		// System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null)
		{
			System.out.println(s);
		}

	}

}
