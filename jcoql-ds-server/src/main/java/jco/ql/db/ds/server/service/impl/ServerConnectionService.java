package jco.ql.db.ds.server.service.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jco.ql.db.ds.core.DSConstants;
import jco.ql.db.ds.server.command.CommandRegistry;
import jco.ql.db.ds.server.connection.ClientConnectionHandler;
import jco.ql.db.ds.server.service.DataSourceService;

@Service
@Profile("server")
public class ServerConnectionService implements DSConstants {
	private static final Logger logger = LoggerFactory.getLogger(ServerConnectionService.class);
	
	private final CommandRegistry commandRegistry;
	private final ConcurrentHashMap<String, ClientConnectionHandler> clientConnections;

	private Properties settings;
	private Properties instanceMetadata;
	
	@Autowired
	public ServerConnectionService(CommandRegistry commandRegistry, DataSourceService dataSourceService) {
		this.commandRegistry = commandRegistry;
		
		this.clientConnections = new ConcurrentHashMap<>();
		
		settings = dataSourceService.getServerSettings();
		instanceMetadata = dataSourceService.getInstanceMetadata();
		
		startupConnections();
	}
	
	@PreDestroy
	protected void deinit() {
		for(ClientConnectionHandler clientConnection : clientConnections.values()) {
			if(clientConnection.isRunning()) {
				clientConnection.stop();
			}
		}
	}
	
	private void startupConnections() {
		int port = Integer.parseInt(settings.getProperty(SETTINGS_SERVER_PORT, DEFAULT_SETTINGS_SERVER_PORT));
		
		try {
			logger.info("Starting the DS Server socket on port {}. Press CTRL+C to terminate", port);
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = null;
			while((socket = serverSocket.accept()) != null) {
				handleNewConnection(socket);
			}
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error starting the server", e);
		}
		
	}

	private void handleNewConnection(Socket socket) {
		if(socket == null) {
			return;
		}
		
		SocketAddress clientAddress = socket.getRemoteSocketAddress();
		logger.info("Got connection from {}", clientAddress);
		ClientConnectionHandler clientConnectionHandler = clientConnections.get(clientAddress.toString());
		if(clientConnectionHandler == null) {
			clientConnectionHandler = new ClientConnectionHandler(settings, instanceMetadata, socket, commandRegistry);
			clientConnections.put(clientAddress.toString(), clientConnectionHandler);
			Thread thread = new Thread(clientConnectionHandler);
			thread.start();
		}
		
	}

}
