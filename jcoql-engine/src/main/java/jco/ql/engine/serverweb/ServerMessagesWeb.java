package jco.ql.engine.serverweb;

import jco.ql.engine.server.ServerMessages;

public class ServerMessagesWeb extends ServerMessages{

	public ServerMessagesWeb() {
		super();
	}
// PF. ZUN CHECK ... move to ServerMessages	
	public String getMsgCollection(String title, String documents) {
		String prefix = "##BEGIN-COLLECTION##\n";
		String suffix = "\n##END-COLLECTION##";
		return prefix + title + "###" + documents + suffix;
	}
	
}
