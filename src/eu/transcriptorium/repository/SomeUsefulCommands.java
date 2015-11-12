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
	
	// java eu.transcriptorium.repository.RepositoryCL INVOKE BUILDLM '{script:"TestScripts/basicModelBuilding.sh",
	// conf:"TestScripts/test.settings.sh",OUTPUT:sortedFile}'
	static public Command getBasicLMBuildingCommand()
	{
		//Map<String, Object> m = new HashMap<String,Object>();

		Object[][] paramsWithFile = 
			{ 
					{ "script", "java.lang.String"},
				
					{ "conf", FileArgument, 
					Command.ioType.CONFIG, Command.referenceType.NAME},
					
					{"OUTPUT", FileArgument, Command.ioType.OUTPUT_DIRECTORY, 
						Command.referenceType.INSERT_INTO_CONFIG},  // vergeet niet hier passToCommand false te maken

					{ "languageModel", FileArgument, 
						Command.ioType.OUT,
						Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY},
						
					{ "dictionary", FileArgument, 
							Command.ioType.OUT,
							Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY}
			};

		ExternalCommand c1 = new ExternalCommand("bash", paramsWithFile);
		
		c1.formalParameters.get(3).baseName = "OUTPUT";
		c1.formalParameters.get(4).baseName = "OUTPUT";
		
		//m.clear();
		//m.put("f", new Integer(1));

		c1.addSRILMandHTKToPath();

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

	public Command getLmInterpolationCommand()
	{
		return null;
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
