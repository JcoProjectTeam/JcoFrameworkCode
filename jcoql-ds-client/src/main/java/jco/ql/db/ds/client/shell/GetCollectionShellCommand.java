package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import jco.ql.db.ds.core.DocumentDefinitionUtils;
import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.GetCollectionMessage;

@JCoShellCommand
public class GetCollectionShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(GetCollectionShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public GetCollectionShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "get-collection", value = "Get the content of a collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection, 
			@ShellOption(defaultValue = "-1", help = "Maximum number of documents to retrieve") Integer limit,
			@ShellOption(defaultValue = "0", help = "The first document to retrieve") Integer offset,
			@ShellOption(defaultValue = "false", help = "Disable console output log") boolean disableOutput) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the new collection";
		} else {
			long startTime = System.currentTimeMillis();
			int documentCount = 0;
			try {
				GetCollectionMessage message = new GetCollectionMessage(database, collection, limit, offset);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				List<Map<String, Object>> allDocuments = new LinkedList<Map<String, Object>>();
				while(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");
					if(documents != null) {
						if(disableOutput) {
							documentCount += documents.size();
						} else {
							allDocuments.addAll(documents);
						}
					} else {
						response = "Error getting collection";
						break;
					}
					
					if(Boolean.FALSE.equals(responseBody.get("complete"))) {
						int remaining = (Integer) responseBody.get("remaining");
						int partialOffset = (Integer) responseBody.get("partialOffset");
						message = new GetCollectionMessage(database, collection, remaining, partialOffset);
						logger.info("Retrieving next segment");
						responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
						logger.info("Received response {}", responseMessage);
					} else {
						break;
					}
				}
				if(disableOutput) {
					response += "Got " + documentCount + " documents";
				} else {
					response += DocumentDefinitionUtils.prettyPrintJSON(allDocuments);
				}
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing get collection", e);
			}
			logger.info("Execution time: {} ms", System.currentTimeMillis() - startTime);
		}
		
		return response;
	}
}

