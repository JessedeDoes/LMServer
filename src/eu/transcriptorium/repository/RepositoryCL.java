package eu.transcriptorium.repository;
import java.io.*;
import java.util.*;

import com.google.gson.*;

import eu.transcriptorium.repository.Repository.FileInfo;
import eu.transcriptorium.util.*;

/**
 * 
 * @author does
 *
 *Command line for repository.
 */

public class RepositoryCL 
{
	Repository r = new PostgresRepository(PostgresRepository.getDefaultConnectionProperties());
	Map<String, Command> commandMap = new HashMap<String,Command>();

	public RepositoryCL()
	{
		commandMap = SomeUsefulCommands.getBasicCommands(r);
	}
/**
 * Misschien nog een extract all to zip toevoegen?
 * Maar wat doe je dan met de metadata?
 * <br>
 * En wellicht een STORE_AS_PLAIN_TEXT of STORE_AS_TEI met conversiefilters uit openconvert?
 * @author does
 *
 */
	public static enum command
	{
		LIST,
		STORE,
		STORE_WITH_METADATA,
		STORE_COLLECTION,
		GETMETADATA,
		SEARCHBYNAME,
		SEARCH,
		SETMETADATA,
		REPLACE_METADATA,
		CLEAR,
		DELETE,
		INVOKE,
		EXTRACT,
		ADDUSER
	}

	public void exec(command c, String[] args) throws Exception
	{
		switch(c)
		{
		case LIST:
			List<FileInfo> V = r.list();
			for (FileInfo fi: V)
			{
				System.out.println(fi + " --> " + r.getMetadata(fi.id));
			};
			break;
		case STORE:
			for (String a: args)
				System.out.println(r.storeFile(new FileInputStream(a), a, null));
			break;
		case STORE_WITH_METADATA:
		{
			JsonObject o = JSON.fromString(args[1]);
			Properties p0 = JSON.toProperties(o);
			System.out.println(r.storeFile(new FileInputStream(args[0]), args[0], p0));
			break;
		}
		case STORE_COLLECTION:
		{
			File dir = new File(args[0]);
			JsonObject o = JSON.fromString(args[1]);
			Properties p0 = JSON.toProperties(o);
			int cid = r.createCollection(args[0], p0);
			for (String s: dir.list())
			{
				Properties p1 = JSON.toProperties(o);
				String p =args[0] + "/" + s;
				System.err.println("Store file with path: " + p);
				int fid = r.storeFile(new FileInputStream(p), p, p1);
				r.addToCollection(cid, fid);
			}
			break;
		}
		case EXTRACT:
			InputStream str = r.openFile(Integer.parseInt(args[0]));
			if (str != null)
				FileUtils.copyStream(str, new File(args[1]));
			break;
		case GETMETADATA:
			Properties p = r.getMetadata(Integer.parseInt(args[0]));
			System.err.println(p.keySet());
			p.store(System.out, "metadata for repository item " + args[0]);
			break;
		case REPLACE_METADATA:
			Repository.Static.replaceMetadata(r, args[0], args[1]);
			break;
		case SEARCH:
		{
			JsonObject o = JSON.fromString(args[0]);
			//System.err.println("Parsed properties:" + o);
			Properties p0 = JSON.toProperties(o);
			Set<Integer> V0 = r.search(p0);
			System.out.println(V0);
		}
		break;
		case SEARCHBYNAME:
			System.out.println(r.searchByName(args[0]));
			break;
		case SETMETADATA:
		{
			int id = Integer.parseInt(args[0]);
			JsonObject o = JSON.fromString(args[1]);
			Properties p1 = JSON.toProperties(o);
			r.setMetadata(id, p1);
			break;
		}	
		case DELETE:
			for (String a: args)
			{
				int id = Integer.parseInt(a);
				r.delete(id);
			}
			break;
		case CLEAR:
			r.clear();
			break;
		case INVOKE: // e.g. java eu.transcriptorium.repository.RepositoryCL INVOKE LM2PFSG '{lm:13,pfsg:my_new_pfsg}'
		{
			Command cmd = commandMap.get(args[0]);
			JsonObject o = JSON.fromString(args[1]);
			Map<String, Object> p1 = JSON.toMap(o);
			System.out.println(JSON.intMapToJson(cmd.invoke(p1)));
			break;
		}	
		case ADDUSER:
		{
			Map<String,String> cr = new HashMap<String,String>();
			cr.put("username", args[0]);
			cr.put("role", args[1]);
			cr.put("password", args[2]);
			r.addUser(cr);
			break;
		}
		}
	}

	public static void main(String[] args) throws Exception
	{
		RepositoryCL  cl = new RepositoryCL();
		if (args.length > 0)
		{
			command c = command.valueOf(args[0]);
			cl.exec(c, Arrays.copyOfRange(args,1, args.length));
		} else
		{
			String s;
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			while ((s = r.readLine() ) != null)
			{
				args = s.split("\\s+");
				command c = command.valueOf(args[0].toUpperCase());
				cl.exec(c, Arrays.copyOfRange(args,1, args.length));
			}
		}
	}
}
