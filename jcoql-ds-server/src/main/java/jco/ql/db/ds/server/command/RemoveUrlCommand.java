package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.RemoveUrlResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsCommand
public class RemoveUrlCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public RemoveUrlCommand(DataSourceService dataSourceService) {
		super(MessageCodes.REMOVE_URL);
		this.dataSourceService = dataSourceService;
	}
	
	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String name = (String) params.get("name");
		Integer index = (Integer) params.get("index");
		
		
		return new RemoveUrlResponseMessage(dataSourceService.removeUrl(database,name,index));
	}


}
