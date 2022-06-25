package jco.ql.db.ds.server.command;

import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;

public abstract class AbstractCommand implements ICommand {
	
	protected final long commandCode;
	
	public AbstractCommand(long commandCode) {
		this.commandCode = commandCode;
	}

	@Override
	public long getCode() {
		return commandCode;
	}

	@Override
	public IMessageData execute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		return doExecute(serverSettings, instanceMetadata, request);
	}
	
	abstract protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request);
	
}
