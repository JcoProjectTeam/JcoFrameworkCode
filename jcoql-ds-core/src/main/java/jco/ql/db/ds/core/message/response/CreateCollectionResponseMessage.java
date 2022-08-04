package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class CreateCollectionResponseMessage extends AbstractMessage<CreateCollectionResponseMessage> implements IMessageData {

	public CreateCollectionResponseMessage(boolean success) {
		super(MessageCodes.CREATE_COLLECTION_RESPONSE);
		
		addBodyParam("success", success);
	}

}
