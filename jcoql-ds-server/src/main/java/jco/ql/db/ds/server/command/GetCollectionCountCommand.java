package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.GetCollectionCountResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class GetCollectionCountCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public GetCollectionCountCommand(DataSourceService dataSourceService) {
		super(MessageCodes.GET_COLLECTION_COUNT);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String collection = (String) params.get("collection");
		GetCollectionCountResponseMessage response = new GetCollectionCountResponseMessage(database, collection, dataSourceService.getCollectionCount(database, collection));
		return response;
	}

}
