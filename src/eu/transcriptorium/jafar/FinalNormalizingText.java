package eu.transcriptorium.jafar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * 
 * @author jafar Dit (=WNorm.jar) lijkt gewoon te uppercasen, bovendien niet op
 *         een correctie manier (karakters met diacritische tekens gaan fout) Wat had ik hiermee ook alweer
 *         gedaan voor de Reichsgericht etc data? Gewoon vervangen door  simpele uppercaser
  */
public class FinalNormalizingText
{

	public static String ToUpperCase(String line)
	{

		StringBuilder name = new StringBuilder(line);
		for (int i = 0; i < name.length(); i++)
		{
			if (name.charAt(i) >= 'a' && name.charAt(i) <= 'z')
				name.setCharAt(i, (char) (name.charAt(i) - 32));

		}
		return name.toString();
	}

	public static void main(String[] args) throws IOException
	{

		if (args == null)
		{
			System.out.println("please identy the parameters.....");
			System.out.println("Use WNorm.jar  inputfile outputfile \n\n ");
		} else if (args.length < 2)
		{
			System.out.println(" You have used a wrong parametr!\n");
			System.out
					.println(" Please use WNrom.jar  inputfile outputfile \n");
		} else
		{
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			System.out
					.println("\n\n \t \t Normalizing of the text is started! and It takes a few seconds!");
			// PrintWriter out = new PrintWriter(new BufferedWriter(new
			// FileWriter(args[1])));

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(args[1]),
							"UTF8")));
			// out.write(getString());

			try
			{
				String line = br.readLine();
				// String SampleStringForMatch="[^XYZ]";
				while (line != null)
				{
					line = ToUpperCase(line);
					/*
					 * String[] NWrod = line.split(" "); for ( String words :
					 * NWrod) { String W=words; if( !(W.contains("X") ||
					 * W.contains("Y") || W.contains("Z"))){ //
					 * System.out.println
					 * ("correcttttttttttttttttttttt ===    "+W);
					 * 
					 * out.print(W+" "); }
					 * 
					 * }//for
					 */
					out.println(line);
					line = br.readLine();
				}
				br.close();
				out.flush();
				out.close();
			} finally
			{
				// System.out.println("Errorrrrrrrrrrrrrrrrrrrr");
			}

			System.out.println("\n \t\t It's done! and the results are  in: "
					+ args[1] + "  file");

		}

	}

}
