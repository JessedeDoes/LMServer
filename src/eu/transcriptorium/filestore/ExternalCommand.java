package eu.transcriptorium.filestore;
import java.io.*;
import java.util.*;

import eu.transcriptorium.filestore.Command.Argument;

public class ExternalCommand extends JavaInternalCommand 
{
	static boolean verbose = true;
	String exe = null;
	
	public ExternalCommand(String commandName, Object[][] args)
	{
		this.arguments = Argument.makeArgumentList(args);
		this.exe = commandName;
	}
	
	public String[] buildCommand(List<Argument> arguments, Object[] args)
	{
		List<String> a = new ArrayList<String>();
		a.add(exe);
		for (int i=0; i < args.length; i++)
		{
			String flag = arguments.get(i).flagName;
			if (flag != null)
				a.add("-" + flag);
			a.add(args[i].toString());
		}
		String[] r = new String[a.size()];
		for (int i=0; i < a.size(); i++)
			r[i] = a.get(i);
		return r;
	}
	@Override
	protected Object invokeCommand(List<Argument> arguments, Object[] args)
	{
	
            try
            {	
            		String[] command  = buildCommand(arguments,args);
                    ProcessBuilder pb  = new ProcessBuilder(command);

                    Map<String, String> env = pb.environment();
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
                                                    // als regel "examples read " bevat, kan
                                                    // je de trainingFile weggooien...
                                                    if (line.contains("examples read"))
                                                    {
                                                            System.err.println("deleting combined training file...");
                                                            tf.delete();
                                                    }
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

}
