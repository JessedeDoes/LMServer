package eu.transcriptorium.repository;
import java.io.*;
import java.util.*;

import eu.transcriptorium.repository.Command.FormalParameter;
import eu.transcriptorium.repository.JavaInternalCommand.TestObject;
import eu.transcriptorium.util.StringUtils;

public class ExternalCommand extends JavaInternalCommand 
{
	static boolean verbose = true;
	String exe = null;
	
	List<String> pathEntries = new ArrayList<String>();
	static String PATH_SEPARATOR = ":";
	
	static String SRILM_DIR="/home/jesse/Tools/srilm";
	static String HTK_DIR="/home/jesse/Tools/htk3-4-Atros";
	
	public ExternalCommand(String commandName, Object[][] args)
	{
		this.formalParameters = FormalParameter.makeArgumentList(args);
		this.exe = commandName;
	}

	public String[] buildCommand(List<FormalParameter> formalParameters, Object[] args)
	{
		List<String> a = new ArrayList<String>();
		a.add(exe);
		
		// sommige parameters komen niet in de command string terecht
		// (de bestanden die je ophaalt uit een output folder)
		
		for (int i=0; i < args.length; i++)
		{
			FormalParameter p = formalParameters.get(i);
			if (!p.passToCommand)
				continue;
			String flag = p.flagName;
			if (flag != null)
				a.add("-" + flag);
			a.add(args[i].toString());
		}
		String[] r = new String[a.size()];
		for (int i=0; i < a.size(); i++)
			r[i] = a.get(i);
		return r;
	}
	
	public void buildEnvironment(Map<String,String> env, List<FormalParameter> formalParameters, Object[] args)
	{
		for (int i=0; i < formalParameters.size(); i++)
		{
			env.put(formalParameters.get(i).name, args[i].toString());
		}
	}
	
	public void addToPath(String pathName)
	{
		pathEntries.add(pathName);
	}
	
	@Override
	protected Object invokeCommand(List<FormalParameter> formalParameters, Object[] args)
	{
		
		try
		{	
			String[] command  = buildCommand(formalParameters,args);
			ProcessBuilder pb  = new ProcessBuilder(command);

			Map<String, String> env = pb.environment();
			
			addPathToEnvironment(env);
			
			//env.put("PATH", programDir + "/");
			//pb.directory(new File(programDir));

			pb.redirectErrorStream(true);


			final Process process = pb.start();
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String line;
						final InputStream stdout = process.getInputStream();
						BufferedReader brCleanUp = new BufferedReader(
								new InputStreamReader(stdout));
						while ((line = brCleanUp.readLine()) != null)
						{
							if (verbose)
								System.err.println("[Stdout] " + line);
						}
						brCleanUp.close();
					} catch (IOException e)
					{
						e.printStackTrace(System.err);
					}
				}
			}).start();
			process.waitFor();
		} catch (Exception e)
		{
			e.printStackTrace();
			// TODO: handle exception
		}
		return null;
	}
	
	private void addPathToEnvironment(Map<String, String> env) 
	{
		// TODO Auto-generated method stub
		String currentPath = env.get("PATH");
		System.err.println("PATH: " + currentPath);
		if (currentPath == null)
			currentPath="";
		if (currentPath.length() > 0 && this.pathEntries.size() > 0)
			currentPath += PATH_SEPARATOR;
		currentPath += StringUtils.join(this.pathEntries, PATH_SEPARATOR);
		env.put("PATH", currentPath);
	}

	public static void main(String[] args) throws IOException
	{
		String FA = Command.FileArgument.class.getName();
		
		
		Map<String, Object> m = new HashMap<String,Object>();
		
		
		Object[][] paramsWithFile = 
		{ 
				{ "f", Command.FileArgument.class.getName(), Command.ioType.IN, Command.referenceType.ID} 		
		};
		
		ExternalCommand c1 = new ExternalCommand("cat", paramsWithFile);
		m.clear();
		m.put("f", new Integer(1));
		
		c1.addToPath(SRILM_DIR + "/bin");
		c1.addToPath(SRILM_DIR + "/bin/i686-m64");
		c1.addToPath(HTK_DIR + "/bin.linux");
		c1.invoke(m);
	}
}
