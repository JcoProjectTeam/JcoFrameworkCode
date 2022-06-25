package jco.ql.db.ds.core.message.request;




import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class RemoveUrlMessage extends AbstractMessage<RemoveUrlMessage> implements IMessageData {

	public RemoveUrlMessage(String database, String collectionName, Integer index) {
		super(MessageCodes.REMOVE_URL);
		
		addParam("database", database);
		addParam("name", collectionName);
		addParam("index", index);
	}
}