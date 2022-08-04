package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class AddUrlResponseMessage extends AbstractMessage<AddUrlResponseMessage> implements IMessageData {

	public AddUrlResponseMessage(boolean success) {
		super(MessageCodes.ADD_URL_RESPONSE);
		
		addBodyParam("success", success);
	}

}
