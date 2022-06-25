package jco.ql.db.ds.server.command;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.AddUrlResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;
import jco.ql.db.ds.server.collectionsDescriptor.Url;
import jco.ql.db.ds.server.service.DataSourceService;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsCommand
public class AddUrlCommand extends AbstractCommand {
	
	private final DataSourceService dataSourceService;

	public AddUrlCommand(DataSourceService dataSourceService) {
		super(MessageCodes.ADD_URL);
		this.dataSourceService = dataSourceService;
	}
	
	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		Map<String, Object> params = request.getParams();
		String database = (String) params.get("database");
		String name = (String) params.get("name");
		@SuppressWarnings("unchecked")
		List<String> temp = (List<String>) params.get("url");
		List<Url> url = new ArrayList<Url>();
		
		for(String k:temp)
		{
			Url temp2 = new Url(k);
			url.add(temp2);
		}
		
		
		
		return new AddUrlResponseMessage(dataSourceService.addUrl(database,name,url));
	}


}
