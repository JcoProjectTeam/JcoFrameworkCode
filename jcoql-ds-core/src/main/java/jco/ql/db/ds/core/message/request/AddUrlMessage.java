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
public class AddUrlMessage extends AbstractMessage<AddUrlMessage> implements IMessageData {

	public AddUrlMessage(String database, String collectionName, List<String> url) {
		super(MessageCodes.ADD_URL);
		
		addParam("database", database);
		addParam("name", collectionName);
		addParam("url", url);
	}
}
