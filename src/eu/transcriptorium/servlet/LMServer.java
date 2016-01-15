package eu.transcriptorium.servlet;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.*;
//import org.
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.WriterOutputStream;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.collections.Counter;
import edu.berkeley.nlp.lm.io.LmReaders;
import eu.transcriptorium.lattice.Lattice;
import eu.transcriptorium.lattice.LatticeDecoder;
import eu.transcriptorium.lattice.LatticeToDot;
import eu.transcriptorium.lattice.StandardLatticeFile;
import eu.transcriptorium.lm.ScoreWordSubstitutions;
import eu.transcriptorium.repository.Command;
import eu.transcriptorium.repository.ExternalCommand;
import eu.transcriptorium.repository.FileUtils;
import eu.transcriptorium.repository.PostgresRepository;
import eu.transcriptorium.repository.Repository;
import eu.transcriptorium.repository.SomeUsefulCommands;
import eu.transcriptorium.repository.Repository.FileInfo;
import eu.transcriptorium.suggest.Suggest;
import eu.transcriptorium.util.JSON;
import eu.transcriptorium.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

	Repository repository= null; 

	private static final long serialVersionUID = 1L;
	private String basePath="/datalokaal/Corpus/LM/";



	Map<String, Command> commandMap = null; 
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
		// demo functions
		NONE,
		LIST_LMS,
		SUBSTITUTION,
		EVALUATION,
		COMPLETION,
		SUGGESTION,
		BUILD_LM,
		DECODE_WG,

		// repository functions

		LIST,
		STORE,
		GETMETADATA,
		SEARCHBYNAME,
		SEARCH,
		SETMETADATA,
		CLEAR,
		DELETE,
		INVOKE,
		EXTRACT
	};

	private Map<String,ScoreWordSubstitutions> ScoreWordSubstitutionsMap = new HashMap<String,ScoreWordSubstitutions>(); 
	private Map<String,Suggest> suggesterMap = new HashMap<String,Suggest>();
	private Map<String,NgramLanguageModel> modelMap = new HashMap<String,NgramLanguageModel>();
	private Map<String, String> modelDescriptionMap = new HashMap<String,String>();
	static String lmType = "{type:lm}";
	static Properties lmProps = JSON.toProperties(JSON.fromString(lmType));


	private NgramLanguageModel getModel(String name)
	{
		NgramLanguageModel lm = null;
		System.err.println("requesting model for " + name);
		if ((lm = modelMap.get(name)) != null)
		{
			System.err.println("lm already loaded for " + name + ":" + lm);
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
			System.err.println("attempt to read model from " + languageModel);
			try
			{
				if (!languageModel.endsWith(".bin"))
					lm = LmReaders.readArrayEncodedLmFromArpa(languageModel,false);
				else
					lm = LmReaders.readLmBinary(languageModel);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			System.err.println("finished reading LM");
			modelMap.put(name,lm);
		} else
		{
			System.err.println("no model found for: " + name);
		}
		return lm;
	}

	protected File createTempFile() throws IOException 
	{
		File f = File.createTempFile("repo", ".repo");
		f.deleteOnExit();
		//tempFileSet.add(f.getCanonicalPath());
		return f;
	}

	protected File saveToTempFile(int repoId) throws IOException 
	{
		File f = createTempFile();

		InputStream stream = repository.openFile(repoId);
		try 
		{
			FileUtils.copyStream(stream, f);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return f;
	}

	private NgramLanguageModel getModelFromRepository(String name)
	{
		NgramLanguageModel lm = null;
		System.err.println("requesting model for " + name);
		if ((lm = modelMap.get(name)) != null)
		{
			System.err.println("lm already loaded for " + name + ":" + lm);
			return lm;
		}
		String languageModelFilename = null;
		File languageModelFile = null;

		int id = repository.search(name);

		if (id >= 0)
		{
			try
			{
				languageModelFile = saveToTempFile(id);
				languageModelFile.deleteOnExit();
				languageModelFilename = languageModelFile.getCanonicalPath();
			} catch (Exception e)
			{

			}
		}

		if (languageModelFilename != null)
		{
			System.err.println("attempt to read model from " + languageModelFilename);
			try
			{
				if (!languageModelFilename.endsWith(".bin")) // verkeerde check...
					lm = LmReaders.readArrayEncodedLmFromArpa(languageModelFilename,false);
				else
					lm = LmReaders.readLmBinary(languageModelFilename);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			System.err.println("finished reading LM");
			modelMap.put(name,lm);
		} else
		{
			System.err.println("no model found for: " + name);
		}
		if (languageModelFile != null)
			languageModelFile.delete();
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
		response.setContentType("application/json"); // niet altijd!

		Map<String,String> parameterMap = cloneParameterMap(request);

		MultipartFormData mpfd = null;
		if (ServletFileUpload.isMultipartContent(request))
		{
			mpfd = new MultipartFormData(request); 
			parameterMap = mpfd.getFields();
		}
		java.io.PrintWriter out = response.getWriter( );
		//response.getOutputStream();

		Action action = Action.NONE;

		try
		{
			action = Action.valueOf(parameterMap.get("action").toUpperCase());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		performAction(response, parameterMap, mpfd, out, action);
	}

	private void repositoryAction(HttpServletResponse response, Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out, Action action) throws FileNotFoundException, IOException
	{
		switch(action)
		{


		// repository functions (make this a separate servlet? )
		case LIST:
			com.google.gson.JsonArray L = Repository.Static.list(repository);
			out.println(L.toString());
			break;
		case GETMETADATA:
			com.google.gson.JsonObject p = Repository.Static.getMetadata(repository, Integer.parseInt(parameterMap.get("id")));
			out.println(p.toString());
			break;
		case SEARCHBYNAME:
		{
			com.google.gson.JsonArray result = Repository.Static.searchByName(repository, parameterMap.get("filename"));
			out.println(result.toString());
			break;
		}
		case SEARCH:
		{
			String metadata = parameterMap.get("metadata");
			com.google.gson.JsonObject o = JSON.fromString(metadata);
			Properties p1 = JSON.toProperties(o);
			com.google.gson.JsonArray result = Repository.Static.search(repository, p1);
			out.println(result.toString());
			break;
		}
		case SETMETADATA:
			int id = Integer.parseInt(parameterMap.get("id"));
			String metadata = parameterMap.get("metadata");
			com.google.gson.JsonObject o = JSON.fromString(metadata);
			Properties p1 = JSON.toProperties(o);
			repository.setMetadata(id, p1);
			break;
		case CLEAR:
			repository.clear();
			break;
		case DELETE:
			repository.delete(Integer.parseInt(parameterMap.get("id")));
			break;
		case EXTRACT:
			response.setContentType("application/octet-stream");
			InputStream strm = repository.openFile(Integer.parseInt(parameterMap.get("id")));
			try
			{
				FileUtils.copyStream(strm, new WriterOutputStream(out)); // SLECHT: writer to stream is niet solide
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		case STORE: // upload a list of files and one metadata JSON object
			handleUpload(parameterMap, mpfd,out);
			break;

		case INVOKE:
			invokeCommand(parameterMap, mpfd, out);
			break;

		default:
			out.println("No valid action specified. Doing nothing!");
		}
	}

	private void demoAction(HttpServletResponse response, Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out, Action action) throws FileNotFoundException, IOException {
		switch(action)
		{
		case LIST_LMS:
			out.println(mapToJSON(this.modelDescriptionMap));
			break;

		case SUGGESTION: // bijvoorbeeld
			// http://svprre02:8080/LMServer/LMServer?action=suggestion&lm=Bentham&left=sinister
			System.err.println("suggestion action requested....");
			suggest(parameterMap, out);
			break;

		case BUILD_LM:
			buildLM(mpfd);
			break;

		case DECODE_WG:
			decodeWG(response, parameterMap, mpfd, out);
			break;

		case SUBSTITUTION:
			scoreSubstitution(parameterMap, out);
			break;

			// repository functions (make this a separate servlet? )


		default:
			out.println("No valid action specified. Doing nothing!");
		}
	}
	private void performAction(HttpServletResponse response, Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out, Action action) throws FileNotFoundException, IOException {
		switch(action)
		{
		case LIST_LMS: case SUGGESTION: case BUILD_LM: case DECODE_WG: case SUBSTITUTION:
			this.demoAction(response, parameterMap, mpfd, out, action);
			break;
			// repository functions (make this a separate servlet? )
		case LIST: case GETMETADATA: case SEARCHBYNAME: case SEARCH: 
		case SETMETADATA: case CLEAR: case DELETE: case EXTRACT: 
		case STORE: case INVOKE: 
			this.repositoryAction(response, parameterMap, mpfd, out, action);
			break;

		default:
			out.println("No valid action specified. Doing nothing!");
		}
	}

	private void invokeCommand(Map<String, String> parameterMap,
			MultipartFormData mpfd,  java.io.PrintWriter out)
	{
		// TODO Auto-generated method stub
		String commandName = parameterMap.get("command");
		Command c = commandMap.get(commandName);
		Map<String,Object> args = new HashMap<String,Object>();

		String parametersJSON = parameterMap.get("params");
		if (parametersJSON != null)
		{
			com.google.gson.JsonObject o = JSON.fromString(parametersJSON);
			Properties p1 = JSON.toProperties(o);
			for (Object k: p1.keySet())
			{
				args.put(k.toString(), p1.get(k));
			}
		} else for (String s: parameterMap.keySet())
		{
			if (!s.equalsIgnoreCase("command") && !s.equalsIgnoreCase("action"))
				args.put(s, parameterMap.get(s));
		}

		if (c != null)
		{
			try
			{
				Map<String,Integer> createdResources = c.invoke(args);
				out.println(JSON.intMapToJson(createdResources));
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // hm...
		}
		System.err.println("###finished invocation of command" + c);
	}

	private void handleUpload(Map<String, String> parameterMap,
			MultipartFormData mpfd, java.io.PrintWriter out) throws FileNotFoundException, IOException
	{
		Map <String, File> files = mpfd.getFileMap();

		String metadata = parameterMap.get("metadata"); // hmpf?

		for (String n: files.keySet())
		{
			File f  = files.get(n);
			Properties p = JSON.toProperties(JSON.fromString(metadata));
			p.setProperty("uploadFieldName", n);
			p.setProperty("uploadName", mpfd.getUploadName(n));
			int id = repository.storeFile(new FileInputStream(f), f.getCanonicalPath(), p);
			f.delete();
			out.println(mpfd.getUploadName(n) + ":" + id + " " + p);
		}
		this.getLMsFromRepository(); // you might have uploaded an LM
	}

	private void scoreSubstitution(Map<String, String> parameterMap,
			java.io.PrintWriter out)
	{
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
	}

	private void suggest(Map<String, String> parameterMap,
			java.io.PrintWriter out)
	{
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
	}

	private void decodeWG(HttpServletResponse response,
			Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out)
	{
		response.setContentType("text/html");
		String lmName = parameterMap.get("lm");
		boolean showWordGraphs  = false; 
		String swg = parameterMap.get("showWG");
		if (swg != null) showWordGraphs = swg.matches("yes|true|checked|on");
		NgramLanguageModel lm = this.getModel(lmName);
		LatticeDecoder decoder = new LatticeDecoder();
		decoder.setLanguageModel(lm);
		out.println("<html><head><style type='text/css'>.zoom { font-size: 14pt} \n svg {width: 100%; border-style: solid; border-width: 1px; border-color: pink} \n g { background-color: pink }  </style>" + 
				"<script type='text/javascript' src='JS/toggle.js'></script></head><body>");
		int k=0;
		String template = "<div>_S <span style='color: blue' onclick=\"toggle_element('div_ID')\">[Show word graph]</span>" + 
				"<div style='display: none' id='div_ID'><span class='zoom' onclick=\"zoom_in('_ID')\">+</span> <span cass='zoom' onclick=\"zoom_out('_ID')\">-</span><br> _G</div></div><br>";
		for (String l: mpfd.getNamesOfUploadedfiles())
		{
			Lattice lat = StandardLatticeFile.readLatticeFromFile(l);
			List<String> sentence = decoder.decode(lat);
			String sent = StringUtils.join(sentence, " ");
			sent = sent.replaceAll("<.*?>", "");

			if (showWordGraphs)
			{
				String svg = LatticeToDot.latticeToSVG(lat);
				String id = "g_" + k;
				svg = svg.replaceAll("<svg", "<svg id='" + id + "'");
				String inst = template.replaceAll("_S", sent).replaceAll("_ID", id).replaceAll("_G", svg);
				out.println(inst);
			} else
			{
				out.println(sent + "<br>");
			}
			k++;
		}
	}

	private void buildLM(MultipartFormData mpfd)
	{
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
	}

	private Suggest getSuggester(String lmName)
	{
		// TODO Auto-generated method stub
		Suggest s = null;
		s = this.suggesterMap.get(lmName);
		if (s != null)
		{
			System.err.println("found suggester"  + s);
			return s;
		}

		NgramLanguageModel lm = this.getModel(lmName);

		if (lm != null)
		{
			s = new Suggest(lm);
			this.suggesterMap.put(lmName, s);
			return s;
		}
		return null;
	}

	public void init() // hierin moet je dus al de database uitlezen
	{
		for (String [] x: this.lmLocations)
		{
			this.modelDescriptionMap.put(x[0], x[2]);
		}

		String toolPath = this.getServletContext().getRealPath("/Tools");
		String scriptPath = this.getServletContext().getRealPath("/LMServerScripts");
		
		String connectionParams = getInitParameter("repositoryConnection");
		Properties connectionProperties;
		if (connectionParams != null)
		{
			connectionProperties  = JSON.toProperties(JSON.fromString(connectionParams));
			try {
				connectionProperties.store(System.out, "Connection properties from server:" + connectionParams);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			connectionProperties = PostgresRepository.getDefaultConnectionProperties();

		System.err.println("Tomcat tool path:" + toolPath);
		ExternalCommand.EXTERNAL_TOOL_PATH = toolPath;
		ExternalCommand.LM_SCRIPT_PATH = scriptPath;
		
		repository = new PostgresRepository(connectionProperties);

		commandMap = SomeUsefulCommands.getBasicCommands(repository);
		getLMsFromRepository();
	}

	private void getLMsFromRepository()
	{
		Set<Integer> lmids = repository.search(lmProps);
		for (int id: lmids)
		{
			String name = repository.getName(id);
			//NgramLanguageModel lm = this.getModelFromRepository(name);
			this.modelDescriptionMap.put(name, repository.getMetadataProperty(id, "description"));
		}
	}

	public static String mapToJSON(Map<String,String> map)
	{
		StringWriter  strw = new StringWriter();
		BufferedWriter sw = new BufferedWriter(strw);
		Map<String, Object> properties = new HashMap<String, Object>(1);
		com.google.gson.JsonObject o = JSON.mapToJson(map);
		return o.toString();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException 
	{
		System.err.println("GET REQUEST:" + request.getQueryString());
		doPost(request,response);
	} 
}
