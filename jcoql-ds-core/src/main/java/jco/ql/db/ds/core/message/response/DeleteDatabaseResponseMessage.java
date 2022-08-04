package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class DeleteDatabaseResponseMessage extends AbstractMessage<DeleteDatabaseResponseMessage> implements IMessageData {

	public DeleteDatabaseResponseMessage(boolean success) {
		super(MessageCodes.DELETE_DATABASE_RESPONSE);
		
		addBodyParam("success", success);
	}

}
