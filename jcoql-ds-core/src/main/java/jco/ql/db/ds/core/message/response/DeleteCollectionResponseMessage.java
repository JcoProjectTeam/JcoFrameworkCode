package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class DeleteCollectionResponseMessage extends AbstractMessage<DeleteCollectionResponseMessage> implements IMessageData {

	public DeleteCollectionResponseMessage(boolean success) {
		super(MessageCodes.DELETE_COLLECTION_RESPONSE);
		
		addBodyParam("success", success);
	}

}
