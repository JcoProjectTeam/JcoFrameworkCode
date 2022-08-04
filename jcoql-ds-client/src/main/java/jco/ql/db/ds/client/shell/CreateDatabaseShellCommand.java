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
import jco.ql.db.ds.core.message.request.CreateDatabaseMessage;

@JCoShellCommand
public class CreateDatabaseShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(CreateDatabaseShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public CreateDatabaseShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "create-database", value = "Create a new database")
	public String execute(@ShellOption(help = "Database name") String name) {
		String response = "";
		if(name == null || name.trim().isEmpty()) {
			response = "Please specify a name for the new database";
		} else {
			try {
				CreateDatabaseMessage message = new CreateDatabaseMessage(name);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) {
						response = "Database " + name + " created";
					} else {
						response = "Error creating new database: " + responseBody.get("errorMessage");
					}
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing create database", e);
			}
		}
		
		return response;
	}
}

