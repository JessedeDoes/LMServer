package eu.transcriptorium.lm;

import java.util.List;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.stats.MakeFrequencyList;
import nl.namescape.stats.WordList;

import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.transcriptorium.lm.VariantLexicon.Variant;
import eu.transcriptorium.util.Options;

public class Corpus2Lexicon implements nl.namescape.filehandling.DoSomethingWithFile, StringNormalizer
{
	WordList tf = new WordList();
	VariantLexicon variantLexicon = new VariantLexicon();
	int nTokens = 0;

	enum Type {word, lemma, lwt};
	Type type = Type.word;

	public StringNormalizer normalizer = this;
	
	public String getNormalizedForm(String lemma, String tag, String wordform)
	{
		tag = tag.replaceAll(",\\s*lexicon:.*", "");
		tag = tag.replaceAll("corpus:", "");
		if (lemma == null || lemma.length() == 0)
			lemma = wordform.toLowerCase();
		return lemma + ":" + tag;
	}

	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(d.getDocumentElement());
			for (Element e: tokens)
			{
				handleToken(e);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private synchronized void handleToken(Element e) 
	{
		nTokens++;

		String lemma = e.getAttribute("lemma");
		String wordform = e.getTextContent();
		String tag = e.getAttribute("type");

		String normalForm = normalizer.getNormalizedForm(lemma,tag,wordform); 
		Variant v = variantLexicon.lookupOrCreateVariant(wordform, normalForm);
		v.absoluteFrequency++; // ugly...
	}

	public void print()
	{
		this.variantLexicon.computeRelativeProbabilities();
		for (String n: variantLexicon.getNormalizedForms())
		{
			for (Variant v: variantLexicon.getVariantsFromNormalForm(n))
			{
				System.out.println(v.toStringUPV());
			}
		}
	}

	public static void main(String[] args)
	{
		Options options = new Options(args)
		{
			protected void setOptions()
			{
				this.addOption("n", "normalizer", true, "Set class for normalizer");
			} 
		};
		args = options.commandLine.getArgs();
		
		Corpus2Lexicon s = new Corpus2Lexicon();
	
		if (options.getOption("normalizer") != null)
		{
			try
			{
				Class c = Class.forName(options.getOption("normalizer"));
				StringNormalizer n = (StringNormalizer) c.newInstance();
				s.normalizer = n;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(s,4);
		s.type = Type.lwt;

		if (args.length > 0)
		{
			for (String d: args)
				DirectoryHandling.traverseDirectory(m,d);
			m.shutdown();
			s.print();
		}
		else
			DirectoryHandling.traverseDirectory(s,"N:/Taalbank/CL-SE-Data/Corpora/GrootModernCorpus/parole-boeken");
	}
}
