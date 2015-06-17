package eu.transcriptorium.trpclient;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.io.*;
import java.net.*;
import java.util.*;

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
	String password="inl2014";
	String user="inl";
	String host = "dbis-faxe.uibk.ac.at";
	String application = "TrpServer";
	String service = "https://" + host + "/" + application;
	String sessionId;

	public TranskribusClient()
	{
		init();
	}

	public void authenticate(HttpsURLConnection uc)
	{
		String userpass = user + ":" + password;

		String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));

		uc.setRequestProperty ("Authorization", basicAuth);
	}


	public void login()
	{
		try 
		{
			URL u = new URL(service + "/rest/auth/login_debug?user=" + user + "&pw=" + password);
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
			System.err.println("Session id:" +sessionId);
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
				return new PasswordAuthentication (user, password.toCharArray());
			}
		});
	}

	// docs/62/fulldoc.xml
	// Content-disposition: attachment; filename="035_328_001.xml"
	
	
	public List<String> getTranscriptURLS(String documentId)
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			Document d = executeRequest("docs/" + documentId + "/fulldoc.xml");
			Element r = d.getDocumentElement();
			List<Element> l = XML.getElementsByTagname(r, "transcripts", false);
			for (Element t: l)
			{
				Element u = XML.getElementByTagname(t, "url");
				list.add(u.getTextContent());
				System.err.println(u.getTextContent());
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return list;
	}
	
	public void downloadTranscripts(String folderName, String documentId)
	{
		File d = new File(folderName + "/doc." + documentId);
		d.mkdir();
		List<String> urls = getTranscriptURLS(documentId);
		int k=1;
		for (String u: urls)
		{
			try {
				downloadTranscript(d, u, k++);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void downloadTranscript(File saveTo, String url, int k)
	{
		try
		{
			URL u = new URL(url);
			URLConnection c = u.openConnection();
			String s = c.getHeaderField("content-disposition");
			System.err.println(s);
			int i1 = s.indexOf("filename=\"") + "filename=.".length() ;
			int i2 = s.lastIndexOf("\"");
			String sub = s.substring(i1,i2);
			System.err.println(sub);
			File f = new File(saveTo.getCanonicalPath() +  "/" + sub + "." + k); 
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
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Document executeRequest(String request) throws Exception 
	{
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(host, 443),
				new UsernamePasswordCredentials(user, password));
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.build();
		try 
		{
			URL u = new URL(service + "/rest/auth/login_debug?user=" + user + "&pw=" + password);
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
					System.out.println(l);
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

	public static  void main(String[] args) throws Exception
	{
		TranskribusClient c = new TranskribusClient();
		c.login();
		c.downloadTranscripts("/tmp", "31");
		//Document d = c.testIt("docs/62/fulldoc.xml");
		//System.out.println(XML.documentToString(d));
	}
}
