package jco.ql.db.ds.core.message.response;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.datatype.CollectionWrapper;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class GetCollectionResponseMessage extends AbstractMessage<GetCollectionResponseMessage> implements IMessageData {
	
	public GetCollectionResponseMessage(String database, String collectionName, CollectionWrapper collection) {
		super(MessageCodes.GET_COLLECTION_RESPONSE);
		

		addBodyParam("database", database);
		addBodyParam("collection", collectionName);
		if(collection != null) {
			addBodyParam("count", collection.getCount());
			addBodyParam("documents", collection.getDocuments());
			addBodyParam("complete", collection.isComplete());
			addBodyParam("remaining", collection.getRemaining());
			addBodyParam("partialOffset", collection.getPartialOffset());
		}
	}

}
