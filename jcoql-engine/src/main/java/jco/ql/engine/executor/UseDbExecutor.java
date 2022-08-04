package jco.ql.engine.executor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.db.ds.core.datatype.JcoDsRemoteDatabase;
import jco.ql.db.elasticsearch.ElasticDatabase;
import jco.ql.db.mongodb.MongoDbDatabase;
import jco.ql.engine.Pipeline;
import jco.ql.engine.ServerConfiguration;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.engine.registry.Servers;
import jco.ql.model.command.UseDbCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.JMH;
import jco.ql.parser.model.UseDb;
import jco.ql.parser.model.util.DbName;

@Executor(UseDbCommand.class)
public class UseDbExecutor implements IExecutor<UseDbCommand> {

	private DatabaseRegistry registry;
	private Servers s;

	@Autowired
	public UseDbExecutor(DatabaseRegistry registry, Servers s) {
		this.registry = registry;
		this.s = s;
	}

	@Override
	public void execute(Pipeline pipeline, UseDbCommand command) throws ExecuteProcessException {
		IDatabase db = null;
		String dbname = "";
		List<String> dbnames = new ArrayList<>();
		if (command.getType() == UseDb.DEFAULT) {
			// ON DEFAULT SERVER
			for (ServerConfiguration c : s.getConfigurations()) {
				if (c.isDefault()) {
					for (DbName name : command.getDbNames()) {
						if (name.alias != null) {
							dbname = name.alias;
						} else {
							dbname = name.db;
						}
						db = createDB(c.getType(), c.getHost(), c.getPort(), name.db);
						dbnames.add(dbname);
						// registry.registerDatabase(dbname, db);
					}
				}
			}
		} else if (command.getType() == UseDb.SERVER) {
			// ON SERVER serverName
			for (ServerConfiguration c : s.getConfigurations()) {
				if (c.getServer().equals(command.getServerName())) {
					for (DbName name : command.getDbNames()) {
						if (name.alias != null) {
							dbname = name.alias;
						} else {
							dbname = name.db;
						}
						db = createDB(c.getType(), c.getHost(), c.getPort(), name.db);
						dbnames.add(dbname);
						// registry.registerDatabase(dbname, db);
					}
				}
			}
		} else if (command.getType() == UseDb.SERVER_CONNECTED) {
			// ON SERVER serverType serverName
			String serverType = command.getServerType().toLowerCase();
			if (serverType.startsWith("'"))
				serverType = serverType.substring(1, serverType.length() - 1);
			URL u = null;
			for (DbName name : command.getDbNames())
				try {
					u = new URL(command.getConnectionString().substring(1, command.getConnectionString().length() - 1));
					String host = u.getHost();
					if (u.getPath() != null)
						host = host + u.getPath();

					if (name.alias != null)
						dbname = name.alias;
					else
						dbname = name.db;
					db = createDB(serverType, host, u.getPort(), name.db);
					dbnames.add(dbname);
					// registry.registerDatabase(dbname, db);
				} catch (MalformedURLException e) {
					JMH.addIOMessage("Error: the URL is incorrect");
					System.out.println("Error: the URL is incorrect");
					System.exit(1);
				}
		}

    	// PF. 2022.03.24 - New Policy... in case of already existing USE DB, a message is emitted before the newer version replaces the old one
		if (registry.getDatabase(dbname) != null) 
			JMH.addIOMessage("[" + command.getInstruction().getInstructionName() + "]: reference to database " + dbname + " has been replaced.");
		registry.registerDatabase(dbname, db);
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + dbname);
		// PF - what's for? // ZUN CHECK*
		pipeline.addCollection(dbnames);

	}
/*
	private void addDatabase(IDatabase db, String dbname) {
		// Verifico se il database esiste o meno
		if (registry.getDatabase(dbname) == null) {
			registry.registerDatabase(dbname, db);
		} else {
			throw new ExecuteProcessException("[USE DB]: Error: database " + dbname + " already exists");
		}
	}
*/
	private IDatabase createDB(String type, String host, int port, String dbname) {
		IDatabase result = null;
		if (type.toLowerCase().equals("mongodb")) {
			result = new MongoDbDatabase(host, port, dbname);
		} else if (type.toLowerCase().equals("elasticsearch")) {
			result = new ElasticDatabase(host, port, dbname);
		} else if (type.toLowerCase().equals("jcods")) {
			result = new JcoDsRemoteDatabase(host, port, dbname);
		}
		return result;
	}

}
