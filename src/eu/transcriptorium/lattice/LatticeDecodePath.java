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
}
