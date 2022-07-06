package jco.ql.model.command;
import java.util.List;

import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.UseDb;
import jco.ql.parser.model.util.DbName;

public class UseDbCommand implements ICommand {
	private Instruction instruction = null;

	private List<DbName> list;
	private boolean defaultServer;
	private String serverName;
	private String connectionString;
	private String serverType;
	private int type;
	
	
	public UseDbCommand(UseDb useDbInstr) {
		instruction = useDbInstr;
		list = useDbInstr.dbList;
		type = useDbInstr.type;
		defaultServer = (useDbInstr.type == UseDb.DEFAULT);
		serverName = useDbInstr.server;		
		serverType = useDbInstr.server;		
		connectionString = useDbInstr.connectionString;
	}

	
	//ON DEFAULT SERVER
	public UseDbCommand(List<DbName> list, boolean defaultServer) {
		this.list = list;
		this.defaultServer = defaultServer;
		this.type = UseDb.DEFAULT;
	}
	
	//ON SERVER serverName
	public UseDbCommand(List<DbName> list, String serverName) {
		this.list = list;
		this.defaultServer = false;
		this.serverName = serverName;
		this.type = UseDb.SERVER;
	}
	
	//ON SERVER serverType serverName
	public UseDbCommand(List<DbName> list, String serverType, String connectionString) {
		this.list = list;
		this.serverType = serverType;
		this.connectionString = connectionString;
		this.defaultServer = false;
		this.type = UseDb.SERVER_CONNECTED;
	}
	
	public List<DbName> getDbNames(){
		return list;
	}
	
	public boolean isDefaultServer() {
		return defaultServer;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getServerType() {
		return serverType;
	}
	
	public String getConnectionString() {
		return connectionString;
	}
	
	public int getType() {
		return type;
	}
	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
