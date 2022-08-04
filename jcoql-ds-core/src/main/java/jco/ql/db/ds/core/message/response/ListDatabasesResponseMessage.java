package jco.ql.db.ds.core.message.response;

import java.util.List;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class ListDatabasesResponseMessage extends AbstractMessage<ListDatabasesResponseMessage> implements IMessageData {
	
	public ListDatabasesResponseMessage(List<String> databaseList) {
		super(MessageCodes.LIST_DATABASE_RESPONSE);
		
		if(databaseList != null) {
			setBodyData(databaseList);
		}
	}

	private void setBodyData(List<String> databaseList) {
		addBodyParam("databases", databaseList);
	}
	
}
