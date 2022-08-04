package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class CreateDynamicCollectionResponseMessage extends AbstractMessage<CreateDynamicCollectionResponseMessage> implements IMessageData {

	public CreateDynamicCollectionResponseMessage(boolean success) {
		super(MessageCodes.CREATE_DYNAMIC_COLLECTION_RESPONSE);
		
		addBodyParam("success", success);
	}

}
