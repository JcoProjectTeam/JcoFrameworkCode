package jco.ql.db.ds.core.message;

import java.io.IOException;

public class SimpleMessage extends AbstractMessage<SimpleMessage> {

	public SimpleMessage(long commandCode) {
		super(commandCode);
	}

	@Override
	public IMessageData decodeMessageData(int paramsSize, int bodySize, byte[] data) throws IOException {
		return super.decodeMessageData(code, paramsSize, bodySize, data);
	}

	@Override
	public IMessageData decodeMessageData(byte[] data) throws IOException {
		return super.decodeMessageData(code, data);
	}

}
