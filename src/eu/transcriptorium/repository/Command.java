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
 *We (unfortunately) need something like a "general" mechanism to be able to execute both command line applications and java methods on files in such a way that
 *
 *<ul>
 *<li>
 * we can run commands on files in the repository and store results in the repository.
 *<li> command are easily invoked by means of the web service
 *</ul>
 *
 *<p>
 *The messy bit is passing parameters and translating paths on the server to repository ids etc.
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
	Repository repository = null; // new PostgresRepository(PostgresRepository.getDefaultProperties()); // do this differently...
	static private Pattern variablePattern = Pattern.compile("\\$\\{[^{}]*\\}");
	
	public String commandName;
	List<FormalParameter> formalParameters = null;
	Properties configuration = null;

	Set<String> tempFileSet  = new HashSet<String>();
	Set<String> tempDirSet  = new HashSet<String>();

	private static boolean doNotCleanup = false;
	public enum type
	{
		SHELL,
		JAVA
	} ;

	static public enum ioType
	{
		IN,
		INPUT_COLLECTION,
		OUT,
		CONFIG,
		OUTPUT_DIRECTORY // directory arguments are always temporary (?)
	};

	static public enum referenceType
	{
		NAME,
		ID,
		COLLECTION,
		RELATIVE_TO_OUTPUT_DIRECTORY,
		PICKUP_FROM_CONFIG, 
		INSERT_INTO_CONFIG
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

			if (this.referenceType == referenceType.PICKUP_FROM_CONFIG || 
					this.referenceType == referenceType.INSERT_INTO_CONFIG ||
					this.referenceType == referenceType.RELATIVE_TO_OUTPUT_DIRECTORY) // (?)

				this.passToCommand = false;
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
		this.formalParameters.add(new FormalParameter(name, className));
	}

	public static class FileArgument
	{
		String pathName;
		int repositoryId;
		String basePathName;
	}


	protected Object invokeCommand(List<FormalParameter> formalParameters, Object[] args, Properties configuration) throws IllegalAccessException, 
	InvocationTargetException 
	{
		return null;
	}


	public void invoke(Map<String, Object> actualParameters) throws IOException
	{
		Object[] args = new Object[this.formalParameters.size()];
		configuration = new Properties();
		File saveConfigTo = null;
		Properties configToSave = null;
		Properties originalConfig = null;
		for (int i=0; i < this.formalParameters.size(); i++)
		{
			FormalParameter formalParameter = this.formalParameters.get(i);
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
								File f = saveToTempFile(repoId);
								System.err.println("saved to temp file:" + f.getAbsolutePath());
								args[i] = f.getAbsolutePath();
							}
						} else if (formalParameter.ioType == Command.ioType.IN && 
								formalParameter.referenceType == Command.referenceType.INSERT_INTO_CONFIG)
						{
							//File f = saveToTempFile(repoId); // why not???
							int repoId = findRepositoryID(actualParameter, formalParameter.referenceType);
							File f = saveToTempFile(repoId);
							System.err.println("saved to temp file:" + f.getAbsolutePath());
							args[i] = f.getAbsolutePath();
							configuration.put(formalParameter.name, f.toString());

							// nee dit is onzin: je moet ook opslaan uit de repository
						} else if (formalParameter.ioType == Command.ioType.INPUT_COLLECTION && 
								formalParameter.referenceType == Command.referenceType.INSERT_INTO_CONFIG)
						{
							int collection_id = findRepositoryID(actualParameter, formalParameter.referenceType);
							Set<Integer> V = repository.getCollectionItems(collection_id);
							Path p = createTempDir();
							// this is a bit silly, but...
							// we need to save to the temp dir, stripping the collection name from the filenames...
							String collectionName = repository.getName(collection_id);
							for (int id: V)
							{
								String name = repository.getName(id);
								name = name.replace(collectionName, "");  // shaky
								String p1 = p.toString() + "/" + name;
								System.err.println( "saving file:"  + p1 + " id= " + id);
								saveToFile(p1,id);
							}
							configuration.put(formalParameter.name, p.toString());
						}
						else if (formalParameter.ioType == Command.ioType.OUT)
						{
							// dit wordt nazorg om het weer terug te krijgen
							// in de repo. Maar hier aannemen dat het altijd een string is?
							if (formalParameter.referenceType == Command.referenceType.PICKUP_FROM_CONFIG)
							{
								File f = createTempFile();
								args[i] = f.toString();
								configuration.put(formalParameter.name, f.toString());
							} else if (String.class.isAssignableFrom(actualParameter.getClass()))
							{
								if (formalParameter.referenceType == Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY)
								{
									String s = (String) actualParameter;
									args[i] = s;
								} else
								{
									// dit is niet goed: je moet opslaan in een temp file
									// maar wel de in de parameters gegeven naam in de repository zetten
									
									File f = createTempFile();
									args[i] = f.toString();
								}
								// args[i] = s;
							}
						} else if (formalParameter.ioType == Command.ioType.OUTPUT_DIRECTORY)
						{
							Path p = createTempDir();
							args[i]  = p.toString();
							configuration.put(formalParameter.name, p.toString());
						} else if (formalParameter.ioType == Command.ioType.CONFIG)
						{
							int repoId = findRepositoryID(actualParameter, formalParameter.referenceType);
							if (repoId >=0)
							{
								// Neen: dit moet je eerst aanpassen, om de 
								// temp directory opties uit te schrijven
								File f = saveToTempFile(repoId);
								Properties p = new Properties();
								// and put this in the environment???
								p.load(new FileInputStream(f));
								originalConfig = (Properties) p.clone();

								for (Object x: configuration.keySet())
								{
									p.put(x,configuration.get(x));
								} // maar omgekeerd niets... Waarom niet?

								expandVariables(p);
								saveConfigTo = f;
								configToSave = p;

								// p.store(new FileOutputStream(f), "");

								args[i] = f.getAbsolutePath();
								p.store(new OutputStreamWriter(System.out), "listing properties");
							}
						} 
					}
				}
			}
		}

		// System.err.println(args[0]);
		if (saveConfigTo != null && configToSave != null)
		{
			for (Object x: configuration.keySet())
			{
				configToSave.put(x,configuration.get(x));
			}
			configToSave.store(new FileOutputStream(saveConfigTo), "");
		}
		expandVariables(configuration);

		//configuration.store(System.out, "hallo!");

		try 
		{
			Object r = invokeCommand(this.formalParameters, args, configToSave);
			System.out.println("Result:" + r);

			// en nu de nazorg: opruimen en resultatem opslaan...

			for (int i=0; i < this.formalParameters.size(); i++)
			{
				FormalParameter formalParameter = this.formalParameters.get(i);
				Object actualParameter = actualParameters.get(formalParameter.name);

				if (actualParameter != null)
				{
					if (formalParameter.argumentClass.equals(FileArgument.class))
					{
						if (formalParameter.ioType == Command.ioType.OUT)
						{
							// delete the file named args[i]
							// store output
							System.err.println("Reading output for " +  formalParameter.name +  " from file:" + args[i]);
							String fName = (String) args[i];
							Properties p = new Properties();
							if (formalParameter.referenceType == Command.referenceType.RELATIVE_TO_OUTPUT_DIRECTORY)
							{
								System.err.println("Looking for base: "  + formalParameter.baseName);
								Object base = actualParameters.get(formalParameter.baseName);
								p.put("filename",  base.toString() + "/" + fName);

								for (int j=0; j < this.formalParameters.size(); j++)
								{
									if (this.formalParameters.get(j).name.equals(formalParameter.baseName))
									{
										fName = args[j] + "/"  + fName;
									}
								}
								System.err.println("relative path expanded to:"  + fName);
							} else if (formalParameter.referenceType == Command.referenceType.PICKUP_FROM_CONFIG)
							{
								System.err.println("Pickup fName: " + fName);
								fName = configuration.getProperty(formalParameter.name);
								p.put("filename", originalConfig.getProperty(formalParameter.name));
							}

							if (p.get("filename") == null)
							{
								p.put("filename", actualParameter.toString());
							}

							InputStream str = new FileInputStream(fName);

							// System.err.println(str);				

							p.put("createdBy", this.commandName);
							p.put("createdAt", new Date(System.currentTimeMillis()).toString());
							p.put("createdWithArguments", actualParameters.toString());

							System.err.println("Storing new file with properties:"  + p);

							repository.storeFile(str, p.getProperty("filename"), p);

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
		this.cleanup();
	}


	private File saveToFile(String p1, int id) 
	{
		// TODO Auto-generated method stub
		File f = new File(p1);
		if (!doNotCleanup) f.deleteOnExit();
		tempFileSet.add(p1);
		InputStream stream = repository.openFile(id);
		try 
		{
			FileUtils.copyStream(stream, f);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return f;
	}


	protected File createTempFile() throws IOException 
	{
		File f = File.createTempFile("repo", ".repo");
		if (!doNotCleanup) f.deleteOnExit();
		tempFileSet.add(f.getCanonicalPath());
		return f;
	}


	protected File createTempFile(File dir) throws IOException 
	{
		File f = File.createTempFile("repo", ".repo", dir);
		if (!doNotCleanup) f.deleteOnExit();
		tempFileSet.add(f.getCanonicalPath());
		return f;
	}

	protected File saveToTempFile(int repoId) throws IOException 
	{
		File f = createTempFile();

		
		try 
		{
			InputStream stream = repository.openFile(repoId);
			FileUtils.copyStream(stream, f);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return f;
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
				repoId = repository.search(s);
			}
			break;
		default: // ugly....
			if (String.class.isAssignableFrom(actualParameter.getClass()))
			{
				String s = (String) actualParameter;
				repoId = repository.search(s);
			}
		}

		return repoId;
	}

	protected  Path createTempDir() throws IOException
	{
		System.err.println("Creating temp dir....");
		String property = "java.io.tmpdir";

		String tempDir = System.getProperty(property);

		System.err.println("System temp dir: " + tempDir);
		Path p = Files.createTempDirectory(Paths.get(tempDir) ,
				"lmserverTemp.");

		this.tempDirSet.add(p.toString());

		return p;
	}

	protected static void expandVariables(Properties p)
	{
		expandVariables(p,p);
	}

	protected void cleanup()
	{
		if (doNotCleanup)
			return;
		for (String d: this.tempDirSet)
		{
			System.err.println("cleanup dir: " + d);
			File dir = new File(d);
			if (dir.isDirectory())
				FileUtils.deleteRecursively(dir);
			else if (dir.exists())
				dir.delete();
		}
		for (String s: this.tempFileSet)
		{
			System.err.println("cleanup  file: " + s);
			File f = new File(s);
			if (f.exists())
			{
				f.delete();
			}
		}
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
	
	public void setRepository(Repository r)
	{
		this.repository = r;
	}
}
