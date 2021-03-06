package eu.transcriptorium.lattice;
import java.util.*;
import java.io.*;
import eu.transcriptorium.util.*;

/**
 * 
 * Simple parser for HTK word graph lattices.
 * <br>Not very generic, specific purpose in mind
 * <ol>
 * <li>Concatenating all lines in a text region
 * <li>Rescoring with more complex models (after concatenation with SRILM, or on the fly)
 * </ol>
 * <p>
 * More info on http://wiki.inl.loc/wiki116/index.php/HTKLattices
 *
 * @author does
 *
 *<p>
 *Header looks like this:
 *<pre>
 *VERSION=1.0
UTTERANCE=./frases/115_070_002_02_18.fea
lmname=bgram.gram
lmscale=15.00  wdpenalty=-26.00
acscale=1.00  
vocab=dic
N=128  L=487  
 *</pre>
 */

public class StandardLatticeFile
{
	static enum State {prologue, nodes, arcs};
	static double htkLogScale =  1/Math.log(10);
	public static void printLattice(PrintStream p, Lattice l)
	{
		Map<String,String> m = l.properties;
		p.printf("VERSION=%s\nUTTERANCE=%s\nlmname=%s\nlmscale=%s wdpenalty=%s\n", 
				m.get("VERSION"), m.get("UTTERANCE"), m.get("lmname"), m.get("lmscale"), m.get("wdpenalty") );
		p.printf("acscale=%s\nvocab=%s\nN=%d L=%d\n", 
				m.get("acscale"), m.get("vocab"), l.N, l.L);

		if (false)
			return;
		List<Integer> li = new ArrayList<Integer>();
		for (Node n: l.getNodes())
			li.add(Integer.parseInt(n.id));

		Collections.sort(li);

		for (Integer i: li)
		{
			Node n = l.getNode(i.toString());
			p.printf("I=%s W=%s v=%d\n", n.id, n.word, n.v);
		}

		for (Arc a: l.arcs) // J=15    S=2    E=9    a=-1130.85  l=-9.330
		{
			p.printf("J=%s S=%s E=%s a=%f l=%f\n", a.id, a.source.id, a.destination.id, a.acoustic, a.language);
		}
	}

	/**
	 * ! SRILM uses the new lm score and wd penalty to compute weights.....
	 * @param fileName
	 * @return
	 */
	public static Lattice readLatticeFromFile(String fileName)
	{
		State scanState = State.prologue;
		Lattice lattice = new Lattice();
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fileName));
			String l;
			int n = 0; // number of nodes seen
			while ((l = b.readLine()) != null)
			{
				Map<String,String> m = Functions.getAttributesFromLine(l);
				if (scanState == State.prologue)
				{
					for (Map.Entry<String,String> e: m.entrySet())
					{
						lattice.properties.put(e.getKey(), e.getValue());
						if (m.get("N") != null)
						{
							lattice.N = Integer.parseInt(m.get("N"));
							scanState = State.nodes;
						}
						if (m.get("L") != null)
						{
							lattice.L = Integer.parseInt(m.get("L"));
						}
						if (m.get("lmscale") != null)
						{
							lattice.lmscale = Double.parseDouble(m.get("lmscale"));
						}
						if (m.get("acscale") != null)
						{
							lattice.acscale = Double.parseDouble(m.get("acscale"));
						}
						if (m.get("wdpenalty") != null)
						{
							lattice.wdpenalty = Double.parseDouble(m.get("wdpenalty"));
						}
					}
				} else if (scanState == State.nodes) // I=111  W=REFEREES            v=1  
				{
					if (n >= lattice.N)
					{
						//System.err.println("switch to arc scanning at " + n + " N = "  + lattice.N);
						scanState = State.arcs;
					}
					else
					{
						Node node = new Node();
						String id = m.get("I");
						//System.err.println("Node with id " + id  + " in line " + l );
						node.id = id;
						node.word = StringUtils.unescapeOctal(m.get( "W"));
						if (node.word == null) //  this is not always correct ... null nodes are possible 
						{
							// System.err.println("No W in line: " + l  + "  file "  + fileName);
							//System.exit(1);
							node.word = Lattice.nullWordSymbol;
						}
						String v = m.get( "v");
						if (v != null)
						{
							node.v = Integer.parseInt(v);
						}
						lattice.nodes.put(id, node);
						n++;
					}
				}  
				if (scanState == State.arcs) // J=15    S=2    E=9    a=-1130.85  l=-9.330
				{
					Arc arc = new Arc();
					arc.id = m.get("J");

					Node startNode = lattice.getNode(m.get("S"));
					Node endNode = lattice.getNode(m.get("E"));

					arc.acoustic = htkLogScale * Double.parseDouble(m.get("a"));
					arc.language = htkLogScale * Double.parseDouble(m.get("l"));

					// ToDo if there is word info, then create extra Node in between??

					String word = StringUtils.unescapeOctal(m.get( "W"));
					
					Node intermediateNode = null;
					
					if (word != null)
					{
						intermediateNode = new Node();
						intermediateNode.id = n++ + "";
						intermediateNode.word = word;
						String v = m.get( "v");
						if (v != null)
						{
							intermediateNode.v = Integer.parseInt(v);
						}
						lattice.nodes.put(intermediateNode.id, intermediateNode);
					}
					// System.err.println("arc from " + startNode + " to  " + endNode);
					// acscale * acoustic + lmscale * language + wdpenalty
					// System.err.println("Endnode: " + endNode);

					if (startNode != null && endNode != null)
					{
						arc.source = startNode;
						if (intermediateNode != null) // untested case ...
						{
							arc.destination = intermediateNode;
							Arc a = new Arc();
							a.source = intermediateNode;
							a.destination = endNode;
							a.weight = 0;
							a.acoustic = a.language = 0;
							a.source.arcs.add(a);
							// TODO give Id to this arc....
						}
						else
							arc.destination = endNode;
						arc.setWeight(lattice.acscale, lattice.lmscale, lattice.wdpenalty);
						arc.source.arcs.add(arc);
						//System.err.println(arc);
					} else
					{
						System.err.println("Arc problem: " + l);
					}
				}
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lattice.rebuildArcList();
		lattice.getStartNode();
		//lattice.renumberNodes();
		//printLattice(System.err,lattice);
		return lattice;
	}

	public static void main(String [] args)
	{
		StandardLatticeFile slf = new StandardLatticeFile();
		Lattice l = slf.readLatticeFromFile("resources/exampleData/115_070_002_02_18.lattice");
		//System.err.println("l:" + l.getSize());
		Lattice ll = LatticeConcatenate.concatenate(l, l);
		Lattice lll = LatticeConcatenate.concatenate(ll, ll);
		Lattice llll = LatticeConcatenate.concatenate(lll, lll);
		Lattice lllll = LatticeConcatenate.concatenate(llll, llll);
		slf.printLattice(System.out,lllll);
		//System.err.println(ll.getSize());
	}
}
