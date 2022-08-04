package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class ErrorResponseMessage extends AbstractMessage<ErrorResponseMessage> implements IMessageData {

	protected ErrorResponseMessage() {
		this("Unknown error");
	}
	
	public ErrorResponseMessage(String errorMsg) {
		super(MessageCodes.ERROR_RESPONSE);
		addBodyParam("success", false);
		addBodyParam("errorMessage", errorMsg);
	}

}
