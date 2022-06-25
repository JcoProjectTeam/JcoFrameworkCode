package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.SetUpdateTypeResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsCommand
public class SetUpdateTypeCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public SetUpdateTypeCommand(DataSourceService dataSourceService) {
		super(MessageCodes.SET_UPDATE_TYPE);
		this.dataSourceService = dataSourceService;
	}
	
	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String collection = (String) params.get("name");
		Integer index = (Integer) params.get("index");
		Integer type = (Integer) params.get("type");
		
		
		return new SetUpdateTypeResponseMessage(dataSourceService.setUpdateType(database,collection,index,type));
	}


}