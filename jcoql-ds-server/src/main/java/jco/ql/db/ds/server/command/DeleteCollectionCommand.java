package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.DeleteCollectionResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class DeleteCollectionCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public DeleteCollectionCommand(DataSourceService dataSourceService) {
		super(MessageCodes.DELETE_COLLECTION);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String name = (String) params.get("name");
		return new DeleteCollectionResponseMessage(dataSourceService.deleteCollection(database, name));
	}

}
