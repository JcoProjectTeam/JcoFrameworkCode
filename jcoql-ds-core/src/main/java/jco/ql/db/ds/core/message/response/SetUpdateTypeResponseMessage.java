package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class SetUpdateTypeResponseMessage extends AbstractMessage< SetUpdateTypeResponseMessage> implements IMessageData {

	public  SetUpdateTypeResponseMessage(boolean success) {
		super(MessageCodes.SET_UPDATE_TYPE_RESPONSE);
		
		addBodyParam("success", success);
	}

}
