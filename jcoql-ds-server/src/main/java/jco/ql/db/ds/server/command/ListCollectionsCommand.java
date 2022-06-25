package jco.ql.db.ds.server.command;

import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.ListCollectionsResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class ListCollectionsCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public ListCollectionsCommand(DataSourceService dataSourceService) {
		super(MessageCodes.LIST_COLLECTIONS);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		String database = (String) request.getParams().get("database");
		return new ListCollectionsResponseMessage(database, dataSourceService.listCollections(database));
	}

}
