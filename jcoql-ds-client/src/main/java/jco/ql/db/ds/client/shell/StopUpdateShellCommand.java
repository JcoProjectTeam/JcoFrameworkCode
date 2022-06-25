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
import jco.ql.db.ds.core.message.request.StopUpdateMessage;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JCoShellCommand
public class StopUpdateShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(StopUpdateShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public StopUpdateShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "stop-update", value = "Stop a dynamic collection from updating automatically")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection)
	{
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the collection";
		}{
			try {
				StopUpdateMessage message = new StopUpdateMessage(database, collection);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Update stopped on collection " + collection + " in database " + database;
					} else {
						response = "Error stopping the update " + responseBody.get("errorMessage");
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing StopUpdate", e);
			}
		}
		
		return response;
	}
}




