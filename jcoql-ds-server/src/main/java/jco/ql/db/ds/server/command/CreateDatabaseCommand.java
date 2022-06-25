package jco.ql.db.ds.server.command;

import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.CreateDatabaseResponseMessage;
import jco.ql.db.ds.core.message.response.ErrorResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;
import jco.ql.db.ds.server.util.DataSourceUtils;

@JcoDsCommand
public class CreateDatabaseCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public CreateDatabaseCommand(DataSourceService dataSourceService) {
		super(MessageCodes.CREATE_DATABASE);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String name = (String) params.get("name");
		if(DataSourceUtils.validDatabaseName(name)) {
			return new CreateDatabaseResponseMessage(dataSourceService.createDatabase(name));
		} else {
			return new ErrorResponseMessage("Invalid database name");
		}
	}

}
