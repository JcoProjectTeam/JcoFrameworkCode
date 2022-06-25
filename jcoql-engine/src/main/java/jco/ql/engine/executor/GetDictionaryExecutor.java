package jco.ql.engine.executor;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.command.GetDictionaryCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;

@Executor(GetDictionaryCommand.class)
public class GetDictionaryExecutor implements IExecutor<GetDictionaryCommand> {

	private DatabaseRegistry databaseRegistry;

	@Autowired
	public GetDictionaryExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	@Override
	public void execute(Pipeline pipeline, GetDictionaryCommand command) throws ExecuteProcessException {
		if (command.getDbName() != null) {
			IDatabase database = databaseRegistry.getDatabase(command.getDbName());
			if (database == null) {
				throw new ExecuteProcessException("[GET DICTIONARY]: Invalid database " + command.getDbName());
			}

			IDocumentCollection collection = database.getCollection(command.getCollectionName());
			pipeline.addDictionary (collection, command.getDictionary());
		} 
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + command.getDictionary() + " dictionary registered");
	}


}
