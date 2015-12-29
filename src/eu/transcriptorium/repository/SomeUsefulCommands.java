package eu.transcriptorium.repository;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import eu.transcriptorium.lm.pfsg.LM2PFSG;
import eu.transcriptorium.servlet.LMBuilder;

/**
 * 
 * @author jesse
 * Most external command will have the form:
 * bash + script name + conf name
 */
public class SomeUsefulCommands 
{
	static final String FileArgument = Command.FileArgument.class.getName();


	/**
	 * Example invocation:
	 <pre>
	 java eu.transcriptorium.repository.RepositoryCL INVOKE BUILDLM 
	 '{script:"TestScripts/basicModelBuilding.sh",
	       conf:"TestScripts/test.settings.sh",
	       languageModel:"languageModel.lm",
	       dictionary:"dictionary.txt", 
	       OUTPUT:"blabla"}'
	 </pre>
	 * @return
	 */

	// java eu.transcriptorium.repository.RepositoryCL INVOKE BUILDLM '{script:"TestScripts/basicModelBuilding.sh",conf:"TestScripts/test.settings.sh",languageModel:"languageModel.lm",dictionary:"dictionary.txt", OUTPUT:"veryUsefulLanguageModel"}'

	static public Command getBasicLMBuildingCommand()
	{
		//Map<String, Object> m = new HashMap<String,Object>();

		Object[][] paramsWithFile = // hier moeten alle files in....
			{ 
					{ "script", FileArgument, Command.ioType.IN, Command.referenceType.NAME}, // nee.....

					{ "conf", FileArgument, 
						Command.ioType.CONFIG, Command.referenceType.NAME},

					{ "CHARSET", FileArgument, Command.ioType.IN, Command.referenceType.INSERT_INTO_CONFIG},


					{ "CORPUS", FileArgument, Command.ioType.IN, Command.referenceType.INSERT_INTO_CONFIG},

					{"OUTPUT", FileArgument, Command.ioType.OUTPUT_DIRECTORY, // we moeten hier een collectienaam aan kunnen koppelen
						Command.referenceType.INSERT_INTO_CONFIG},  // vergeet niet hier passToCommand false te maken

					{ "languageModel", FileArgument, 
							Command.ioType.OUT,
							Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY},

					{ "dictionary", FileArgument, 
								Command.ioType.OUT,
								Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY}
			};

		ExternalCommand c1 = new ExternalCommand("bash", paramsWithFile);

		c1.formalParameters.get(5).baseName = "OUTPUT";
		c1.formalParameters.get(6).baseName = "OUTPUT";


		c1.addSRILMandHTKToPath();
		// en voeg de nodige zaken toe aan classpath....
		return c1;
	}

	// java eu.transcriptorium.repository.RepositoryCL INVOKE TEST '{script:"TestScripts/testCommand.sh",conf:"TestScripts/test.settings.sh",OUTPUT:sortedFile}'

	public static Command getTestCommand()
	{
		Object[][] paramsWithFile = 
			{ 
					{ "script", "java.lang.String"},
					{ "conf", FileArgument, 
						Command.ioType.CONFIG, Command.referenceType.NAME},
					{ "OUTPUT", FileArgument, 
							Command.ioType.OUT,
							Command.referenceType.PICKUP_FROM_CONFIG},
			};

		ExternalCommand c1 = new ExternalCommand("bash", paramsWithFile);

		//m.clear();
		//m.put("f", new Integer(1));



		return c1;
	}
	public static Command getLM2PFSGCommand()
	{
		Object[][] args  = 
			{
					{"lm", FileArgument, Command.ioType.IN, Command.referenceType.ID},
					{"pfsg", FileArgument, Command.ioType.OUT,  Command.referenceType.ID}
			};
		LM2PFSG x = new LM2PFSG();
		JavaInternalCommand c = new JavaInternalCommand(x, "ngram2pfsg", args);
		return c;
	}

	public Command getLmInterpolationCommand(int k)
	{
		Object[][] paramsWithFile = 
			{ 
					{ "script", "java.lang.String"},

					{ "conf", FileArgument, 
						Command.ioType.CONFIG, Command.referenceType.NAME},

					{ "VALIDATION_FILE", FileArgument, Command.ioType.IN, Command.referenceType.INSERT_INTO_CONFIG},

					{ "MODEL_DESTINATION_DIR", FileArgument, Command.ioType.OUTPUT_DIRECTORY, 
						Command.referenceType.INSERT_INTO_CONFIG},  // vergeet niet hier passToCommand false te maken

					{ "languageModel", FileArgument, 
							Command.ioType.OUT,
							Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY},

					{ "dictionary", FileArgument, 
								Command.ioType.OUT,
								Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY}
			};

		ExternalCommand c1 = new ExternalCommand("/bin/bash", paramsWithFile);

		c1.formalParameters.get(4).baseName = "MODEL_DESTIONATION_DIR";
		c1.formalParameters.get(5).baseName = "MODEL_DESTINATION_DIR";

		for (int i=0; i < k; i++) // component models (?) (maar die komen niet in de conf?)
		{
			Command.FormalParameter f = new Command.FormalParameter();
			f.name = "COMPONENT_" + i;
			f.referenceType = Command.referenceType.INSERT_INTO_CONFIG;
			f.ioType = Command.ioType.INPUT_COLLECTION;
			c1.formalParameters.add(f);
		}

		c1.addSRILMandHTKToPath();

		return c1;
	}

	public static Map<String,Command> getBasicCommands()
	{
		Map<String,Command> m = new HashMap<String,Command>();
		m.put("BUILDLM",   SomeUsefulCommands.getBasicLMBuildingCommand());
		m.put("LM2PFSG", SomeUsefulCommands.getLM2PFSGCommand() );
		return m;
	}

	public static void main(String args[])
	{
		Repository r = new PostgresRepository(PostgresRepository.getDefaultProperties());
		r.clear();

		String scriptName = args[0];
		String confName = args[1];

		try
		{

			r.storeFile(new FileInputStream(confName), confName, null);
			Command c = getBasicLMBuildingCommand();

			Map<String, Object> m = new HashMap<String, Object>();

			m.put("script", scriptName);
			m.put("conf", confName);

			// nu is het probleem dat de variabelen uit de conf komen
			// en hoe pakken we ze weer op?

			c.invoke(m);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
