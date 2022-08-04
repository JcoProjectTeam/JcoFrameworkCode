package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.SetFrequencyResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsCommand
public class SetFrequencyCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public SetFrequencyCommand(DataSourceService dataSourceService) {
		super(MessageCodes.SET_FREQUENCY);
		this.dataSourceService = dataSourceService;
	}
	
	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String collection = (String) params.get("name");
		Integer index = (Integer) params.get("index");
		Integer frequency = (Integer) params.get("frequency");
		
		
		return new SetFrequencyResponseMessage(dataSourceService.setFrequency(database,collection,index,frequency));
	}


}
