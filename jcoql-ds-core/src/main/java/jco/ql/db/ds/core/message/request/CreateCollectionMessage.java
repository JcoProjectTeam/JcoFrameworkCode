package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class CreateCollectionMessage extends AbstractMessage<CreateCollectionMessage> implements IMessageData {

	public CreateCollectionMessage(String database, String collectionName) {
		super(MessageCodes.CREATE_COLLECTION);
		
		addParam("database", database);
		addParam("name", collectionName);
	}

}
