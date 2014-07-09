package eu.transcriptorium.lattice;

import java.io.Serializable;

public class Arc  implements Serializable
{
	transient Node destination = null;
	transient Node source = null;
	
	String id;
	double acoustic; // acoustice prob
	double language; // lm prob
	
	public String toString()
	{
		return destination.id + ":"  + acoustic;
	}
	
	public Arc clone() // does NOT clone the node references in the arcs
	{
		Arc a =new Arc();
		a.id = this.id;
		a.language = this.language;
		a.acoustic = this.acoustic;
		a.destination = this.destination;
		a.source = this.source;
		return a;
	}
}
