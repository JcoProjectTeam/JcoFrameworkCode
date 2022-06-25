package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class GetCollectionCountResponseMessage extends AbstractMessage<GetCollectionCountResponseMessage> implements IMessageData {
	
	public GetCollectionCountResponseMessage(String database, String collectionName, Long count) {
		super(MessageCodes.GET_COLLECTION_COUNT_RESPONSE);
		

		addBodyParam("database", database);
		addBodyParam("collection", collectionName);
		addBodyParam("count", count);
	}

}
