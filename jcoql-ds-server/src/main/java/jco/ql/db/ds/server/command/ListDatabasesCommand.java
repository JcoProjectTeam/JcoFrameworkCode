package jco.ql.db.ds.server.command;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.ListDatabasesResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class ListDatabasesCommand extends AbstractCommand {
//	private static final Logger logger = LoggerFactory.getLogger(ListDatabasesCommand.class);
	
	private final DataSourceService dataSourceService;
	
	@Autowired
	public ListDatabasesCommand(DataSourceService dataSourceService) {
		super(MessageCodes.LIST_DATABASE);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		return new ListDatabasesResponseMessage(dataSourceService.listDatabases());
	}

}
