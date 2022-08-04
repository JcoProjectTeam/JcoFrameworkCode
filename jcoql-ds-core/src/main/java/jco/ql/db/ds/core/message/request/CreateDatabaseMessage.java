package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class CreateDatabaseMessage extends AbstractMessage<CreateDatabaseMessage> implements IMessageData {

	public CreateDatabaseMessage(String name) {
		super(MessageCodes.CREATE_DATABASE);
		
		addParam("name", name);
	}

}
