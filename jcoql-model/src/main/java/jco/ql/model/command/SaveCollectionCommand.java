package jco.ql.model.command;

import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.SaveCollection;

public class SaveCollectionCommand implements ICommand {	
	private Instruction instruction = null;
	private String dbName;	
	private String collectionName;
	private String filePathName;
	private int type;
	
	
	public SaveCollectionCommand(SaveCollection saveCollection) {
		instruction = saveCollection;
		type = saveCollection.type;
		if (saveCollection.type == SaveCollection.DB_TYPE) {
			dbName = saveCollection.collection.db;
			collectionName = saveCollection.collection.collection;
			filePathName = null;
		} else {			
			dbName = null;
			collectionName = null;
			filePathName = saveCollection.filePathName;
		}
	}


	public String getDbName() {
		return dbName;
	}

	public int getType() {
		return type;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getFilePathName() {
		return filePathName;
	}

	public String getName () {
    	if (type == SaveCollection.DB_TYPE)
    		return "Save As";
    	return "Save To File";
    }
	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
