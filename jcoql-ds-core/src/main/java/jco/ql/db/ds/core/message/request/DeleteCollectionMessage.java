package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class DeleteCollectionMessage extends AbstractMessage<DeleteCollectionMessage> implements IMessageData {

	public DeleteCollectionMessage(String database, String collectionName) {
		super(MessageCodes.DELETE_COLLECTION);
		
		addParam("database", database);
		addParam("name", collectionName);
	}

}
