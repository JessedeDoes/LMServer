package eu.transcriptorium.trpclient;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.io.*;
import java.net.*;
import java.util.*;

import eu.transcriptorium.page.ExtractText;
import eu.transcriptorium.util.*;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.*;

import javax.net.ssl.HttpsURLConnection;

import com.sun.org.apache.xml.internal.security.utils.Base64;


/**
 * 
 * @author jesse
 *
 *Hi Jesse,

as discussed, I send you a description to access the REST service of the Transkribus platform.

For testing the service in the browser you can use the debug-login path, for instance to the test server:

https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/auth/login_debug?user=inl&pw=yourPassword

After that call you should be able to access other resources on the server.

(Just for the sake of completeness: the official method (same path without "_debug"), that should be used by applications, is HTTP POST instead of GET and takes user and pw as form parameters, so that credentials do not show up in the server access log.
The session ID returned by the server is then attached to each request for authorization.)

Some of the most important methods for accessing documents are:

-Document list:
https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/docs/list.xml

-Show document, e.g. 62:
https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/docs/62/fulldoc.xml

-Show transcripts for page 1 of this document:
https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/docs/62/1

-List HTR results for page 1 (HTR results are only available for document 62 at the moment):

https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/docs/62/1/wordgraphs

The full webapp description with all methods can be found here:
https://dbis-faxe.uibk.ac.at/TrpServerTesting/rest/application.wadl


Hope this helps for the beginning. Don't hesitate to contact me if you need more information on a specific part or if anything doesn't work as expected.

Best regards,
Philip

 */
public class TranskribusClient 
{
	private String password="inl2014";
	private String user="inl";
	private String host = "dbis-faxe.uibk.ac.at";
	private String application = "TrpServer";
	String service = "https://" + getHost() + "/" + getApplication();
	String sessionId;

	public TranskribusClient()
	{
		init();
	}

