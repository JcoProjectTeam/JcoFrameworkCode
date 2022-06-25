package jco.ql.db.ds.server.command;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.SaveCollectionResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.service.DataSourceService;

@JcoDsCommand
public class SaveCollectionCommand extends AbstractCommand {
	private static final Logger logger = LoggerFactory.getLogger(SaveCollectionCommand.class);
	
	private final DataSourceService dataSourceService;

	public SaveCollectionCommand(DataSourceService dataSourceService) {
		super(MessageCodes.SAVE_COLLECTION);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		String database = null;
		String collection = null;
		try {
			Map<String, Object> params = request.getParams();
			database = (String) params.get("database");
			collection = (String) params.get("collection");
			boolean append = Optional.ofNullable((Boolean) params.get("append")).orElse(false);
			Map<String, Object> body = request.getBody();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
			if(documents != null) {
				return new SaveCollectionResponseMessage(database, collection, dataSourceService.saveCollection(database, collection, documents, append));
			}
		} catch (Exception e) {
			logger.error("Error executing save collection command", e);
		}
		return new SaveCollectionResponseMessage(database, collection, false);
	}

}
