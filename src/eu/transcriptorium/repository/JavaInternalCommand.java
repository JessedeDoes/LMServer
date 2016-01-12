package eu.transcriptorium.repository;
import java.util.*;

import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.servlet.LMBuilder;

import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;

public class JavaInternalCommand extends Command
{
	String className;
	String methodName;
	Class c;
	Object object;
	Method method;
	
	boolean isStatic = false;
	
	public JavaInternalCommand()
	{
		
	}
	
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
		try 
		{
			init();
		} catch (ClassNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JavaInternalCommand(Object o, String m, Object[][] args)
	{
		this.className = o.getClass().getName();
		this.methodName = m;
		this.object = o;
		this.commandName = className + "."  + this.methodName;
		
		this.formalParameters = FormalParameter.makeArgumentList(args);
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
				if (this.formalParameters == null) // temporary fix
				{
					Class[] pType = m.getParameterTypes(); // extra check, TODO
					//Parameter[] parameters = m.getReturnType().getP
					this.formalParameters = new ArrayList<FormalParameter>();
					//m.get
					for (int i=0; i < pType.length; i++)
					{
						FormalParameter a = new FormalParameter();
						a.argumentClass = pType[i];
						a.className = a.argumentClass.getName();
						a.ioType = Command.ioType.IN;
						a.name = "parameter" + i;
						formalParameters.add(a);
					}
				}
			}
		}
		System.err.println(formalParameters.get(0));
	}
	
	//@Override
	
		
		// nazorg: stop de output weer in de repository
	

	@Override
	protected Object invokeCommand(List<FormalParameter> formalParameters, Object[] args, Properties config) throws IllegalAccessException, InvocationTargetException {
		return this.method.invoke(object, args);
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
