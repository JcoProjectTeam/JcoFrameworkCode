package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;

import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.PingMessage;

@JCoShellCommand
public class PingShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(PingShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public PingShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "ping", value = "Ping the server")
	public String execute() {
		String response = "";
		try {
			PingMessage message = new PingMessage();
			DataInputStream is = connectionManager.getInputStream();
			DataOutputStream os = connectionManager.getOutputStream();
			
			IMessageData pingResponseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
			if(pingResponseMessage != null) {
				logger.info("Received response {}", pingResponseMessage);
				response = "Server alive";
			} else {
				response = "PING command failed";
			}
			
			is.close();
			os.close();
			connectionManager.disconnect();
		} catch (IOException e) {
			logger.error("Error executing ping", e);
		} 
		return response;
	}
}

