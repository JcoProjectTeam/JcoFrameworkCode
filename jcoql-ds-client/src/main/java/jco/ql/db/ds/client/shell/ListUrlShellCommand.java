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
import jco.ql.db.ds.core.message.request.ListUrlMessage;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JCoShellCommand
public class ListUrlShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(ListUrlShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public ListUrlShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@SuppressWarnings("unchecked")
	@ShellMethod(key = "list-url", value = "Get the list of Url associated with an existing virtual or dynamic collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the collection";
		} else
	    {
			try {
				ListUrlMessage message = new ListUrlMessage(database, collection);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				Map<String, Object> body = responseMessage.getBody();
				if(responseMessage != null) {
					response = ShellUtils.formatStringOptions((List<String>) body.get("list"));
					
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing ListUrl", e);
			}
		}
		
		return response;
	}
}
