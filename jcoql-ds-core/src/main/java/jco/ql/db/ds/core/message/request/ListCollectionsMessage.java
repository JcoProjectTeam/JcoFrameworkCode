package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class ListCollectionsMessage extends AbstractMessage<ListCollectionsMessage> implements IMessageData {
	
	public ListCollectionsMessage(String database) {
		super(MessageCodes.LIST_COLLECTIONS);
		
		if(database != null) {
			addParam("database", database);
		}
	}

}
