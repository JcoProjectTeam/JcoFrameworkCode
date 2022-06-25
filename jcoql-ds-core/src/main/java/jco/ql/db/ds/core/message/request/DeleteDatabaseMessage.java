package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class DeleteDatabaseMessage extends AbstractMessage<DeleteDatabaseMessage> implements IMessageData {

	public DeleteDatabaseMessage(String name) {
		super(MessageCodes.DELETE_DATABASE);
		
		addParam("name", name);
	}

}
