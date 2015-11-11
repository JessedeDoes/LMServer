package eu.transcriptorium.repository;
import java.io.*;
import java.util.*;

import com.google.gson.*;

import eu.transcriptorium.util.*;

public class RepositoryCL 
{
	Repository r = new PostgresRepository(PostgresRepository.getDefaultProperties());

	public static enum command
	{
		LIST,
		STORE,
		GETMETADATA,
		SEARCH_BY_NAME,
		SEARCH,
		SET_METADATA,
		CLEAR,
		DELETE
	}

	public void exec(command c, String[] args) throws Exception
	{
		switch(c)
		{
		case LIST:
			Set<Integer> V = r.list();
			for (int id: V)
			{
				System.out.println(id + " --> " + r.getMetadata(id));
			};
			break;
		case STORE:
			for (String a: args)
				System.out.println(r.storeFile(new FileInputStream(a), a, null));
			break;
		case GETMETADATA:
			Properties p = r.getMetadata(Integer.parseInt(args[0]));
			p.store(System.out, "jippie");
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
		case SEARCH_BY_NAME:
			System.out.println(r.searchByName(args[0]));
			break;
		case SET_METADATA:
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
