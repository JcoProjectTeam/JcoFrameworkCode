package jco.ql.db.ds.core.datatype;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jco.ql.db.ds.core.DocumentDefinitionUtils;
import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.datatype.json.GeoJsonValueSerializer;
import jco.ql.db.ds.core.datatype.json.JcoValueDeserializer;
import jco.ql.db.ds.core.message.IMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.GetCollectionMessage;
import jco.ql.db.ds.core.message.request.SaveCollectionMessage;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;

public class JcoDsRemoteDatabase implements IDatabase {
	private static final Logger logger = LoggerFactory.getLogger(JcoDsRemoteDatabase.class);

	private static final int MAX_DOCUMENTS_PER_MESSAGE = 500;
	
	private final String dbName;
	private final ObjectMapper jsonMapper;
	private final ClientConnectionManager clientConnectionManager;
	
	public JcoDsRemoteDatabase(String host, int port, String dbName) {
		this.dbName = dbName;
		
		jsonMapper = new ObjectMapper();
		initDeserializer();
		
		this.clientConnectionManager = new ClientConnectionManager(host, port);
	}

	private void initDeserializer() {
		SimpleModule valueModule = new SimpleModule();
		valueModule.addSerializer(GeometryValue.class, new GeoJsonValueSerializer());
		valueModule.addDeserializer(JCOValue.class, new JcoValueDeserializer());
		jsonMapper.registerModule(valueModule);
	}

	@Override
	public String getName() {
		return dbName;
	}

	@Override
	public IDocumentCollection getCollection(String name) {
		IDocumentCollection collection = null;
		try {
			DataOutputStream os = clientConnectionManager.getOutputStream();
			DataInputStream is = clientConnectionManager.getInputStream();
			
			GetCollectionMessage message = new GetCollectionMessage(this.dbName, name);
			IMessageData responseMessage = doSendAndReceiveMessage(message, is, os);
			
			collection = new JcoDsCollection(name);
			while(responseMessage != null) {
				Map<String, Object> responseBody = responseMessage.getBody();
				if(responseBody != null) {
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> documentArray = (List<Map<String, Object>>) responseBody.get("documents");
					if(documentArray != null) {
						DocumentDefinitionUtils.fromPlainJSON(documentArray)
							.forEach(collection::addDocument);
					}
					
					if(Boolean.FALSE.equals(responseBody.get("complete"))) {
						int remaining = (Integer) responseBody.get("remaining");
						int partialOffset = (Integer) responseBody.get("partialOffset");
						message = new GetCollectionMessage(this.dbName, name, remaining, partialOffset);
						logger.info("Retrieving next segment");
						responseMessage = doSendAndReceiveMessage(message, is, os);
						logger.info("Received response {}", responseMessage);
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			is.close();
			os.close();
		} catch (IOException e) {
			logger.error("Error while retrieving document collection " + name, e);
			JMH.addExceptionMessage("Error while retrieving document collection " + name + "\n" +e.getMessage());
		}
		return collection;
	}

	@Override
	public void addCollection(IDocumentCollection collection) {
		doSaveCollection(collection);
	}

	private IMessageData doSendAndReceiveMessage(IMessage message, DataInputStream is, DataOutputStream os) throws IOException {
//		DataOutputStream os = clientConnectionManager.getOutputStream();
//		DataInputStream is = clientConnectionManager.getInputStream();
		
		clientConnectionManager.sendMessage(message, os);
		IMessageData responseMessage = clientConnectionManager.receiveMessage(is);
//		os.close();
//		is.close();
//		clientConnectionManager.disconnect();
		return responseMessage;
	}

	private Boolean doSaveCollection(IDocumentCollection collection) {
		List<DocumentDefinition> documents = collection.getDocumentList();
		Boolean success = false;
		if(documents == null) {
			logger.warn("No documents inside the collection, skipping save");
			return false;
		}

		try {
			DataOutputStream os = clientConnectionManager.getOutputStream();
			DataInputStream is = clientConnectionManager.getInputStream();
			if(documents.size() > MAX_DOCUMENTS_PER_MESSAGE) {
				boolean append = false;
				int to = 0;
				int size = documents.size();
				for(int from = 0; from < size; from += MAX_DOCUMENTS_PER_MESSAGE) {
					to = to + MAX_DOCUMENTS_PER_MESSAGE > size ? size : to + MAX_DOCUMENTS_PER_MESSAGE;
					SaveCollectionMessage message = new SaveCollectionMessage(this.dbName, collection.getName(), 
							DocumentDefinitionUtils.toPlainJSON(documents.subList(from, to)), append);
					IMessageData responseMessage = doSendAndReceiveMessage(message, is, os);
					if(responseMessage != null && responseMessage.getBody() != null) {
						success = Boolean.TRUE.equals(responseMessage.getBody().get("success"));
					}
					append = true;
					logger.info("Saved {} documents", to);
					if(!success) break;
				}
			} else {
				SaveCollectionMessage message = new SaveCollectionMessage(this.dbName, collection.getName(), 
						DocumentDefinitionUtils.toPlainJSON(documents), false);
				IMessageData responseMessage = doSendAndReceiveMessage(message, is, os);
				if(responseMessage != null && responseMessage.getBody() != null) {
					success = Boolean.TRUE.equals(responseMessage.getBody().get("success"));
				}
			}
			is.close();
			os.close();
		} catch (IOException e) {
			logger.error("Error while saving document collection " + collection.getName(), e);
			JMH.addExceptionMessage("Error while saving document collection " + collection.getName() + "\n" + e.getMessage());
		}
		return success;
	}

}
