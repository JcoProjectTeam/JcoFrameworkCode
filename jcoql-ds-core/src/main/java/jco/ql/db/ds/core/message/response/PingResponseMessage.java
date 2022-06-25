package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class PingResponseMessage extends AbstractMessage<PingResponseMessage> implements IMessageData {

	public PingResponseMessage() {
		super(MessageCodes.PING_RESPONSE);
	}

}
