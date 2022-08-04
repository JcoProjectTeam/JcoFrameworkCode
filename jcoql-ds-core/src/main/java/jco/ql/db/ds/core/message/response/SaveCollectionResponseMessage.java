package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class SaveCollectionResponseMessage extends AbstractMessage<SaveCollectionResponseMessage> implements IMessageData {
	
	public SaveCollectionResponseMessage(String database, String collection, boolean success) {
		super(MessageCodes.SAVE_COLLECTION_RESPONSE);
		
		if(database != null && collection != null) {
			addParam("database", database);
			addParam("collection", collection);
		}

		addBodyParam("success", success);
	}

}
