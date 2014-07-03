package eu.transcriptorium.lattice;

import java.util.*;

public class LatticeDecodePath
{
	Node node;
	double  m_Prob, m_GProb;
	int m_NumPreds;
	PathLink m_Preds;
	Map<String, Double> seen;
	LatticeDecodePath m_Prev;

	List<String> m_Context = new ArrayList<String>();

	public void addLink(LatticeDecodePath path, double diff)
	{
		PathLink l = new PathLink(path, diff, m_Preds);
		m_Preds = l;
		m_NumPreds ++;
	}

	void merge(LatticeDecodePath  p, int nbest) 
	{
		if (m_Prob < p.m_Prob) 
		{
			m_Prob = p.m_Prob;
			m_GProb = p.m_GProb;
			m_Prev = p.m_Prev;
		}

		if (nbest != 0) 
		{	
			double diff = p.m_Prob;
			if (p.m_Prev != null)
				diff -= p.m_Prev.m_Prob;

			addLink(p.m_Prev, diff);
		}
	}

	// sort in descending order
	public static class PathComparator implements Comparator<LatticeDecodePath>
	{
		public int  compare(LatticeDecodePath p1, LatticeDecodePath  p2)
		{
			double pr1 = p1.m_Prob;
			double pr2 = p2.m_Prob;

			if (pr1 > pr2)
				return -1;
			else if (pr1 < pr2)
				return 1;
			else 
				return 0;
		}
	}

	public static class LinkComparator implements Comparator<PathLink>
	{
		public  int compare (PathLink  l1,  PathLink  l2) 
		{
			double p1 = l1.prev.m_Prob + l1.diff;
			double p2 = l2.prev.m_Prob + l2.diff;

			if (p1 < p2) 
				return 1;
			else if (p1 > p2)
				return -1;
			else
				return 0;
		}
	}

	int truncLinks(int maxDegree) 
	{
		if (maxDegree == 0 || m_NumPreds <= maxDegree) 
			return m_NumPreds;

		int i = 0;
		PathLink  p;
		ArrayList<PathLink > array = new   ArrayList<PathLink>() ;
		p = m_Preds;

		for (i = 0; i < m_NumPreds; i++, p = p.next) 
		{
			array.add(p);
		}

		Collections.sort(array, new LinkComparator());

		p = m_Preds = array.get(0);    
		for (i = 1; i < maxDegree ; i++) 
		{
			p.next = array.get(i);
			p = array.get(i);
		}
		p.next = null;
		return (m_NumPreds = maxDegree);
	}
}
