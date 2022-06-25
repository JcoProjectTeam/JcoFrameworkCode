package jco.ql.db.ds.server.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jco.ql.db.ds.core.message.IMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.SimpleMessage;
import jco.ql.db.ds.core.message.response.ErrorResponseMessage;
import jco.ql.db.ds.server.command.CommandRegistry;
import jco.ql.db.ds.server.command.ICommand;

public class ClientConnectionHandler implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);

	private static final int BUFFER_SIZE = 1024;
	private static final int HEADER_SIZE = 16;
	
	private final Socket socket;
	private final Properties serverSettings;
	private final Properties instanceMetadata;
	private final CommandRegistry commandRegistry;
	
	private boolean run;
	private DataInputStream is;
	private DataOutputStream os;

	public ClientConnectionHandler(Properties serverSettings, Properties instanceMetadata, Socket clientSocket, 
			CommandRegistry commandRegistry) {
		
		this.socket = clientSocket;
		this.serverSettings = serverSettings;
		this.instanceMetadata = instanceMetadata;
		
		this.commandRegistry = commandRegistry;
		
		this.run = true;
	}
	
	public void stop() {
		this.run = false;
	}
	
	public boolean isRunning() {
		return this.run;
	}

	@Override
	public void run() {
		this.run = true;
		while(this.run) {
			try {
				checkConnection();
				if(is == null || os == null) {
					stop();
					return;
				}
				try {
					byte[] buf = new byte[HEADER_SIZE];
					
					if(is.read(buf) == 16) {
						logger.info("Message received");
						ByteBuffer headerBuffer = ByteBuffer.wrap(buf);
						long commandCode = headerBuffer.getLong();
						int paramsSize = headerBuffer.getInt();
						int bodySize = headerBuffer.getInt();
						
						int totalSize = paramsSize + bodySize;
						if(totalSize > 0) {
							ByteBuffer commandBuffer = ByteBuffer.allocateDirect(totalSize);
							buf = new byte[BUFFER_SIZE];
							int read = 0;
							int readTotal = 0;
							while((read = is.read(buf)) > 0) {
								commandBuffer.put(buf, 0, read);
								buf = new byte[BUFFER_SIZE];
								readTotal += read;
								if(readTotal >= totalSize) {
									break;
								}
							}
							commandBuffer.rewind();
							buf = new byte[totalSize];
							commandBuffer.get(buf);
							commandBuffer = null;
						}
						handleCommand(commandCode, paramsSize, bodySize, buf, os);
					} else {
						is.close();
						is = null;
						os.close();
						os = null;
						socket.close();
					}
				} catch (IOException | BufferUnderflowException e) {
					logger.error("Error in client connection handling: {}", e.getMessage());
					try {
						if(is != null) {
							is.close();
						}
					} catch (IOException e2) {}

					try {
						if(os != null) {
							os.flush();
							os.close();
						}
					} catch (IOException e2) {}
					
					try {
						socket.close();
					} catch (Exception e1) {}
					
					stop();
				}
			} catch (IOException e) {
				logger.error("Error in client connection handling: {}", e.getMessage());
				this.run = false;
			}
		}
	}

	private void checkConnection() throws IOException {
		if(socket != null && !socket.isClosed() && socket.isConnected()) {
			if(os == null || socket.isOutputShutdown()) {
				os = new DataOutputStream(socket.getOutputStream());
			}
			
			if(is == null || socket.isInputShutdown()) {
				is = new DataInputStream(socket.getInputStream());
			}
		} else {
			stop();
		}
		
	}

	private void handleCommand(long commandCode, int paramsSize, int bodySize, byte[] commandData, OutputStream os) throws IOException {
		logger.info("Received command {} with data of length {} bytes", String.format("%08x", commandCode), commandData.length);
		
		IMessage message = new SimpleMessage(commandCode);
		ICommand command = commandRegistry.getCommand(commandCode);
		if(message != null && command != null) {
			try {
				logger.info("****************************************************************************************");
				IMessageData request = message.decodeMessageData(paramsSize, bodySize, commandData);
				IMessageData response = command.execute(serverSettings, instanceMetadata, request);
				if(response != null) {
					IMessage responseMessage = new SimpleMessage(response.getCode());
					responseMessage.sendMessage(response, os);
					logger.info("Sent response");
				}
			} catch (IOException e) {
				logger.error("Error while handling command", e);
				throw e;
			}
		} else {
			ErrorResponseMessage errorResponseMessage = new ErrorResponseMessage("Invalid message code");
			errorResponseMessage.sendMessage(errorResponseMessage, os);
			logger.warn("Sent error response");
		}
		os.flush();
	}

}
