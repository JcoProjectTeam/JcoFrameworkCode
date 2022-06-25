package jco.ql.db.ds.core.message;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class MessageHandler {
	
	private final ObjectMapper commandMapper;
	
	public MessageHandler(long commandCode) {
		this.commandMapper = new ObjectMapper();
	}

	public <T> void sendMessage(T message, OutputStream os) throws IOException {
		byte[] messageData = null;
		if(message != null) {
			messageData = commandMapper.writeValueAsBytes(message);
		}
		
		if(messageData != null) {
			os.write(messageData);
		}
	}
	
	public <Res> Res decodeMessage(byte[] data, Class<Res> dataType) throws IOException {
		return commandMapper.readValue(data, dataType);
	}

}
