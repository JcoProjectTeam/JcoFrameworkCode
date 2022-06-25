package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.datatype.CollectionWrapper;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.GetCollectionResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class GetCollectionCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public GetCollectionCommand(DataSourceService dataSourceService) {
		super(MessageCodes.GET_COLLECTION);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String collection = (String) params.get("collection");
		Integer limit = (Integer) params.get("limit");
		Integer offset = (Integer) params.get("offset");
		Integer batchSize = (Integer) params.get("batchSize");
		CollectionWrapper collectionWrapper = dataSourceService.getCollection(database, collection, limit, offset, batchSize);
		GetCollectionResponseMessage response = new GetCollectionResponseMessage(database, collection, collectionWrapper);
		return response;
	}

}
