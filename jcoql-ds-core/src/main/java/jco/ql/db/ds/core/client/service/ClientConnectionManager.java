package jco.ql.db.ds.core.client.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jco.ql.db.ds.core.message.IMessage;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.SimpleMessage;

public class ClientConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(ClientConnectionManager.class);

	private static final int HEADER_SIZE = 16;
	private static final int BUFFER_SIZE = 1024;
	
	private Socket socket;
	
	private boolean connected;

	private final String host;
	private final int port;
	
	public ClientConnectionManager(String host, int port) {
		this.host = host;
		this.port = port;
		
		socket = null;
		connected = false;
	}
	
	private void connect() throws IOException {
		if( socket == null || !socket.isConnected() || socket.isClosed()) {
			logger.debug("Connecting to server");
			socket = new Socket(host, port);
			logger.debug("Connected to server");
		}
		
		connected = true;
	}
	
	public void disconnect() throws IOException {
		if(socket != null && socket.isConnected()) {
			socket.close();
			connected = false;
		}
	}

	public DataInputStream getInputStream() throws IOException {
		connect();
		return new DataInputStream(socket.getInputStream());
	}

	public DataOutputStream getOutputStream() throws IOException {
		connect();
		return new DataOutputStream(socket.getOutputStream());
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public void sendMessage(IMessage message, OutputStream os) throws IOException {
		logger.debug("Sending message");
		message.sendMessage((IMessageData) message, os);
		os.flush();
		logger.debug("Message sent");
	}
	
	public IMessageData receiveMessage(InputStream is) throws IOException {
		try {
			byte[] buf = new byte[HEADER_SIZE];
			logger.debug("Receiving message");
			if(is.read(buf) == 16) {
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
				
				IMessage message = new SimpleMessage(commandCode);
				return message.decodeMessageData(paramsSize, bodySize, buf);
			}
			logger.debug("Message received");
		} catch (IOException | BufferUnderflowException e) {
			logger.error("Error in client connection handling: {}", e.getMessage());
			try {
				if(is != null) {
					is.close();
				}
			} catch (IOException e2) {}
		}
		
		return null;
	}
	
	public IMessageData receiveMessageOld(InputStream is) throws IOException {
		try {
			List<byte[]> buffers = new ArrayList<>();
			List<Integer> bufferSizes = new ArrayList<>();
			int total = 0;
			int read = 0;
			byte[] buf = new byte[BUFFER_SIZE];
			logger.info("Receiving message");
			while((read = is.read(buf)) > 0) {
				buffers.add(buf);
				bufferSizes.add(read);
				total += read;
				if(read < 1024) {
					break;
				}
				buf = new byte[BUFFER_SIZE];
			}
			logger.info("Message received");
			if(read >= 0) {
				ByteBuffer commandBuffer = ByteBuffer.allocateDirect(total);
				for(int i = 0; i < buffers.size(); i++) {
					Integer size = bufferSizes.get(i);
					commandBuffer.put(buffers.get(i), 0, size);
				}
				
				long commandCode = 0;
				commandBuffer.rewind();
				commandCode = commandBuffer.getLong();
//				commandCode = readLong(commandBuffer);
				buf = new byte[commandBuffer.capacity() - Long.BYTES];
				commandBuffer.get(buf);
				IMessage message = new SimpleMessage(commandCode);
				return message.decodeMessageData(buf);
			} else {
				is.close();
			}
		} catch (IOException | BufferUnderflowException e) {
			logger.error("Error in client connection handling: {}", e.getMessage());
			try {
				if(is != null) {
					is.close();
				}
			} catch (IOException e2) {}
		}
		
		return null;
	}
	
	public IMessageData sendAndReceiveMessage(IMessage message, OutputStream outputStream, InputStream inputStream) throws IOException {
		sendMessage(message, outputStream);
		return receiveMessage(inputStream);
	}
}
