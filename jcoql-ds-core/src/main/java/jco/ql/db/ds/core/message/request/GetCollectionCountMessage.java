package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class GetCollectionCountMessage extends AbstractMessage<GetCollectionCountMessage> implements IMessageData {
	
	public GetCollectionCountMessage(String database, String collection) {
		super(MessageCodes.GET_COLLECTION_COUNT);
		
		if(database != null) {
			addParam("database", database);
			addParam("collection", collection);
		}
	}
}
