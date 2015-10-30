package eu.transcriptorium.filestore;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;

public class JavaInternalCommand extends Command
{
	String className;
	String methodName;
	Class c;
	Object object;
	Method method;
	Repository repository;
	boolean isStatic = false;
	
	static class TestObject
	{
		public int dubbel(Integer i)
		{
			return 2 * i;
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
							
							// store output
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
		JavaInternalCommand c = new JavaInternalCommand(new TestObject(), "dubbel");
		
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("parameter0", new Integer(2));
		
		c.invoke(m);
	}
}
