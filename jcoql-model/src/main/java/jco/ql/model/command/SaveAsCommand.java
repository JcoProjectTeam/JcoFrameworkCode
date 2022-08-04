package jco.ql.model.command;

import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.SaveAs;

public class SaveAsCommand implements ICommand {	
	private Instruction instruction = null;
	private String dbName;	
	private String collectionName;
	
	
	
	public SaveAsCommand(SaveAs saveAs) {
		instruction = saveAs;
		dbName = saveAs.collection.db;
		collectionName = saveAs.collection.collection;
	}


	public String getDbName() {
		return dbName;
	}

	public String getCollectionName() {
		return collectionName;
	}

    public String getName () {
    	return "Save As";
    }
	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
