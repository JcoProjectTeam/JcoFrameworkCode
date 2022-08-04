package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.DeleteDatabaseResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class DeleteDatabaseCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public DeleteDatabaseCommand(DataSourceService dataSourceService) {
		super(MessageCodes.DELETE_DATABASE);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String name = (String) params.get("name");
		return new DeleteDatabaseResponseMessage(dataSourceService.deleteDatabase(name));
	}

}
