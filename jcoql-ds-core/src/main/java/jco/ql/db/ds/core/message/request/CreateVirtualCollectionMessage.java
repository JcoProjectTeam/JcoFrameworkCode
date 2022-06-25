package jco.ql.db.ds.core.message.request;

import java.util.List;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class CreateVirtualCollectionMessage extends AbstractMessage<CreateVirtualCollectionMessage> implements IMessageData {

	public CreateVirtualCollectionMessage(String database, String collectionName, List<String> url) {
		super(MessageCodes.CREATE_VIRTUAL_COLLECTION);
		
		addParam("database", database);
		addParam("name", collectionName);
		addParam("url", url);
	}
}
