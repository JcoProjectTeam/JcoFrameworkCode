package jco.ql.db.ds.core.message.request;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class ListDatabasesMessage extends AbstractMessage<ListDatabasesMessage> implements IMessageData {

	public ListDatabasesMessage() {
		super(MessageCodes.LIST_DATABASE);
	}

}
