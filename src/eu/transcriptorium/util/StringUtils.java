package eu.transcriptorium.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils 
{
	static public String join(Collection<String> list, String conjunction)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list)
		{
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}
	
	static public String join(String[] list, String conjunction)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list)
		{
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	public static Set<String> removeInfix(String s, String infix)
	{
		Set<String> V = new HashSet<String>(); int p=0;
		V.add(s);
		
		while ((p=s.indexOf(infix,p)) >= 0)
		{
			V.add(s.substring(0,p) + s.substring(p + infix.length()));
			p++;
		}
		
		return V;
	}
	
	public static String xmlSingleQuotedEscape(String s) 
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			switch (c) 
			{
			case '\'': sb.append("&quot;"); break;
			case '&': sb.append("&amp;"); break;
			case '<': sb.append("&lt;"); break;
			case '\n': sb.append("&#xA;"); break;

			case '\000': case '\001': case '\002': case '\003': case '\004':
			case '\005': case '\006': case '\007': case '\010': case '\013':
			case '\014': case '\016': case '\017': case '\020': case '\021':
			case '\022': case '\023': case '\024': case '\025': case '\026':
			case '\027': case '\030': case '\031': case '\032': case '\033':
			case '\034': case '\035': case '\036': case '\037':
				// do nothing, these are disallowed characters
				break;
			default:   sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String unescapeOctal(String s)
	{
		if (s == null)
			return null;
		Pattern p = Pattern.compile("\\\\([0-9]+)");
		Matcher m = p.matcher(s);
		int prevEnd = 0;
		StringBuilder sb = new StringBuilder();
		
		List<Byte> bytes = new ArrayList<Byte>();
		
		while (m.find())
		{
			int start = m.start();
			int end = m.end();
			//sb.append(s.substring(prevEnd,start));
			byte[] xx = s.substring(prevEnd,start).getBytes();
			for (byte b: xx)
				bytes.add(b);
			byte o = (byte) Integer.parseInt(m.group(1),8);
			//System.err.println(o);
			bytes.add(o);
			prevEnd = end;
		}
		byte[] xx = s.substring(prevEnd,s.length()).getBytes();
		for (byte b: xx)
			bytes.add(b);
		//sb.append(s.substring(prevEnd,s.length()));
		byte[] ba = new byte[bytes.size()];
		for (int i=0; i < bytes.size(); i++)
			ba[i] = bytes.get(i);
		String r = new String(ba);
		r = r.replaceAll("\\\\'", "'");
		return r;
	}
	
	public static List<String> readStringList(String fileName) throws IOException
	{
		String l;
		BufferedReader b = new BufferedReader(new FileReader(fileName));

		List<String> L = new ArrayList<String>();
		while ((l = b.readLine()) != null)
		{
			L.add(l);
		}
		return L;
	}
	
	public static void main(String[] args)
	{
		System.out.println(unescapeOctal("N\\342\\206\\265a"));
	}
}
