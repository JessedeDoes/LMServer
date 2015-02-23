package eu.transcriptorium.lattice;
import java.io.*;
import java.util.Map;

public class LatticeToDot 
{
	public static void latticeToDot(Lattice l, PrintWriter  out)
	{
		//System.err.println(l.getSize());
		out.println("digraph graphname {");
		for (Node n: l.nodes.values())
		{
			out.println("N"+ n.id + " [style=filled fillcolor=pink label=\"" + n.word + "\"];");
		}
		for (Arc a: l.arcs)
		{
			out.println("N" + a.source.id + " -> N" + a.destination.id + ";");
		}
		out.println("}");
		out.flush();
	}

	public static void latticeToDot(Lattice l, String f)
	{
		try {
			PrintWriter x = new PrintWriter(new FileWriter(f));
			latticeToDot(l,x);
			x.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static  String collectOutput(String inputFile) throws IOException
	{
		String programPath = "/usr/bin/dot";
		ProcessBuilder pb  = new ProcessBuilder(programPath, "-T", "svg",inputFile);
		final StringBuffer lines = new StringBuffer();
		//Map<String, String> env = pb.environment();
		//env.put("PATH", programDir + "/");

		//pb.directory(new File(programDir));
		
		pb.redirectErrorStream(true);
		

		
		final Process process = pb.start();

		
		new Thread(new Runnable()
		{
			@Override public void run()
			{
				try
				{
					String line;
					final InputStream stdout = process.getInputStream ();
					BufferedReader brCleanUp = new BufferedReader (new InputStreamReader (stdout));
					while ((line = brCleanUp.readLine ()) != null)
					{
						// System.err.println ("[Stdout] " + line);
						lines.append(line);
					}
					brCleanUp.close();
				} catch (IOException e)
				{
					e.printStackTrace(System.err);
				}
			}
		}).start();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines.toString();
	}
	
	public static String latticeToSVG(Lattice l)
	{
		try 
		{
			File tmp = File.createTempFile("test", "dot");
			latticeToDot(l,tmp.getCanonicalPath());
			
			System.err.println(tmp.getCanonicalPath());
			Process p;
			String command = "dot -Tsvg " + tmp.getCanonicalPath() + " 2>/dev/null ";
			try {
				
				String svg =  collectOutput(tmp.getCanonicalPath());
				svg = svg.replaceAll("^.*?<svg", "<svg");
				return svg;
			} catch (Exception e) {
				e.printStackTrace();
			}

			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		String lFile= args[0];
		Lattice l = StandardLatticeFile.readLatticeFromFile(lFile);
		//latticeToDot(l, new PrintWriter(new OutputStreamWriter(System.out)));
		System.out.println(latticeToSVG(l));
	}
}
