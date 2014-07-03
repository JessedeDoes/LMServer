package eu.transcriptorium.lattice;
import java.io.*;

public class NBestWordInfo
{

	public NBestWordInfo()
	{
	}

	//  NBestWordInfo &operator= (const NBestWordInfo &other);

	void write(File file) 
	{
		// write info to file
	}
	
	boolean parse(String s) { return false; };               // parse info from string
	void invalidate() {};                          // invalidate info
	boolean valid() { return true; };                      // check that info is valid
	void merge(NBestWordInfo other, double otherPosterior) {};
	// combine two pieces of info

	String word;
	//NBestTimestamp start;
	// NBestTimestamp duration;
	double acousticScore;
	double languageScore;
	double start;
	String phones;
	String phoneDurs;
	/*
	 * The following two are used optionally when used as input to
	 * WordMesh::wordAlign() to encode case where the word/transition
	 * posteriors differ from the overall hyp posteriors.
	 */
	double wordPosterior;                         // word posterior probability
	double transPosterior;                        // transition to next word p.p.

	/*
	 * Utility functions
	 */
	// static int  length(const NBestWordInfo *words);
	//static NBestWordInfo *copy(NBestWordInfo *to, const NBestWordInfo *from);
	//static VocabIndex *copy(VocabIndex *to, const NBestWordInfo *from);
}


