package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.CreateCollectionMessage;

@JCoShellCommand
public class CreateCollectionShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(CreateCollectionShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public CreateCollectionShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "create-collection", value = "Create a new empty static collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the new collection";
		} else {
			try {
				CreateCollectionMessage message = new CreateCollectionMessage(database, collection);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Collection " + database + "." + collection + " created";
					} else {
						response = "Error creating new collection: " + responseBody.get("errorMessage");
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing create collection", e);
			}
		}
		
		return response;
	}
}

