package eu.transcriptorium.servlet;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.*;
//import org.
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.WriterOutputStream;
//import org.apache.http.auth.Credentials;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.Principal;
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

	private String  nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";
	private String realm = "lr_server_all_realm";
	private boolean useDigest = false;
	String nonceCount="00000001";
	//String cnonce="0a4f113b";
	private String qop = "auth";
	Map<String, Command> commandMap = null; 
	// ToDo: naar configuratiebestandje

	static class UserInfo
	{
		Map<String, String> credentials;
		Set<String> roles;
	}

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
		EXTRACT,
		LIST_COMMANDS, REPLACE_METADATA
	};

	private Map<String,ScoreWordSubstitutions> ScoreWordSubstitutionsMap = new HashMap<String,ScoreWordSubstitutions>(); 
	private Map<String,Suggest> suggesterMap = new HashMap<String,Suggest>();
	private Map<String,NgramLanguageModel> modelMap = new HashMap<String,NgramLanguageModel>();
	private Map<String, String> modelDescriptionMap = new HashMap<String,String>();
	static String lmType = "{type:lm}";
	static Properties lmProps = JSON.toProperties(JSON.fromString(lmType));




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
		/*
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
		 */
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


		Map<String,String> c = getCredentials(request);
		Set<String> roles = null;
		boolean authorized = false;
		if (c != null)
		{
			roles =repository.getRolesForUser(c);
			System.err.println("Roles for " + c +  " : " + roles);
			if (roles.contains("user") || roles.contains("owner"))
				authorized = true;
		}


		if (!authorized)
		{
			response.setStatus(401);
			String xRequestedWith = request.getHeader("X-Requested-With");
			boolean isAjaxRequest = "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
			if (!isAjaxRequest)
			{
				if (useDigest)
					response.setHeader("WWW-Authenticate", "Digest realm=\"" + realm + "\", nonce=\"" + nonce + "\"" + ", qop=\"" + qop + "\"");
				else
					response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm);
			}
			return;
		}

		UserInfo ui = new UserInfo();
		ui.credentials = c;
		ui.roles = roles;

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

		performAction(response, parameterMap, mpfd, out, action, ui);
	}

	private void repositoryAction(HttpServletResponse response, Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out, Action action, UserInfo ui) throws FileNotFoundException, IOException
	{
		Set<String> roles = ui.roles;
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
			this.getLMsFromRepository();
			break;
		case REPLACE_METADATA:
			Repository.Static.replaceMetadata(repository, parameterMap.get("search"), parameterMap.get("replace"));
			break;
		case CLEAR:
			if (roles.contains("owner"))
				repository.clear();
			break;
		case DELETE:
			if (roles.contains("owner"))
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
		case LIST_COMMANDS:
			List<String> cmds = new ArrayList<String>();
			for (Command s: this.commandMap.values())
				cmds.add(s.toString());
			out.println(cmds);
			break;
		default:
			out.println("No valid action specified. Doing nothing!");
		}
	}

	private void demoAction(HttpServletResponse response, Map<String, String> parameterMap, MultipartFormData mpfd,
			java.io.PrintWriter out, Action action, UserInfo ui) throws FileNotFoundException, IOException 
	{
		switch(action)
		{
		case LIST_LMS:
			this.getLMsFromRepository();
			out.println(mapToJSON(this.modelDescriptionMap));
			break;

		case SUGGESTION: // bijvoorbeeld
			// http://svprre02:8080/LMServer/LMServer?action=suggestion&lm=Bentham&left=sinister
			System.err.println("suggestion action requested....");
			suggest(parameterMap, out);
			break;

		case BUILD_LM:
			buildLM(mpfd,ui);
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
			java.io.PrintWriter out, Action action, UserInfo ui) throws FileNotFoundException, IOException {
		switch(action)
		{
		case LIST_LMS: case SUGGESTION: case BUILD_LM: case DECODE_WG: case SUBSTITUTION:
			this.demoAction(response, parameterMap, mpfd, out, action, ui);
			break;
			// repository functions (make this a separate servlet? )
		case LIST: case GETMETADATA: case SEARCHBYNAME: case SEARCH: 
		case SETMETADATA: case REPLACE_METADATA: case CLEAR: case DELETE: case EXTRACT: 
		case STORE: case INVOKE: case LIST_COMMANDS:
			this.repositoryAction(response, parameterMap, mpfd, out, action, ui);
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
		Command command = commandMap.get(commandName);
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

		if (command != null)
		{
			try
			{
				Map<String,Integer> createdResources = command.invoke(args);
				out.println(JSON.intMapToJson(createdResources));
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // hm...
		}
		System.err.println("###finished invocation of command" + command);
	}

	private void handleUpload(Map<String, String> parameterMap,
			MultipartFormData mpfd, java.io.PrintWriter out) throws FileNotFoundException, IOException
	{
		Map <String, File> files = mpfd.getFileMap();

		String metadata = parameterMap.get("metadata"); 
		List<Integer> ids = new ArrayList<Integer>();
		for (String n: files.keySet())
		{
			File f  = files.get(n);
			Properties p = JSON.toProperties(JSON.fromString(metadata));
			p.setProperty("uploadFieldName", n);
			p.setProperty("uploadName", mpfd.getUploadName(n));
			String fn =  mpfd.getUploadName(n);
			if (p.getProperty("filename") != null)
				fn =p.getProperty("filename");
			int id = repository.storeFile(new FileInputStream(f), fn, p);
			f.delete();
			//out.println(mpfd.getUploadName(n) + ":" + id + " " + p);
			ids.add(id);
		}
		out.println(ids);
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
		NgramLanguageModel lm = this.getModelFromRepository(lmName);
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

	private void buildLM(MultipartFormData mpfd, UserInfo ui)
	{
		String name = mpfd.getFields().get("name");
		System.err.println("Building model named "+ name);
		String description = mpfd.getFields().get("description");
		if (description == null)
		{
			description = " Undocumented, created in web interface by " + ui.credentials.get("username");
		}
		File newLMFileName = new LMBuilder().buildLM(3, mpfd.getNamesOfUploadedfiles());
		Properties p = new Properties();
		p.setProperty("description", description);
		p.setProperty("type", "lm");
		p.setProperty("owner", ui.credentials.get("username"));
		p.setProperty("createdAt", new Date(System.currentTimeMillis()).toString());

		try
		{
			repository.storeFile(new FileInputStream(newLMFileName), newLMFileName.getCanonicalPath(), p);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		newLMFileName.delete();
		this.getLMsFromRepository();
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

		NgramLanguageModel lm = this.getModelFromRepository(lmName);

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

		String toolPath = this.getServletContext().getRealPath("/Tools");
		String scriptPath = this.getServletContext().getRealPath("/LMServerScripts");

		String connectionParams = getInitParameter("repositoryConnection");
		Properties connectionProperties;
		if (connectionParams != null)
		{
			connectionProperties  = JSON.toProperties(JSON.fromString(connectionParams));
			try {
				connectionProperties.store(System.out, "Connection properties from server:" + connectionParams);
			} catch (IOException e) 
			{
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

	public static String H(String s)
	{
		try 
		{
			byte[] bytesOfMessage = s.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			//md.
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<thedigest.length;i++) 
			{
				hexString.append(Integer.toHexString(0xFF & thedigest[i]));
			}
			return hexString.toString();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return "oompf";
	}
	/**
	 * 
	 * @param req
	 * @return
	 * Authorization: Digest
           username="<username>",             -- required
           realm="<realm>",                   -- required
           nonce="<nonce>",                   -- required
           uri="<requested-uri>",             -- required
           response="<digest>",               -- required
           message="<message-digest>",        -- OPTIONAL
           opaque="<opaque>"                  -- required if provided by server

        where <digest> := H( H(A1) + ":" + N + ":" + H(A2) )
  and <message-digest> := H( H(A1) + ":" + N + ":" + H(<message-body>) )

        where:

                A1 := U + ':' + R + ':' + P
                A2 := <Method> + ':' + <requested-uri>

                with:
                        N -- nonce value
                        U -- username
                        R -- realm
                        P -- password
                        <Method> -- from header line 0
                        <requested-uri> -- uri sans proxy/routing
	 */
	private static String getDigest(HttpServletRequest req, String user, String password, String realm, String nonce)
	{
		String uri = "/dir/index.html";
		// String uri = req.getRequestURI() + "?" + req.getQueryString()
		String A1 = user + ":" + realm + ":" + password;
		String A2 = req.getMethod() + ':' + uri; //req.getRequestURL();
		System.err.println("A1: " + A1 + " H(A1): " +  H(A1));
		System.err.println("A2: " + A2 + " H(A2): " +  H(A2));
		String digest = H(H(A1) + ":" + nonce +  ":" + H(A2));
		return digest;
		//String d = md.
	}
	// response=MD5(HA1:nonce:nonceCount:clientNonce:qop:HA2)
	private static String getExpectedDigestResponceWithAuthQOP(HttpServletRequest req, String user, String password, String realm, String nonce, String cnonce, String nonceCount)
	{
		//String uri = "/dir/index.html";
		//cnonce = cnonce.replaceAll("=", "");
		String uri = req.getRequestURI() + "?" + req.getQueryString();

		System.err.println("user=" + user);
		System.err.println("password=" + password);
		System.err.println("realm=" + realm);
		System.err.println("nonce=" + nonce);
		System.err.println("cnonce=" + cnonce);
		System.err.println("nonceCount=" + nonceCount);
		String A1 = user + ":" + realm + ":" + password;
		String A2 = req.getMethod() + ':' + uri; //req.getRequestURL();
		System.err.println("A1: " + A1 + " H(A1): " +  H(A1));
		System.err.println("A2: " + A2 + " H(A2): " +  H(A2));
		String z = H(A1) + ":" + nonce +  ":" + nonceCount + ":" + cnonce + ":auth:" + H(A2);
		System.err.println("z=" + z);
		String digest = H(z);
		return digest;
		//String d = md.
	}
	// Authentication header: Digest username="jesse", realm="just_a_realm", nonce="1", uri="/LMServer/LMServer?action=LIST", response="d6d5d5f85b9701ca6c8c1adcfb4e7c33"

	public String getPasswordForUser(String user)
	{
		return "dedoes";
	}

	public Map<String,String> getCredentials(HttpServletRequest req) 
	{
		String authHeader = req.getHeader("Authorization");
		if (authHeader != null) 
		{
			System.err.println("Authentication header: " + authHeader);
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) 
			{
				String method = st.nextToken();

				if (method.equalsIgnoreCase("Basic")) 
				{
					try 
					{
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
						System.err.println("Credentials: " + credentials);
						int p = credentials.indexOf(":");
						if (p != -1) 
						{
							String login = credentials.substring(0, p).trim();
							String password = credentials.substring(p + 1).trim();
							Map<String,String> m = new HashMap<String,String>();
							m.put("method", "basic");
							m.put("username", login);
							m.put("password", password);
							return m;
						} else 
						{
							// LOG.error("Invalid authentication token");
						}
					} catch (UnsupportedEncodingException e) 
					{
						// LOG.warn("Couldn't retrieve authentication", e);
					}
				} else if (method.equalsIgnoreCase("digest"))
				{
					String rest = authHeader.substring(authHeader.indexOf("igest")+5);
					rest = rest.trim();

					Map<String,String> digestProperties = new HashMap<String,String>();
					String[] parts = rest.split(",\\s*");
					for (String p: parts)
					{
						int pos =p.indexOf("=");
						if (pos > 0)
						{
							String n = p.substring(0, pos);
							String v = p.substring(pos+1);
							v = v.replaceAll("\"", "").trim();
							digestProperties.put(n, v);
						}
					}
					System.err.println(digestProperties);
					return(digestProperties);
					/*
	            	String user  = digestProperties.get("username");
	            	String expectedResponse = getExpectedDigestResponceWithAuthQOP(req, user, getPasswordForUser(user), 
	            			digestProperties.get("realm"), digestProperties.get("nonce"), digestProperties.get("cnonce"), digestProperties.get("nc"));
	            	String response = digestProperties.get("response");
	            	System.err.println("expected response: " + expectedResponse +  " received: " + response);
	            	if (expectedResponse.equals(response))
	            	{
	            		return digestProperties;
	            	}
					 */
				}
			}
		}

		return null;
	}
}
