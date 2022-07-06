package jco.ql.model.command;

import jco.ql.parser.model.GetCollection;
import jco.ql.parser.model.Instruction;

public class GetCollectionCommand implements ICommand {
	private Instruction instruction = null;
	private int type;
	private String collectionName = null;
	private String dbName = null;	
	private String urlString = null;
	

	
	public GetCollectionCommand(GetCollection instr) {
		instruction = instr;
		type = instr.type;
		if (type == GetCollection.DB_TYPE && instr.collection != null) {
			dbName = instr.collection.db;
			collectionName = instr.collection.collection;						
		}
		else 
			urlString = instr.urlString;
	}


	public int getType() {
		return type;
	}
	public String getDbName() {
		return dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getUrlString() {
		return urlString;
	}

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
