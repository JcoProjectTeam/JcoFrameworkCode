package jco.ql.db.ds.server.command;

import java.util.Properties;

import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.MessageCodes;
import jco.ql.db.ds.core.message.response.PingResponseMessage;
import jco.ql.db.ds.server.annotation.JcoDsCommand;

@JcoDsCommand
public class PingCommand extends AbstractCommand {
	
	public PingCommand() {
		super(MessageCodes.PING);
	}

	@Override
	protected IMessageData doExecute(Properties serverSettings, Properties instanceMetadata, IMessageData request) {
		return new PingResponseMessage();
	}

}
