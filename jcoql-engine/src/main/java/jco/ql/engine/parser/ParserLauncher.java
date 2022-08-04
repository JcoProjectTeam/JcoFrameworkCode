package jco.ql.engine.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import jco.ql.byZun.ZunTimer;
import jco.ql.engine.JcoEngine;
import jco.ql.engine.ServerConfiguration;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.engine.registry.Servers;
import jco.ql.model.command.ICommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.parser.Environment;
import jco.ql.parser.JCoQLParser;
import jco.ql.parser.model.Instruction;

/**
 *
 * @author Savino Lechiancole
 *
 */

public class ParserLauncher {

	private final AnnotationConfigApplicationContext context;
	private final JcoEngine engine;
	private final DatabaseRegistry registry;
	private final Servers s;
	private final List<ServerConfiguration> configurations;

	public ParserLauncher() throws IOException  {
		context = new AnnotationConfigApplicationContext(JcoEngine.class);
		engine = context.getBean(JcoEngine.class);
		registry = context.getBean(DatabaseRegistry.class);
		s = context.getBean(Servers.class);

		configurations = new ArrayList<ServerConfiguration>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("conf");
		readConfig(is);
	}

	public void readConfig(InputStream is) {
		Properties prop = new Properties();
		try {
			prop.load(is);
			int servers = Integer.parseInt(prop.getProperty("servers", "0"));

			String server = "server_";
			String host = "host_";
			String port = "port_";
			String servertype = "server_type_";
			String def = "default_";


			for (int i = 1; i <= servers; i++) {
				String server_value = prop.getProperty(server + i);
				String host_value = prop.getProperty(host + i);
				int port_value = Integer.parseInt(prop.getProperty(port + i));
				String type_value = prop.getProperty(servertype + i);
				boolean default_value = Boolean.parseBoolean(prop.getProperty(def + i, "false"));

				ServerConfiguration c = new ServerConfiguration(server_value, host_value, port_value, type_value, default_value);
				configurations.add(c);
				s.addConfiguration(c);
			}

			is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * for (Configuration con : configurations) { if
	 * (con.getType().toLowerCase().equals("mongodb")) { //MongoDbDatabase db =
	 * MongoDBCreator.createDB(conf.getServer(), conf.getHost(), conf.getPort());
	 * MongoDbDatabase db = new MongoDbDatabase(con.getHost(), con.getPort(),
	 * con.getServer()); registry.registerDatabase(db.getName(), db); } else
	 * if(con.getType().toLowerCase().equals("elasticsearch")) { ElasticDatabase db
	 * = new ElasticDatabase(con.getHost(), con.getPort(), con.getServer());
	 * registry.registerDatabase(db.getName(), db); } else {
	 * System.out.println("Unknown type: " + con.getType()); System.exit(1); } }
	 */
	public void parse(String script) throws ExecuteProcessException {
		JCoQLParser parser;
		List<String> instructions = new ArrayList<String>();
		JMH.reset();

		try {			
			parser = new JCoQLParser(script);
			parser.start();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			JMH.addParserMessage("Generic JCO Parser Exception:\n" + e.getMessage());
			throw new ExecuteProcessException(e.getMessage() + "\n" + sw.toString());
		}
		Environment env = parser.getEnvironment();
		
		
		if (parser.getErrorList().isEmpty()) {
			for (Instruction ins : env.getInstructionList())
				instructions.add(ins.toMultilineString());
			engine.getPipeline().setIstructions(instructions);
		
			for (int i=0; i<env.getInstructionList().size(); i++) {
				try {			
					ICommand instrTans = Translator.translate(env.getInstructionList().get(i));
					ZunTimer.getInstance().reset();
					// PF. Instruction Execution 
					engine.execute(instrTans);
					ZunTimer.getInstance().getMilliTotal(instrTans.getInstruction().getInstructionName());
	//				ZunTimer.getInstance().saveToFile("Temp");
				} catch (Exception e) {
	// *** PF added- to print stack trace on pop-up
					String instr = env.getInstructionList().get(i).toMultilineString();
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					System.out.println("Exception:" + instr + "\n" + sw.toString());
	// *** PF end
					throw new ExecuteProcessException(e.getMessage() + "\nOn line:\n" + instr + "\n\n" + sw.toString());
	
				} finally {
					context.close();
				}
			}
		}
		else {
			String errorMessage = "Parsing error: \n";
			for (int i = 0; i < env.getErrorList().size(); i++) {
				JMH.addParserMessage(parser.getErrorList().get(i));
				errorMessage = errorMessage + (i + 1) + ". " + parser.getErrorList().get(i) + "\n";
			}
		}

	}



	public void backtrack() {
		engine.getPipeline().backtrack(registry);
	}

	public IDocumentCollection getTemporaryCollection() {
		return engine.getPipeline().getCurrentCollection();
	}

	public List<String> getProcess() {
		return engine.getPipeline().getIstructions();
	}

	public Collection<String> getIRList() {
		return engine.getPipeline().getIRList();
	}

	public IDocumentCollection getIRCollection(String collectionName) {
		return engine.getPipeline().getIRCollection(collectionName);
	}

	public List<ServerConfiguration> getConfigurations() {
		return configurations;
	}

	public List<ServerConfiguration> addServer(String s) {
		readConfig(new ByteArrayInputStream(s.getBytes()));
		return getConfigurations();
	}

}
