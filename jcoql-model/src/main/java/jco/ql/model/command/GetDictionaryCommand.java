package jco.ql.model.command;

import jco.ql.parser.model.GetDictionary;
import jco.ql.parser.model.Instruction;

public class GetDictionaryCommand implements ICommand {

	private Instruction instruction = null;
	private String dbName;	
	private String collectionName;	
	private String dictionary;
	
	public GetDictionaryCommand(GetDictionary gd) {
		instruction = gd;
		this.dbName = gd.collection.db;
		this.collectionName = gd.collection.collection;
		this.dictionary = gd.collection.alias;
	}

	public String getDbName() {
		return dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getDictionary() {
		return dictionary;
	}


	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
