package eu.transcriptorium.lattice;

import java.io.Serializable;

public class Arc  implements Serializable
{
	transient Node destination = null;
	transient Node source = null;
	
	String id;
	double acoustic; // acoustice prob
	double language; // lm prob
	double weight; // weight score as computed by SRI from lattice
	
	public String toString()
	{
		return destination.id + ":"  + acoustic;
	}
	
	public void setWeight(double acscale, double lmscale, double wdpenalty)
	{
		if (this.source.word.equals(Lattice.nullWordSymbol) && this.destination.word.equals(Lattice.sentenceStartSymbol)) // HACK! better remove silly nodes
		{
			//System.err.println("Silly arc from  " +  this.source + "  to " + this.destination);
			this.weight = 0.0;
		}
		else
			this.weight = acscale*this.acoustic + lmscale*this.language + wdpenalty;
		// System.err.printf("Set weight: weight=%f, acscale=%f lmscale=%f wdpenalty=%f acoustic=%f language=%f\n" , weight, acscale, lmscale, wdpenalty, acoustic, language);
		// what about pron?
	}
	
	public Arc clone() // does NOT clone the node references in the arcs
	{
		Arc a =new Arc();
		a.id = this.id;
		a.language = this.language;
		a.acoustic = this.acoustic;
		a.destination = this.destination;
		a.source = this.source;
		a.weight = this.weight;
		return a;
	}
}
