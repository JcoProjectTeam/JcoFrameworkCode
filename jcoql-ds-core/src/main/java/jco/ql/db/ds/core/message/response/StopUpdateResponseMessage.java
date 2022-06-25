package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class StopUpdateResponseMessage extends AbstractMessage< StopUpdateResponseMessage> implements IMessageData {

	public  StopUpdateResponseMessage(boolean success) {
		super(MessageCodes.STOP_UPDATE_RESPONSE);
		
		addBodyParam("success", success);
	}

}
