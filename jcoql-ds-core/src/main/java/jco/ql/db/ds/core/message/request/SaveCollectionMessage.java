package jco.ql.db.ds.core.message.request;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jco.ql.db.ds.core.annotation.JcoDsMessage;
import jco.ql.db.ds.core.message.AbstractMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.value.DocumentValue;

@JcoDsMessage
public class SaveCollectionMessage extends AbstractMessage<SaveCollectionMessage> implements IMessageData {
	
	public SaveCollectionMessage(String database, String collection, List<DocumentDefinition> documents, boolean convert, boolean append) {
		super(MessageCodes.SAVE_COLLECTION);
		
		if(database != null && collection != null) {
			addParam("database", database);
			addParam("collection", collection);
			addParam("append", append);
		}

		if(documents != null) {
			if(convert) {
				addBodyParam("documents", documents.stream().map(DocumentValue::new).collect(Collectors.toList()));
			} else {
				addBodyParam("documents", documents);
			}
		}
	}

	public SaveCollectionMessage(String database, String collection, List<Map<String, Object>> documents, boolean append) {
		super(MessageCodes.SAVE_COLLECTION);
		
		if(database != null && collection != null) {
			addParam("database", database);
			addParam("collection", collection);
			addParam("append", append);
		}
		
		if(documents != null) {
			addBodyParam("documents", documents);
		}
	}

}
