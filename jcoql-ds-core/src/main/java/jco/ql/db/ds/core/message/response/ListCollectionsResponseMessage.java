package jco.ql.db.ds.core.message.response;

import java.util.List;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

@JcoDsMessage
public class ListCollectionsResponseMessage extends AbstractMessage<ListCollectionsResponseMessage> implements IMessageData {
	
	public ListCollectionsResponseMessage(String database, List<String> collectionsList) {
		super(MessageCodes.LIST_COLLECTIONS_RESPONSE);
		
		if(database != null && collectionsList != null) {
			setBodyData(database, collectionsList);
		}
	}

	private void setBodyData(String database, List<String> collectionList) {
		addBodyParam("database", database);
		addBodyParam("collections", collectionList);
	}
	
}
