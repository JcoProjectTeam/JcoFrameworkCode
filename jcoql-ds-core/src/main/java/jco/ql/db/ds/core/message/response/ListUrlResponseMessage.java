package jco.ql.db.ds.core.message.response;

import java.util.List;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class ListUrlResponseMessage extends AbstractMessage<ListUrlResponseMessage> implements IMessageData {

	public ListUrlResponseMessage(List<String> list) {
		super(MessageCodes.LIST_URL_RESPONSE);
		
		addBodyParam("list", list);
	}

}
