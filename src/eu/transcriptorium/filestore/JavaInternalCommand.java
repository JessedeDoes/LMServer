package eu.transcriptorium.filestore;
import java.util.*;

import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.servlet.LMBuilder;

import java.lang.reflect.*;
import java.io.*;

public class JavaInternalCommand extends Command
{
	String className;
	String methodName;
	Class c;
	Object object;
	Method method;
	Repository repository = new PostgresRepository(PostgresRepository.getDefaultProperties());
	boolean isStatic = false;
	
	static class TestObject
	{
		public int dubbel(Integer i)
		{
			return 2 * i;
		}
		
		public String doFile(String name)
		{
			String c = "";
			try {
				BufferedReader b = new BufferedReader(new FileReader(name));
				String l;
				while ((l = b.readLine()) != null)
				{
					c += l  + "\n";
				}
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return c;
		}
		
		public static void copyFile(String in, String out) throws IOException
		{
			File fOut = new File(out);
			FileUtils.copyFile(new File(in), fOut);
		}
	}
	
	public JavaInternalCommand(Object o, String m)
	{
		this.className = o.getClass().getName();
		this.methodName = m;
		this.object = o;
		try {
			init();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JavaInternalCommand(Object o, String m, Object[][] args)
	{
		this.className = o.getClass().getName();
		this.methodName = m;
		this.object = o;
		this.arguments = Argument.makeArgumentList(args);
		try {
			init();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init() throws ClassNotFoundException
	{
		c = Class.forName(className);
		for (Method m: c.getDeclaredMethods())
		{
			if (m.getName().equals(methodName))
			{
				method = m;
				//
				if (this.arguments == null) // temporary fix
				{
					Class[] pType = m.getParameterTypes(); // extra check, TODO
					//Parameter[] parameters = m.getReturnType().getP
					this.arguments = new ArrayList<Argument>();
					//m.get
					for (int i=0; i < pType.length; i++)
					{
						Argument a = new Argument();
						a.argumentClass = pType[i];
						a.className = a.argumentClass.getName();
						a.ioType = Command.ioType.IN;
						a.name = "parameter" + i;
						arguments.add(a);
					}
				}
			}
		}
		System.err.println(arguments.get(0));
	}
	
	@Override
	public void invoke(Map<String, Object> arguments) throws IOException
	{
		Object[] args = new Object[this.arguments.size()];
		for (int i=0; i < this.arguments.size(); i++)
		{
			Argument a = this.arguments.get(i);
			Object a1 = arguments.get(a.name);
			System.err.println("param found: " + a.name + "= " + a1);
			if (a1 == null)
			{
				args[i]  = null;
			} else
			{
				System.err.println("expected: " + a.argumentClass.getName() + " found: " + a1.getClass().getName());
				if (a.argumentClass.isAssignableFrom(a1.getClass()))
				{
					System.err.println("Youpie!");
					args[i] = a1;
				} else // some conversion is needed ...
				{
					if (a.argumentClass.equals(FileArgument.class))
					{
						if (a.referenceType == Command.referenceType.ID && a.ioType == Command.ioType.IN)
						{
							int repoId = -1;
							if (Integer.class.isAssignableFrom(a1.getClass()))
							{
								repoId = (Integer) a1;
							} else if (String.class.isAssignableFrom(a1.getClass()))
							{
								String s = (String) a1;
								repoId = Integer.parseInt(s);
								
							}
							
							if (repoId >= 0)
							{
								File f = File.createTempFile("repo", ".repo");
								args[i] = f.getAbsolutePath();
								InputStream stream = repository.openFile(repoId);
								try {
									FileUtils.copyStream(stream, f);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else if (a.ioType == Command.ioType.OUT)
						{
							// dit wordt nazorg om het weer terug te krijgen
							// in de repo. Maar hie aannemen dat het altijd een string is?
							if (String.class.isAssignableFrom(a1.getClass()))
							{
								String s = (String) a1;
								args[i] = s;
							}
						}
					}
				}
			}
		}
		
		System.err.println(args[0]);
		try 
		{
			Object r = this.method.invoke(object, args);
			System.out.println("Result:" + r);
			// en nu de nazorg: opruimen en resultatem opslaan...
			for (int i=0; i < this.arguments.size(); i++)
			{
				Argument a = this.arguments.get(i);
				Object a1 = arguments.get(a.name);
				
				if (a1 != null)
				{
					if (a.argumentClass.equals(FileArgument.class))
					{
						if (a.ioType == Command.ioType.OUT)
						{
							// delete the file named args[i]
							// store output
							System.err.println("Reading output from file:" + args[i]);
							InputStream str = new FileInputStream((String) args[i]);
							System.err.println(str);
							Properties p = new Properties();
							p.put("createdBy", this.className + "."  + this.methodName);
							p.put("createdAt", new Date(System.currentTimeMillis()).toString());
							p.put("createdWithArguments", arguments.toString());
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
		
		// nazorg: stop de output weer in de repository
	}
	
	public static void main(String[] args) throws IOException
	{
	    String FA = Command.FileArgument.class.getName();
		Object[][] params = { { "i", "java.lang.Integer"}};
		
		//JavaInternalCommand c = new JavaInternalCommand(new TestObject(), "dubbel", params);
		
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("i", new Integer(2));
		
		//c.invoke(m);
		
		
		//////
		
		Object[][] paramsWithFile = 
		{ 
				{ "f", Command.FileArgument.class.getName(), Command.ioType.IN, Command.referenceType.ID} 		
		};
		JavaInternalCommand c1 = new JavaInternalCommand(new TestObject(), "doFile", paramsWithFile);
		m.clear();
		m.put("f", new Integer(1));
		c1.invoke(m);
		
		////// 
		
		Object[][] paramsWithOutFile = 
		{ 
				{ "in", FA, Command.ioType.IN, Command.referenceType.ID}, 
				{ "out", FA, Command.ioType.OUT, Command.referenceType.ID}
		};
		
		JavaInternalCommand c2 = new JavaInternalCommand(new TestObject(), "copyFile", paramsWithOutFile);
		m.clear();
		m.put("in", 1);
		m.put("out", "/tmp/hola");
		c2.invoke(m);
		
		
		
		/////
		
		Object[][] somethingMoreReal  = 
			{
					{"lmOrder", "java.lang.Integer"},
					{"inputText", FA, Command.ioType.IN, Command.referenceType.ID},
					{"outputLM", FA, Command.ioType.OUT,  Command.referenceType.ID}
			};
		
		m.clear();
		m.put("lmOrder", 8);
		m.put("inputText", 1);
		m.put("outputLM", "/tmp/lm.out");
		
		JavaInternalCommand c3 = new JavaInternalCommand(new LMBuilder(), "buildLMFromOneText", somethingMoreReal);
		
		c3.invoke(m);
		// LmReaders.createKneserNeyLmFromTextFiles(files, wordIndexer, lmOrder, arpaOutputFile, opts);
	}
}
