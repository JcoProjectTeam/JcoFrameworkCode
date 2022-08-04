package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class StopUpdateMessage extends AbstractMessage<StopUpdateMessage> implements IMessageData {

	public StopUpdateMessage(String database, String collectionName) {
		super(MessageCodes.STOP_UPDATE);
		
		addParam("database", database);
		addParam("name", collectionName);
		
	}
}
