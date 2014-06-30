package eu.transcriptorium.lattice;

import java.io.Serializable;

public class Arc  implements Serializable
{
	transient Node destination = null;
	transient Node source = null;
	
	String id;
	double a; // acoustice prob
	double l; // lm prob
	
	public String toString()
	{
		return destination.id + ":"  + a;
	}
	
	public Arc clone() // does NOT clone the node references in the arcs
	{
		Arc a =new Arc();
		a.id = this.id;
		a.l = this.l;
		a.a = this.a;
		a.destination = this.destination;
		a.source = this.source;
		return a;
	}
}
