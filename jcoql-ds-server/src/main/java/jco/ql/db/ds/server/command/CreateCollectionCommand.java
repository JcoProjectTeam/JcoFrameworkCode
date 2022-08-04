package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.CreateCollectionResponseMessage;
import jco.ql.db.ds.core.message.response.ErrorResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;
import jco.ql.db.ds.server.util.DataSourceUtils;

@JcoDsCommand
public class CreateCollectionCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public CreateCollectionCommand(DataSourceService dataSourceService) {
		super(MessageCodes.CREATE_COLLECTION);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String name = (String) params.get("name");
		if(DataSourceUtils.validCollectionName(name)) {
			return new CreateCollectionResponseMessage(dataSourceService.createCollection(database, name));
		} else {
			return new ErrorResponseMessage("Invalid collection name");
		}
	}

}
