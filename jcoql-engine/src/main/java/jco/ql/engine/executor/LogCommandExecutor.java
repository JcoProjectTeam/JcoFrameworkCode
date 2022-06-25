package jco.ql.engine.executor;

import java.util.List;
import java.util.StringJoiner;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.LogCommand;
import jco.ql.model.engine.IDocumentCollection;

@Executor(LogCommand.class)
public class LogCommandExecutor implements IExecutor<LogCommand> {
//	private final Logger logger = LoggerFactory.getLogger(LogCommandExecutor.class);

	@Override
	public void execute(Pipeline pipeline, LogCommand command) throws ExecuteProcessException {
		IDocumentCollection collection = pipeline.getCurrentCollection();
		if(collection != null) {
			List<DocumentDefinition> documents = collection.getDocumentList();
			if(documents != null) {
				StringJoiner joiner = new StringJoiner(",");
				for(DocumentDefinition document : documents) {
					joiner.add(document.toString());
				}
// PS LOGGER 	ripristinare? System.out.println(joiner.toString());
			}
		}
	}

}
