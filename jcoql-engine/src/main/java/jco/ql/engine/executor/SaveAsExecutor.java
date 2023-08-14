package jco.ql.engine.executor;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.json.JSONHandler;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.SaveAsCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;

@Executor(SaveAsCommand.class)
public class SaveAsExecutor implements IExecutor<SaveAsCommand> {

	private DatabaseRegistry databaseRegistry;

	@Autowired
	public SaveAsExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}


	@Override
	public void execute(Pipeline pipeline, SaveAsCommand command) throws ExecuteProcessException {
		IDocumentCollection outCollection = null;
		IDocumentCollection collection = pipeline.getCurrentCollection();

		if(collection == null) {
			//PF
	    	String outSt = "{ }";
	    	List<DocumentDefinition> list = new ArrayList<>();
			Document bson = Document.parse(outSt);
			list.add(DocumentUtils.mapDocumentDefinitionFromBson(bson));
			outCollection = new SimpleDocumentCollection(command.getCollectionName(), list);
		} 
		else {
			outCollection = new SimpleDocumentCollection(command.getCollectionName(), collection.getDocumentList());
		}

		if (command.getDbName() != null) {
			IDatabase database = databaseRegistry.getDatabase(command.getDbName());
			if (database != null) {
				database.addCollection(outCollection);						
				JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents saved");
			}
			else
				JMH.addIOMessage("[" + command.getInstruction().getInstructionName() + "] Error:\t DB " + command.getDbName() + " not defined");				
		} 
		// PF. Extends SAVE AS to run as SET INTERMEDIATE
		else {
			JSONHandler j = new JSONHandler();
			String fileName = j.createFile(outCollection, command.getCollectionName());
			pipeline.addFiles(command.getCollectionName(), fileName);			
			JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents saved");
		}
			
		pipeline.addCollection(outCollection);
	}

}
