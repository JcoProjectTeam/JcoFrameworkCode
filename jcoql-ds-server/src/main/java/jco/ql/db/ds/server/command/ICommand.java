package jco.ql.db.ds.server.command;

import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;

public interface ICommand {
	
	long getCode();

	IMessageData execute(final Properties serverSettings, final Properties instanceMetadata, IMessageData request);
	
}
