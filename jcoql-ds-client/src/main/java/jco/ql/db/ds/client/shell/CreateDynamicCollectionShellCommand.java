package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.CreateDynamicCollectionMessage;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JCoShellCommand
public class CreateDynamicCollectionShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(CreateDynamicCollectionShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public CreateDynamicCollectionShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "create-dynamic-collection", value = "Create a new empty dynamic collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection,
			@ShellOption(help = "Url associated with the collection") List<String> url) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the new collection";
		} else if(url == null || url.size() == 0){
			response = "Please specify at least one url for the new collection";
		} else
		{
			try {
				CreateDynamicCollectionMessage message = new CreateDynamicCollectionMessage(database, collection,url);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Dynamic Collection " + collection + " created in database " + database;
					} else {
						response = "Error creating new collection: " + responseBody.get("errorMessage");
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing create dynamic collection", e);
			}
		}
		
		return response;
	}
}


