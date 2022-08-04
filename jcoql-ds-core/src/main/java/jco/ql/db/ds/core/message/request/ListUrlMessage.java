package jco.ql.db.ds.core.message.request;




import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class ListUrlMessage extends AbstractMessage<ListUrlMessage> implements IMessageData {

	public ListUrlMessage(String database, String collectionName) {
		super(MessageCodes.LIST_URL);
		
		addParam("database", database);
		addParam("name", collectionName);
		
	}
}
