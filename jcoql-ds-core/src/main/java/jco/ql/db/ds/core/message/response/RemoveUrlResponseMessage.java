package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class RemoveUrlResponseMessage extends AbstractMessage<RemoveUrlResponseMessage> implements IMessageData {

	public RemoveUrlResponseMessage(boolean success) {
		super(MessageCodes.REMOVE_URL_RESPONSE);
		
		addBodyParam("success", success);
	}

}
