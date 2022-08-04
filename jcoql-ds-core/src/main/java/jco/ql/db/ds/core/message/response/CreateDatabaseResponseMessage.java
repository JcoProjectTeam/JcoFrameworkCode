package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class CreateDatabaseResponseMessage extends AbstractMessage<CreateDatabaseResponseMessage> implements IMessageData {

	public CreateDatabaseResponseMessage(boolean success) {
		super(MessageCodes.CREATE_DATABASE_RESPONSE);
		
		addBodyParam("success", success);		
	}

}
