package eu.transcriptorium.lattice;

public class PathLink
{
	LatticeDecodePath prev;
	double diff;
	PathLink  next;

	public PathLink(LatticeDecodePath  pa, double pd, PathLink pn) 
	{ 
		prev = pa;
		diff = pd; 
		next = pn; 
	}
	
	public PathLink() 
	{ 
		prev = null; 
		diff = 0; 
		next = null; 
	}
}

