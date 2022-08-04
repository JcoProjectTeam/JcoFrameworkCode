package jco.ql.db.ds.core.message;

import java.io.IOException;
import java.io.OutputStream;

public interface IMessage {
	
	long getCode();
	
	IMessageData getMessageData();

	void sendMessage(IMessageData message, OutputStream os) throws IOException;
	
	IMessageData decodeMessageData(byte[] data) throws IOException;

	IMessageData decodeMessageData(int paramsSize, int bodySize, byte[] data) throws IOException;

}
