package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class SetUpdateTypeMessage extends AbstractMessage<SetUpdateTypeMessage> implements IMessageData {

	public SetUpdateTypeMessage(String database, String collectionName, Integer index, Integer type) {
		super(MessageCodes.SET_UPDATE_TYPE);
		
		addParam("database", database);
		addParam("name", collectionName);
		addParam("index", index);
		addParam("frequency", type);
	}
}