	public void authenticate(HttpsURLConnection uc)
	{
		String userpass = getUser() + ":" + getPassword();
		String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));
		uc.setRequestProperty ("Authorization", basicAuth);
	}

	/**
	 * Get the session id from XML response to login
	 */

	public void login()
	{
		try 
		{
			URL u = new URL(service + "/rest/auth/login_debug?user=" + getUser() + "&pw=" + getPassword());
			HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();

			InputStream s = connection.getInputStream();
			BufferedReader b = new BufferedReader( new InputStreamReader(s));
			String xml= "";
			String l;
			while ((l = b.readLine()) != null)
			{
				System.out.println(l);
				xml += l;
			}
			Document d = XML.parseString(xml);
			org.w3c.dom.Element r = d.getDocumentElement();
			Element e = XML.getElementByTagname(r, "sessionId");
			this.sessionId = e.getTextContent();
			System.err.println("Logged in with session id " +sessionId);
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init()
	{
		Authenticator.setDefault (new Authenticator() 
		{
			protected PasswordAuthentication getPasswordAuthentication() {
				System.err.println("eek");
				return new PasswordAuthentication (getUser(), getPassword().toCharArray());
			}
		});
	}

	// docs/62/fulldoc.xml



	public List<String> getTranscriptURLs(String documentId)
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			Document d = executeRequest("docs/" + documentId + "/fulldoc.xml");
			Element r = d.getDocumentElement();
			// get list of pages
			List<Element> pages = XML.getElementsByTagname(r, "pages", false);
			for (Element p: pages)
			{
				// try to fetch the latest transcript for the page (most recent timestamp)
				List<Element> transcripts = XML.getElementsByTagname(p, "transcripts", false);
				// choose most recent???
				String url = "";
				String bestTime = null;
				for (Element t: transcripts)
				{
					Element u = XML.getElementByTagname(t, "url");
					String u1 = u.getTextContent();
					Element ts =  XML.getElementByTagname(t, "timestamp");
					String timestamp = ts.getTextContent();
					if (bestTime == null || bestTime.compareTo(timestamp)  < 0)
					{
						// System.err.println("Update from " + bestTime + " to " + timestamp);
						url = u1;
						bestTime = timestamp;
					}
				}
				list.add(url);
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return list;
	}

	public void downloadTranscripts(String baseFolderName, String documentId)
	{
		File d = new File(baseFolderName + "/doc." + documentId);
		if (d.exists())
		{
			try
			{
				System.err.println("deleting existing directory!!!:"  + d.getCanonicalPath());
				deleteFolder(d);
				//d.delete();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (d.mkdir())
		{
			List<String> urls = getTranscriptURLs(documentId);
			int k=1;
			for (String u: urls)
			{
				try 
				{
					downloadTranscript(d, u, k++);
				} catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}  else
		{
			System.err.println("Unable to create directory for " + documentId);
		}
	}

	/**
	 * 	
	 * @param folderToSaveTo
	 * @param url
	 * @param k
	 * 
	 * 
	 * <br/>
	 * 
	 */
	public void downloadTranscript(File folderToSaveTo, String url, int k)
	{
		try
		{
			URL u = new URL(url);
			URLConnection c = u.openConnection();
			String fileName = guessFilename(c);
			System.err.println("file name: " + fileName);
			File f = new File(folderToSaveTo.getCanonicalPath() +  "/" + fileName); //  + "." + k); 
			if (f.createNewFile())
			{
				BufferedWriter fw = new BufferedWriter(new FileWriter(f));
				InputStream istr = c.getInputStream();
				BufferedReader b = new BufferedReader( new InputStreamReader(istr));
				String l;
				while ((l = b.readLine())!= null)
				{
					fw.write(l + "\n");
				}
				fw.close();
			}
			ExtractText.printText(f.getCanonicalPath());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Content-disposition: attachment; filename="035_328_001.xml"
	 * @param c
	 * @return
	 */
	public String guessFilename(URLConnection c)
	{
		String s = c.getHeaderField("content-disposition");
		// System.err.println(s);
		int i1 = s.indexOf("filename=\"") + "filename=.".length() ;
		int i2 = s.lastIndexOf("\"");
		String fileName = s.substring(i1,i2);
		return fileName;
	}

	public Document executeRequest(String request) throws Exception 
	{
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(getHost(), 443),
				new UsernamePasswordCredentials(getUser(), getPassword()));
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.build();
		try 
		{
			//URL u = new URL(service + "/rest/auth/login_debug?user=" + getUser() + "&pw=" + getPassword());
			HttpGet httpget = new HttpGet(service + "/rest/" + request);

			httpget.setHeader("Cookie",  "JSESSIONID=" + sessionId);
			System.out.println("Executing request " + httpget.getRequestLine());

			CloseableHttpResponse response = httpclient.execute(httpget);

			try 
			{
				System.err.println(response.getStatusLine());
				InputStream s = response.getEntity().getContent();
				BufferedReader b = new BufferedReader( new InputStreamReader(s));
				String xml= "";
				String l;
				while ((l = b.readLine()) != null)
				{
					// System.out.println(l);
					xml += l;
				}
				Document d = XML.parseString(xml);
				return d;
				//EntityUtils.consume(response.getEntity());
			} finally 
			{
				response.close();
			}
		} finally 
		{
			httpclient.close();
		}
	}

	

	String getPassword() {
		return password;
	}

	void setPassword(String password) {
		this.password = password;
	}

	String getUser() {
		return user;
	}

	void setUser(String user) 
	{
		this.user = user;
	}

	String getHost() {
		return host;
	}

	void setHost(String host) {
		this.host = host;
	}

	String getApplication() {
		return application;
	}

	void setApplication(String application) {
		this.application = application;
	}
	
	public static void deleteFolder(File folder) 
	{
	    File[] files = folder.listFiles();
	    if (files!=null) 
	    { //some JVMs return null for empty dirs
	        for(File f: files) 
	        {
	            if(f.isDirectory()) 
	            {
	                deleteFolder(f);
	            } else 
	            {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
	
	public static  void main(String[] args) throws Exception
	{
		TranskribusClient c = new TranskribusClient();
		c.login();
		c.downloadTranscripts("/tmp", args[0]);

		//Document d = c.testIt("docs/62/fulldoc.xml");
		//System.out.println(XML.documentToString(d));
	}
}
