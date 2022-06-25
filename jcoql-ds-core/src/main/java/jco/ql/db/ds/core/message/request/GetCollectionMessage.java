package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class GetCollectionMessage extends AbstractMessage<GetCollectionMessage> implements IMessageData {
	
	public GetCollectionMessage(String database, String collection) {
		this(database, collection, -1, 0);
	}
	
	public GetCollectionMessage(String database, String collection, Integer limit, Integer offset) {
		super(MessageCodes.GET_COLLECTION);
		
		if(database != null) {
			addParam("database", database);
			addParam("collection", collection);
			addParam("limit", limit);
			addParam("offset", offset);
		}
	}

	public GetCollectionMessage(String database, String collection, Integer limit, Integer offset, Integer documentsPerBatch) {
		this(database, collection, limit, offset);
		
		if(documentsPerBatch != null && documentsPerBatch > 0) {
			addParam("batchSize", documentsPerBatch);
		}
	}
}
