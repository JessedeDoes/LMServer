package eu.transcriptorium.lm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.*;

import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.collections.Iterators;
import edu.berkeley.nlp.lm.io.IOUtils;
import edu.berkeley.nlp.lm.io.KneserNeyFileWritingLmReaderCallback;
import edu.berkeley.nlp.lm.io.KneserNeyLmReaderCallback;
import edu.berkeley.nlp.lm.io.LmReader;
import edu.berkeley.nlp.lm.io.LmReaderCallback;
import edu.berkeley.nlp.lm.util.Logger;
import edu.berkeley.nlp.lm.util.LongRef;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.stats.MakeFrequencyList;
import nl.namescape.stats.WordList;
import nl.namescape.tei.TEITagClasses;

import nl.namescape.util.XML;
import eu.transcriptorium.util.*;

/**
 * To be worked out: model from TEI (do we really need this?)
 * @author does
 *
 * @param <W>
 */

public class TEIReader<W>  implements LmReader<LongRef, LmReaderCallback<LongRef>>, DoSomethingWithFile
{

	private final WordIndexer<W> wordIndexer;

	//private final Iterable<String> lineIterator = null;
	private List<String> inputFiles;
	LmReaderCallback<LongRef> callback = null;
	
	public boolean tokenIsOK(String s)
	{
		if (s.length() > 30) return false;
		if (s.matches("^[A-Za-z0-9]+"))
		{
			return true;
		}
		return false;
	}
	
	public TEIReader(final List<String> inputFiles, final WordIndexer<W> wordIndexer) 
	{
		//this(getLineIterator(inputFiles), wordIndexer);
		this.wordIndexer = wordIndexer;
		this.inputFiles = inputFiles;
	}

	/*
	public TEIReader(Iterable<String> lineIterator, final WordIndexer<W> wordIndexer) 
	{
		this.lineIterator = lineIterator;
		this.wordIndexer = wordIndexer;
	}
    */
	
	/**
	 * Reads newline-separated plain text from inputFiles, and writes an ARPA lm
	 * file to outputFile. If files have a .gz suffix, then they will be
	 * (un)zipped as necessary.
	 * 
	 * @param inputFiles
	 * @param outputFile
	 */
	@Override
	public void parse(final LmReaderCallback< LongRef> callback) 
	{
		this.callback = callback;
		
		for (String fName: inputFiles)
		{
			DirectoryHandling.traverseDirectory(this, fName);
			//handleFile(fName);
		}
		callback.cleanup();
		//readFromFiles(callback);
	}

	/*
	private void readFromFiles(final LmReaderCallback<LongRef> callback) 
	{
		Logger.startTrack("Reading in ngrams from TEI text");

		countNgrams(lineIterator, callback);
		Logger.endTrack();

	}
    */
	
	/**
	 * @param <W>
	 * @param wordIndexer
	 * @param maxOrder
	 * @param allLinesIterator
	 * @param callback
	 * @param ngrams
	 * @return
	 */
	/*
	private void countNgrams(final Iterable<String> allLinesIterator, 
			final LmReaderCallback<LongRef> callback) 
	{
		long numLines = 0;

		for (final String line : allLinesIterator) 
		{
			if (numLines % 10000 == 0) Logger.logs("On line " + numLines);
			numLines++;
			final String[] words = line.split("\\s+");
			final int[] sent = new int[words.length + 2];
			sent[0] = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
			sent[sent.length - 1] = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
			for (int i = 0; i < words.length; ++i) {
				sent[i + 1] = wordIndexer.getOrAddIndexFromString(words[i]);
			}
			callback.call(sent, 0, sent.length, new LongRef(1L), line);

			//			for (int ngramOrder = 0; ngramOrder < lmOrder; ++ngramOrder) {
			//				for (int i = 0; i < sent.length; ++i) {
			//					if (i - ngramOrder < 0) continue;
			//					callback.call(sent, i - ngramOrder, i + 1, null, line);
			//				}
			//			}
		}
		callback.cleanup();
	}
    */
	/**
	 * @param files
	 * @return
	 */
	/*
	private static Iterable<String> getLineIterator(final Iterable<String> files) {
		final Iterable<String> allLinesIterator = Iterators.flatten(new Iterators.Transform<String, Iterator<String>>(files.iterator())
		{

			@Override
			protected Iterator<String> transform(final String file) {
				try {
					if (file.equals("-")) {
						return IOUtils.lineIterator(IOUtils.getReader(System.in));
					} else
						return IOUtils.lineIterator(file);
				} catch (final IOException e) {
					throw new RuntimeException(e);

				}
			}
		});
		return allLinesIterator;
	}
	*/

	@Override
	public void handleFile(String arg0) 
	{
		// TODO Auto-generated method stub
		try
		{
			Document d = XML.parse(arg0);
			for (Element s: TEITagClasses.getSentenceElements(d))
			{
				List<Element> tokens = TEITagClasses.getTokenElements(s);
				final int[] sent = new int[tokens.size() + 2];
				final List<String> words = new ArrayList<String>();
				sent[0] = wordIndexer.getOrAddIndex(wordIndexer.getStartSymbol());
				sent[sent.length - 1] = wordIndexer.getOrAddIndex(wordIndexer.getEndSymbol());
				for (int i=0; i < tokens.size(); i++)
				{
					String w = tokens.get(i).getTextContent();
					if (tokenIsOK(w))
					{
						sent[i + 1] = wordIndexer.getOrAddIndexFromString(w);
						words.add(w);
					}
				}
				String sentence = Functions.join(words, " ");
				callback.call(sent, 0, sent.length, new LongRef(1L), sentence);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Estimates a Kneser-Ney language model from TEI input, and writes a file
	 * (in ARPA format)
	 * (not really necessary (we can extract the text and call the plain text methods))
	 * @param <W>
	 * @param files
	 *            Files of raw text (new-line separated).
	 * @param wordIndexer
	 * @param lmOrder
	 * @param arpaOutputFile
	 */
	public static <W> void createKneserNeyLmFromTEIFiles(final List<String> files, final WordIndexer<W> wordIndexer, final int lmOrder,
			final File arpaOutputFile, final ConfigOptions opts) 
	{
		final TEIReader<W> reader = new TEIReader<W>(files, wordIndexer);
		KneserNeyLmReaderCallback<W> kneserNeyReader = new KneserNeyLmReaderCallback<W>(wordIndexer, lmOrder, opts);
		reader.parse(kneserNeyReader);
		kneserNeyReader.parse(new KneserNeyFileWritingLmReaderCallback<W>(arpaOutputFile, wordIndexer));
	}
}
