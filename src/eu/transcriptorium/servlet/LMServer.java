package eu.transcriptorium.servlet;
import javax.servlet.ServletException;
import javax.servlet.http.*;



import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.Counter;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lm.ScoreWordSubstitutions;
import eu.transcriptorium.suggest.Suggest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//import json.JSONObjects;

/**
 * 
 * @author does
 *
 */

public class LMServer extends  javax.servlet.http.HttpServlet
{

	private static final long serialVersionUID = 1L;
	private String basePath="/datalokaal/Corpus/LM/";

	// ToDo: naar configuratiebestandje

	private String[][] lmLocations = 
		{
			// {"Sonar",  basePath + "Sonar.lm.gz"},
			{"MNL",  basePath + "cdrom_mnl.lm"},
			{"Bentham", basePath + "trigramModel.lm.bin"},
			{"Reichsgericht",  basePath + "ReichsGericht.lm"},
			{"Plantas",  basePath + "PlantasGutenberg.lm"},
		};

	enum Action 
	{
		NONE,
		SUBSTITUTION,
		EVALUATION,
		COMPLETION,
		SUGGESTION
	};

	private Map<String,ScoreWordSubstitutions> ScoreWordSubstitutionsMap = new HashMap<String,ScoreWordSubstitutions>(); 
	private Map<String,Suggest> suggesterMap = new HashMap<String,Suggest>();
	private Map<String,NgramLanguageModel> modelMap = new HashMap<String,NgramLanguageModel>();

	private NgramLanguageModel getModel(String name)
	{
		NgramLanguageModel lm = null;
		if ((lm = modelMap.get(name)) != null)
		{
			return lm;
		}
		String languageModel = null;
		for (int i=0; i < lmLocations.length; i++)
		{
			if (lmLocations[i][0].equalsIgnoreCase(name))
			{
				languageModel = lmLocations[i][1];
			}
		}
		if (languageModel != null)
		{
			if (!languageModel.endsWith(".bin"))
				lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
			else
				lm = LmReaders.readLmBinary(languageModel);
			System.err.println("finished reading LM");
			modelMap.put(name,lm);
		}
		return lm;
	}

	private ScoreWordSubstitutions getScoreWordSubstitutions(String name)
	{
		ScoreWordSubstitutions s;
		if ((s = ScoreWordSubstitutionsMap.get(name)) != null)
		{
			return s;
		}
		for (int i=0; i < lmLocations.length; i++)
		{
			if (lmLocations[i][0].equalsIgnoreCase(name))
			{
				try
				{
					String location = lmLocations[i][1];
					System.err.println("Opening from " + location);
					s = new ScoreWordSubstitutions(lmLocations[i][1]);
					ScoreWordSubstitutionsMap.put(name, s);
					return s;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public Map<String,String> cloneParameterMap(HttpServletRequest request)
	{
		Map<String,String> parameterMap = new HashMap<String,String>();
		for (Object s: request.getParameterMap().keySet())
		{
			System.err.println(s + " --> " + request.getParameter((String) s));
			parameterMap.put((String) s, request.getParameter((String) s)); 
		}
		return parameterMap;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException 
	{

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		Map<String,String> parameterMap = cloneParameterMap(request);


		java.io.PrintWriter out = response.getWriter( );


		Action action = Action.NONE;

		try
		{
			action = Action.valueOf(parameterMap.get("action").toUpperCase());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		switch(action)
		{
		case SUGGESTION: // bijvoorbeeld http://svprre02:8080/LMServer/LMServer?action=suggestion&lm=Bentham&left=sinister
			Suggest s = this.getSuggester(parameterMap.get("lm"));
			String max = parameterMap.get("number");
			int maxSuggestions = 10;
			if (max != null)
			{
				try
				{
					maxSuggestions = Integer.parseInt(max);
				} catch (Exception e) {};
			}
			if (s != null)
			{
				Counter<String> c = s.getDistributionOverContextWords(parameterMap.get("left"), parameterMap.get("right"), parameterMap.get("pattern"));
				out.println(Suggest.counterToJSON(c, maxSuggestions));
			}
			break;
		case SUBSTITUTION:
			try
			{
				ScoreWordSubstitutions sws = this.getScoreWordSubstitutions(parameterMap.get("lm"));
				System.err.println("sws=" + sws);
				String text = parameterMap.get("text");
				String[] lines = text.split("\\s*(\n|<n>)\\s*");
				for (String line: lines)
				{
					String[] parts = line.split("\t|\\s*#\\s*|\\s*\\|\\s*");
					String id = parts[0];
					String sentence = parts[1];
					String[] candidates = parts[2].split("\\s+");
					List<Double> scores = sws.scoreCandidates(sentence, candidates);
					out.print(id + "|");
					for (int i=0; i < candidates.length; i++)
					{
						if (i > 0) out.print(" ");
						out.print(candidates[i] + ":" + scores.get(i));
					}
					out.println();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		default:
			out.println("No valid action specified. Doing nothing!");
		}

	}



	private Suggest getSuggester(String lmName)
	{
		// TODO Auto-generated method stub
		Suggest s = null;
		s = this.suggesterMap.get(lmName);
		if (s != null)
			return s;
		NgramLanguageModel lm = this.getModel(lmName);
		if (lm != null)
		{
			s = new Suggest(lm);
			this.suggesterMap.put(lmName, s);
			return s;
		}
		return null;
	}

	public void init()
	{

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException 
			{
		System.err.println("GET REQUEST:" + request.getQueryString());
		doPost(request,response);
			} 
}
