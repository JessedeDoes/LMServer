package eu.transcriptorium.repository;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 * @author jesse
 *
 *<p>
 *A "general" mechanism to be able to execute both command line applications and java methods on files in such a way that we can
 *also run on files in the repository and store results in the repository.
 *
 *
 *<p>
 *The messy bit is passing parameters.
 *
 *<p>
 *A Command has as set of parameters, which
 *<ul>
 *	<li> may not be files. In this case they can simply be passed through
 *	<li> may be files
 *		<ul>
 *			<li>Input files
 *				<ul>
 *					<li>Specified by repository id
 *					<li>Specified by repository filename attribute
 *				</ul>
 *			<li>Output files. Need to be stored in the repository. 
 				The filename specified for the parameter is stored in the repository together with the file.
 			<li>Configuration files. Are passed as input to the command like the ordinary input files. 
 			Besides, read the properties into the command environment Properties object. 
 			After parameter instantiation, variable expansion on the environment.
 *		</ul>
 *	<li> May be temporary output directories. The created temp directory is passed to the command line process. 
 *Information about the name should also be kept in the environment. 
 *Should be deleted after successful command completion.
 *</ul>
 *
 *Some parameters (notably output files to be picked up from a directory) are not passed to the command line process.
 *
 */
public class Command
{
	Repository repository = new PostgresRepository(PostgresRepository.getDefaultProperties());
	static Pattern variablePattern = Pattern.compile("\\$\\{[^{}]*\\}");
	public String commandName;
	List<FormalParameter> expectedParameters = null;
	Properties environment = null;
	
	public enum type
	{
		SHELL,
		JAVA
	} ;

	static public enum ioType
	{
		IN,
		OUT,
		OPTIONS,
		TEMP_DIR // directory arguments are always temporary (?)
	};

	static public enum referenceType
	{
		NAME,
		ID,
		RELATIVE_TO_TEMPDIR
	};

	type commandType;

	static class FormalParameter
	{
		String name;
		String className;
		Class argumentClass;
		ioType ioType = Command.ioType.IN;
		referenceType referenceType;
		String flagName = null;
		String baseName;
		boolean passToCommand =  true;
		
		public String toString()
		{
			return "name=" + name + "; class=" + className;
		}

