package eu.transcriptorium.servlet;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.Counter;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lm.ScoreWordSubstitutions;
import eu.transcriptorium.suggest.Suggest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
			{"MNL",  basePath + "cdrom_mnl.lm", "Middelnederlands"},
			{"Bentham", basePath + "trigramModel.lm.bin", "Bentham with ECCO"},
			{"Reichsgericht",  basePath + "ReichsGericht.lm", "Reichsgericht"},
			{"Plantas",  basePath + "PlantasGutenberg.lm", "Plantas with Gutenberg Spanish"},
            {"SpanishGutenberg", basePath + "SpanishGutenberg.lm.gz", "Spanish Gutenberg"}
		};

	enum Action 
	{
		NONE,
		LIST_LMS,
		SUBSTITUTION,
		EVALUATION,
		COMPLETION,
		SUGGESTION,
		BUILD_LM
	};

	private Map<String,ScoreWordSubstitutions> ScoreWordSubstitutionsMap = new HashMap<String,ScoreWordSubstitutions>(); 
	private Map<String,Suggest> suggesterMap = new HashMap<String,Suggest>();
	private Map<String,NgramLanguageModel> modelMap = new HashMap<String,NgramLanguageModel>();
	private Map<String, String> modelDescriptionMap = new HashMap<String,String>();

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
					System.err.println("Opening  LM from " + location);
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
		//FileUploadBase.
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		Map<String,String> parameterMap = cloneParameterMap(request);
		MultipartFormData mpfd = null;
		if (ServletFileUpload.isMultipartContent(request))
		{
			mpfd = new MultipartFormData(request); 
			parameterMap = mpfd.getFields();
		}
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
		case LIST_LMS:
			out.println(mapToJSON(this.modelDescriptionMap));
			break;
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
		case BUILD_LM:
			
			String name = mpfd.getFields().get("name");
			System.err.println("Building model named "+ name);
			String description = mpfd.getFields().get("description");
			if (description == null)
			{
				description = " undocumented ";
			}
			String newLMFileName = new LMBuilder().buildLM(3, mpfd.getNamesOfUploadedfiles());
			if (newLMFileName != null)
			{
				NgramLanguageModel lm = null;
				if (!newLMFileName.endsWith(".bin"))
					lm = LmReaders.readArrayEncodedLmFromArpa(newLMFileName,false);
				else
					lm = LmReaders.readLmBinary(newLMFileName);
				System.err.println("finished reading LM");
				modelMap.put(name,lm);
				this.modelDescriptionMap.put(name, description);
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
		for (String [] x: this.lmLocations)
		{
			this.modelDescriptionMap.put(x[0], x[2]);
		}
	}

	public static String mapToJSON(Map<String,String> map)
	{
		StringWriter  strw = new StringWriter();
		BufferedWriter sw = new BufferedWriter(strw);
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, false); // dit heeft dus geen invloed ...

		JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
		JsonGenerator jg = jgf.createGenerator(sw);

		jg = jg.writeStartObject();
		int i=0;

		for (Map.Entry<String, String> e: 	map.entrySet())
		{
			//System.err.println(sw.toString());
			System.err.println(e);
			jg = jg.write(e.getKey(), e.getValue());

			
		}

		jg = jg.writeEnd();
	

		jg.close();

		return strw.getBuffer().toString();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException 
			{
		System.err.println("GET REQUEST:" + request.getQueryString());
		doPost(request,response);
			} 
}
