package eu.transcriptorium.lattice;

import java.util.*;
/* This links paths and nodes */

public class NodePathInfo
{
	  double m_OldLMScore=0; // m_OldLMScore
	  int m_NumPaths=0; // number of paths
	  double  m_ProbBwd=0; // backward prob
	 // PathHash   m_PHash;
	  // hash key and equality for  LatticeDecodePath should depend on LM context and node (?)
	  Map<LatticeDecodePath, LatticeDecodePath> m_PHash =
			  new HashMap<LatticeDecodePath, LatticeDecodePath>();
	  LatticeDecodePath[] m_PList = null;
}
