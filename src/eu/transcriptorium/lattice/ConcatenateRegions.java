package eu.transcriptorium.lattice;
import java.io.*;
import java.util.*;


/**
 * Simple utility: concatenate word graphs for all lines belonging to a region.
 * @author does
 *
 */
public class ConcatenateRegions
{
	
	static String exampleDir = 
			"//svprre02/datalokaal/Scratch/HTR/BFBNew/Lattices/";
	public static void concatenateRegions(String dirname)
	{
		
		File d = new File(dirname);
		Map<String, List<String>> regions = new HashMap<String, List<String>> ();
		
		FilenameFilter fi = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".lattice");
			}
		};
		
		if (d.isDirectory())
		{
			File[] entries = d.listFiles(fi);
			Arrays.sort(entries);
			for (File f: entries)
			{
				String key = f.getName();
				key = key.replaceAll("_[0-9]+.lattice", "" );
				List<String> l = regions.get(key);
				
				if (l == null)
				{
					System.err.println(key);
					l = new ArrayList<String>();
					regions.put(key, l);
				}
				try
				{
					l.add(f.getCanonicalPath());
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for (String r: regions.keySet())
		{
	
			List<String> lines = regions.get(r);
			Collections.sort(lines);
			List<Lattice> lattices = new ArrayList<Lattice>();
		
			for (String l: lines)
			{
			   lattices.add(StandardLatticeFile.readLatticeFromFile(l));
			}
			
			Lattice c = LatticeConcatenate.concatenate(lattices);
			LatticeConcatenate.removeLinebreaks(c); // AHEM? why does this work in windows and NOT in linux??????
	
			System.err.println("Region " + r + ", " + lines.size() + " lines " + c.N + "  states");
			
			try
			{
				PrintStream z = new PrintStream(new FileOutputStream(dirname + "/" + r +  ".reglat"));
				StandardLatticeFile.printLattice(z, c);
				z.close();
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length > 0)
			ConcatenateRegions.concatenateRegions(args[0]);
		else
			ConcatenateRegions.concatenateRegions(exampleDir);
	}
}
