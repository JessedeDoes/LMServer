package eu.transcriptorium.repository;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author jesse
 * Most external command will have the form:
 * bash + script name + conf name
 */
public class SomeUsefulCommands 
{
	static public Command getBasicLMBuildingCommand()
	{
		String FA = Command.FileArgument.class.getName();
		
		
		//Map<String, Object> m = new HashMap<String,Object>();
		
		Object[][] paramsWithFile = 
		{ 
				{ "script", "java.lang.String"},
				{ "conf", Command.FileArgument.class.getName(), 
					Command.ioType.IN, Command.referenceType.NAME} 		
		};
		
		ExternalCommand c1 = new ExternalCommand("bash", paramsWithFile);
		//m.clear();
		//m.put("f", new Integer(1));
		
		c1.addToPath(ExternalCommand.SRILM_DIR + "/bin");
		c1.addToPath(ExternalCommand.SRILM_DIR + "/bin/i686-m64");
		c1.addToPath(ExternalCommand.HTK_DIR + "/bin.linux");
		
		return c1;
	}
	
	public Command getLM2PFSGCommand()
	{
		return null;
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
