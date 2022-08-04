package jco.ql.db.ds.server.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.CreateDynamicCollectionResponseMessage;
import jco.ql.db.ds.core.message.response.ErrorResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.collectionsDescriptor.Url;
import jco.ql.db.ds.server.service.DataSourceService;
import jco.ql.db.ds.server.util.DataSourceUtils;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsCommand
public class CreateDynamicCollectionCommand extends AbstractCommand{
	
	private final DataSourceService dataSourceService;

	public CreateDynamicCollectionCommand(DataSourceService dataSourceService) {
		super(MessageCodes.CREATE_DYNAMIC_COLLECTION);
		this.dataSourceService = dataSourceService;
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String name = (String) params.get("name");
		@SuppressWarnings("unchecked")
		List<String> url = (List<String>)params.get("url");
		if(DataSourceUtils.validCollectionName(name)) {
				List<Url> urls = new ArrayList<Url>();
				for(String k: url)
				{
					Url newUrl = new Url(k);
					urls.add(newUrl);
				}
			return new CreateDynamicCollectionResponseMessage(dataSourceService.createDynamicCollection(database, name,urls));
		} else {
			return new ErrorResponseMessage("Invalid collection name");
		}
	}


}

