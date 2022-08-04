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
import jco.ql.db.ds.core.message.request.SetUpdateTypeMessage;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JCoShellCommand
public class SetUpdateTypeShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(SetUpdateTypeShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public SetUpdateTypeShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "set-update-type", value = "Set the type of update of a dynamic collection: "+ 
			"0 - (default) Append "  +
	        "     1 - Overwrite  " )
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection,
			@ShellOption(help = "Index of the url which update type must be set") Integer index,
			@ShellOption(help = "Type of update") Integer type) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the collection";
		} else if(index == null || index < 0) {
			response = "Please set a valid index for the url which update type must be set";
		} else if(type == null || type < 0 || type > 1){
			response = "Please specify a valid type of udpate";
		}else
	    {
			try {
				SetUpdateTypeMessage message = new SetUpdateTypeMessage(database, collection, index, type);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Update type of the url in position " + index + " changed correctly on collection " + collection + " in database " + database;
					} else {
						response = "Error setting the new update type: " + responseBody.get("errorMessage");
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing SetUpdateType", e);
			}
		}
		
		return response;
	}
}



