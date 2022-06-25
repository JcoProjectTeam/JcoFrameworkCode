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
import jco.ql.db.ds.core.message.request.ListCollectionsMessage;

@JCoShellCommand
public class ListCollectionsShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(ListCollectionsShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public ListCollectionsShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@SuppressWarnings("unchecked")
	@ShellMethod(key = "list-collections", value = "List the available collections in a database")
	public String execute(@ShellOption(help = "Database name") String database) {
		String response = "";
		try {
			ListCollectionsMessage message = new ListCollectionsMessage(database);
			DataInputStream is = connectionManager.getInputStream();
			DataOutputStream os = connectionManager.getOutputStream();
			logger.info("Sent message {}", message);
			IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
			logger.info("Received response {}", responseMessage);
			Map<String, Object> body = responseMessage.getBody();
			if(body != null) {
				response = ShellUtils.formatStringOptions((List<String>) body.get("collections"));
			}
			
			is.close();
			os.close();
			connectionManager.disconnect();
		} catch (IOException e) {
			logger.error("Error executing list collections", e);
		}
		return response;
	}
}

