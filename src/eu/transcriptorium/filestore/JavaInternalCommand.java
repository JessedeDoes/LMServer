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
	
	public void init() throws ClassNotFoundException
	{
		c = Class.forName(className);
		for (Method m: c.getDeclaredMethods())
		{
			if (m.getName().equals(methodName))
			{
				method = m;
				Type[] pType = m.getGenericParameterTypes();
			}
		}
	}
	
	@Override
	public void invoke(Map<String, Object> arguments) throws IOException
	{
		Object[] args = new Object[this.arguments.size()];
		for (int i=0; i < this.arguments.size(); i++)
		{
			Argument a = this.arguments.get(i);
			Object a1 = arguments.get(a.name);
			if (a1 == null)
			{
				args[i]  = null;
			} else
			{
				if (a.argumentClass.isAssignableFrom(a1.getClass()))
				{
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
		try 
		{
			this.method.invoke(object, args);
			
			// en nu de nazorg: opruimen en resultatem opslaan...
			
		} catch (Exception  e) 
		{
			e.printStackTrace();
		}
		
		// nazorg: stop de output weer in de repository
	}
}