		public FormalParameter(String name, String className)
		{
			this.name = name;
			this.className = className;
			try 
			{
				this.argumentClass = Class.forName(className);
			} catch (ClassNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public FormalParameter()
		{

		}

		public FormalParameter(Object[] x)
		{
			this((String) x[0], (String) x[1]);
			if (x.length > 2)
				this.ioType = (ioType) x[2];
			if (x.length > 3)
				this.referenceType = (referenceType) x[3];
		}

		public static List<FormalParameter> makeArgumentList(Object[][] x)
		{
			List<FormalParameter> l = new ArrayList<FormalParameter>();
			for (Object [] y: x)
				l.add(new FormalParameter(y));
			return l;
		}
	}

	public void addArgument(String name, String className)
	{
		this.expectedParameters.add(new FormalParameter(name, className));
	}

	public static class FileArgument
	{
		String pathName;
		int repositoryId;
		String basePathName;
	}



	protected Object invokeCommand(List<FormalParameter> formalParameters, Object[] args) throws IllegalAccessException, 
	InvocationTargetException 
	{
		return null;
	}
	

	public void invoke(Map<String, Object> actualParameters) throws IOException
	{
		Object[] args = new Object[this.expectedParameters.size()];
		environment = new Properties();
		
		for (int i=0; i < this.expectedParameters.size(); i++)
		{
			FormalParameter formalParameter = this.expectedParameters.get(i);
			Object actualParameter = actualParameters.get(formalParameter.name);
			System.err.println("param found: " + formalParameter.name + "= " + actualParameter);
			if (actualParameter == null)
			{
				args[i]  = null;
			} else
			{
				System.err.println("Expected: " + formalParameter.argumentClass.getName() + " found: " + actualParameter.getClass().getName());
				if (formalParameter.argumentClass.isAssignableFrom(actualParameter.getClass()))
				{
					System.err.println("Youpie!");
					args[i] = actualParameter;
				} else // some conversion is needed ...
				{
					if (formalParameter.argumentClass.equals(FileArgument.class))
					{
						if (formalParameter.ioType == Command.ioType.IN &&
								(formalParameter.referenceType == Command.referenceType.NAME ||
								formalParameter.referenceType == Command.referenceType.ID))
						{
							int repoId = findRepositoryID(actualParameter, formalParameter.referenceType);
							if (repoId >= 0)
							{
								File f = File.createTempFile("repo", ".repo");
								args[i] = f.getAbsolutePath();
								InputStream stream = repository.openFile(repoId);
								try 
								{
									FileUtils.copyStream(stream, f);
								} catch (Exception e) 
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else if (formalParameter.ioType == Command.ioType.OUT)
						{
							// dit wordt nazorg om het weer terug te krijgen
							// in de repo. Maar hier aannemen dat het altijd een string is?
							if (String.class.isAssignableFrom(actualParameter.getClass()))
							{
								String s = (String) actualParameter;
								args[i] = s;
							}
						} else if (formalParameter.ioType == Command.ioType.TEMP_DIR)
						{
							Path p = createTempDir();
							args[i]  = p.toString();
							environment.put(formalParameter.name, args[i]);
						} else if (formalParameter.ioType == Command.ioType.OPTIONS)
						{
							
						}
					}
				}
			}
		}
		
		// System.err.println(args[0]);
		
		try 
		{
			Object r = invokeCommand(this.expectedParameters, args);
			System.out.println("Result:" + r);
			// en nu de nazorg: opruimen en resultatem opslaan...
			for (int i=0; i < this.expectedParameters.size(); i++)
			{
				FormalParameter a = this.expectedParameters.get(i);
				Object a1 = actualParameters.get(a.name);
				
				if (a1 != null)
				{
					if (a.argumentClass.equals(FileArgument.class))
					{
						if (a.ioType == Command.ioType.OUT)
						{
							// delete the file named args[i]
							// store output
							System.err.println("Reading output from file:" + args[i]);
							String fName = (String) args[i];
							if (a.referenceType == Command.referenceType.RELATIVE_TO_TEMPDIR)
							{
								for (int j=0; i < this.expectedParameters.size(); j++)
								{
									if (this.expectedParameters.get(i).name.equals(a.baseName))
									{
								
										fName = args[j] + "/"  + fName;
									}
								}
							}
							InputStream str = new FileInputStream((String) args[i]);
							System.err.println(str);
							Properties p = new Properties();
							
							p.put("createdBy", this.commandName);
							
							p.put("createdAt", new Date(System.currentTimeMillis()).toString());
							p.put("createdWithArguments", actualParameters.toString());
							repository.storeFile(str, p);
							str.close();
						} else
						{
							// delete temporary files and stuff
						}
					}
				}
			}
			
		} catch (Exception  e) 
		{
			e.printStackTrace();
		}
	}


	private int findRepositoryID(Object actualParameter, referenceType referenceType) 
	{
		int repoId = -1;
		switch (referenceType)
		{
		case ID:
			if (Integer.class.isAssignableFrom(actualParameter.getClass()))
			{
				repoId = (Integer) actualParameter;
			} else if (String.class.isAssignableFrom(actualParameter.getClass()))
			{
				String s = (String) actualParameter;
				repoId = Integer.parseInt(s);
			}
			break;
		case NAME:
			if (String.class.isAssignableFrom(actualParameter.getClass()))
			{
				String s = (String) actualParameter;
				Set<Integer> V = repository.searchByName(s);
				if (V != null && V.size() > 0)
					repoId = V.iterator().next();
			}
			break;
		}
		return repoId;
	}
	
	public static Path createTempDir() throws IOException
	{
		String property = "java.io.tmpdir";

	    String tempDir = System.getProperty(property);
	    
		Path p = Files.createTempDirectory(Paths.get(tempDir) ,
                "lmserverTemp.");
		
		return p;
	}

	protected static void expandVariables(Properties p)
	{
		expandVariables(p,p);
	}
	
	private static void expandVariables(Properties p, Properties baseProps) 
	{
		for (Object o: p.keySet())
		{
			String n = (String) o;
			String v = p.getProperty(n);
			Matcher m = variablePattern.matcher(v);
			Map<String,String> replacements = new HashMap<String,String>();
			while (m.find())
			{

				String z = m.group();
				System.err.println("Found a variable: " + z);
				String z0 = z.substring(2, z.length()-1);
				if (baseProps.containsKey(z0))
				{
					replacements.put(z, baseProps.getProperty(z0));
				} else
				{
					System.err.println("base props does not contain key " + z0 + " : ");
					baseProps.list(System.err);
				}

				System.err.println("REPLACEMENTS: " + replacements);
				for (String x: replacements.keySet())
				{
					v = v.replace(x, replacements.get(x));
				}
				p.setProperty(n, v);
			}
		}
	}
}