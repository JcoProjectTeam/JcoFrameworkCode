package jco.ql.db.ds.core.message.request;


import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JcoDsMessage
public class SetFrequencyMessage extends AbstractMessage<SetFrequencyMessage> implements IMessageData {

	public SetFrequencyMessage(String database, String collectionName, Integer index, Integer frequency) {
		super(MessageCodes.SET_FREQUENCY);
		
		addParam("database", database);
		addParam("name", collectionName);
		addParam("index", index);
		addParam("frequency", frequency);
	}
}
