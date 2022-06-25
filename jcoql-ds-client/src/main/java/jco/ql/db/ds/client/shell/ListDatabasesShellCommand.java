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

import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.ListDatabasesMessage;

@JCoShellCommand
public class ListDatabasesShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(ListDatabasesShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public ListDatabasesShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "list-databases", value = "List the available databases")
	public String execute() {
		String response = "";
		try {
			ListDatabasesMessage message = new ListDatabasesMessage();
			DataInputStream is = connectionManager.getInputStream();
			DataOutputStream os = connectionManager.getOutputStream();
			logger.info("Sent message {}", message);
			IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
			logger.info("Received response {}", responseMessage);
			if(responseMessage != null) {
				Map<String, Object> responseBody = responseMessage.getBody();
				@SuppressWarnings("unchecked")
				List<String> databases = (List<String>) responseBody.get("databases");
				if(databases != null) {
					response = ShellUtils.formatStringOptions(databases);
				}
			}
			
			is.close();
			os.close();
			connectionManager.disconnect();
		} catch (IOException e) {
			logger.error("Error executing list databases", e);
		}
		return response;
	}
}

