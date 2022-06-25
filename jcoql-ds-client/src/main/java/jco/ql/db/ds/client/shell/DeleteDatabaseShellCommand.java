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
import jco.ql.db.ds.core.message.request.DeleteDatabaseMessage;

@JCoShellCommand
public class DeleteDatabaseShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(DeleteDatabaseShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public DeleteDatabaseShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "delete-database", value = "Delete a database")
	public String execute(@ShellOption(help = "Database name") String name) {
		String response = "";
		if(name == null || name.trim().isEmpty()) {
			response = "Please specify the name of the database to delete";
		} else {
			try {
				DeleteDatabaseMessage message = new DeleteDatabaseMessage(name);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Database " + name + " deleted";
					} else {
						response = "Error deleting database";
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing delete database", e);
			}
		}
		
		return response;
	}
}

