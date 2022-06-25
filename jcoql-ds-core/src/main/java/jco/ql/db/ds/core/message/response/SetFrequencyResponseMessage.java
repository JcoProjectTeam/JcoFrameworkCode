package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class SetFrequencyResponseMessage extends AbstractMessage<SetFrequencyResponseMessage> implements IMessageData {

	public SetFrequencyResponseMessage(boolean success) {
		super(MessageCodes.SET_FREQUENCY_RESPONSE);
		
		addBodyParam("success", success);
	}

}
