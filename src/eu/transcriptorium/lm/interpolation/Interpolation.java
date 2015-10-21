package eu.transcriptorium.lm.interpolation;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Java port of the SRILM compute_best_mix awk script
 * @author jesse
 *
 */
public class Interpolation 
{
	private List<NgramLanguageModel<String>> lms = null;
	private float[] lambdas;
	private float[] priors;
	private float [][] samples;
	int nSamples;
	float[] logpost;
	float  logINF = Float.NEGATIVE_INFINITY;
	int[] counts;
	float[] post_totals;
	int nModels; 
	float precision = (float) 0.001;
	List<String> currentText = null;
	
	public Interpolation(List<NgramLanguageModel<String>> models)
	{
		lms = models;
		nModels = models.size();
		for (int i=0; i < nModels; i++)
			models.get(i).setOovWordLogProb(Float.NEGATIVE_INFINITY);
	}

	public List<Float> computeBestMix(List<String> text)
	{
		evaluateText(text);
		optimize();
		List<Float> r = new ArrayList<Float>();
		for (Float f: priors)
			r.add(f);
		return r;
	}

	private void evaluateText(List<String> text)
	{
		samples = new float[nModels][text.size()];
		
		nSamples = text.size();
		counts = new int[nSamples];
		logpost = new float[nModels];
		priors = new float[nModels];
		System.err.println(text.size());
		for (int j=0; j < nSamples; j++)
		{
			List<String> prefix = text.subList(0, j);
			for (int i=0; i < nModels; i++)
			{
				// TODO: check OOV handling
				samples[i][j] = lms.get(i).getLogProb(prefix);
			}
			counts[j] = 1;
		}
		for (int i=0; i < nModels; i++)
			priors[i] = 1 / (float) nModels;
		currentText = text;
	}

	private void optimize()
	{
		boolean converged = false;
		int iter=0;
		float logsum = 0;

		while (!converged)
		{
			iter++;
			int num_oovs=0, num_words=0;
			float log_like=0;
			post_totals = new float[nModels];

			for (int j=0; j < nSamples; j++)
			{
				boolean all_inf=true;
				for (int i=0; i <lms.size(); i++)
				{
					float sample = samples[i][j];
					logpost[i] = (float) (Math.log10(priors[i]) + sample);
					all_inf &= (sample == logINF);
					logsum = (i==0)? logpost[i]: addlogs(logsum, logpost[i]);
				}
				
				if (all_inf) 
				{
					num_oovs += counts[j];
					// System.err.println("OOV " + currentText.get(j));
					continue;
				}
				
				num_words += counts[j];
				log_like += logsum * counts[j];
				for (int i = 0; i < lms.size(); i ++) 
					post_totals[i] += Math.pow(10, logpost[i] - logsum) * counts[j];
			}

			converged = true;
			float epsilon=0;
			for (int i = 0; i < nModels; i ++) 
			{
				float last_prior = priors[i];
				priors[i] = post_totals[i]/num_words;

				if ((epsilon = Math.abs(last_prior - priors[i])) > precision) 
				{
					converged = false;
				}
			}
			float oov_rate = num_oovs / (float) num_words;
			System.err.printf("iter=%d, epsilon=%f, num_oovs=%d, oov_rate=%f\n", iter, epsilon, num_oovs, oov_rate);
		}
	}

	static NgramLanguageModel readLM(String fileName)
	{
		// languageModel = null;
		if (fileName != null)
		{
			if (!fileName.endsWith(".bin"))
				return  LmReaders.readArrayEncodedLmFromArpa(fileName,false);
			else
				return LmReaders.readLmBinary(fileName);

		} else
			return null;
	}
	
	private static List<String> readText(String fileName) throws IOException
	{
		BufferedReader b = new BufferedReader(new FileReader(fileName));

		String txt ="";
		String l;
		while ((l=b.readLine())!=null)
		{
			txt += " " + l;
		}
		return Arrays.asList(txt.split("\\s+"));
	}

	static float addlogs(float x, float y) 
	{
		if (x<y) 
		{
			float temp = x; x = y; y = temp;
		}
		return (float) (x + Math.log10(1 + Math.pow(10, y - x)));
	}

	public static void main(String[] args) throws IOException
	{
		List<NgramLanguageModel<String>> lms = new ArrayList<NgramLanguageModel<String>>();
		for (int i=0; i < args.length-1; i++)
			lms.add(readLM(args[i]));

		Interpolation p = new Interpolation(lms);
		System.out.println(p.computeBestMix(readText(args[args.length-1])));
	}
}
