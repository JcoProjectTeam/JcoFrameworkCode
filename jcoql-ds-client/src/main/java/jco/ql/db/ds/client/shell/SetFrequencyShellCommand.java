package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.SetFrequencyMessage;

/**
 * 21-08 ROSSONI ALBERTO
 */

@JCoShellCommand
public class SetFrequencyShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(SetFrequencyShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public SetFrequencyShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "set-frequency", value = "Set the frequency of update of a dynamic collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection,
			@ShellOption(help = "Index of the url which frequency must be set") Integer index,
			@ShellOption(help = "Frequency of update in M for minute, H for hours, D for days and W for weeks (example: 5D for 5 days)") String frequency) {
		String response = "";
		Integer convertedFrequency = ConvertFrequency(frequency);

		if(database == null || database.trim().isEmpty()) 
			response = "Please specify the database name";

		else if(collection == null || collection.trim().isEmpty()) 
			response = "Please specify a name for the collection";

		else if(index == null || index < 0) 
			response = "Please insert a valid index for the url";
		
		else if(frequency == null || convertedFrequency == 0 || convertedFrequency < 1000)
			response = "Please set a valid frequency ";
		
		else {
			try {
				SetFrequencyMessage message = new SetFrequencyMessage(database, collection, index, convertedFrequency);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				if(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					if(Boolean.TRUE.equals(responseBody.get("success"))) 
						response = "Frequency of url number " + index + " set correctly on collection  " + collection + " in database " + database;
					else 
						response = "Error setting the new frequency: " + responseBody.get("errorMessage");
				}
				
				is.close();
				os.close();
				connectionManager.disconnect();
			} catch (IOException e) {
				logger.error("Error executing setFrequency", e);
			}
		}
		
		return response;
	}
	
	
	public int ConvertFrequency(String frequency) {
		String category = frequency.substring(frequency.length()-1); 
		String number = frequency.substring(0,frequency.length()-1);
		int finalNumber = Integer.parseInt(number);

		if ("M".equalsIgnoreCase(category))
			return finalNumber*60*1000;        		//every minute is equal to 60000 milliseconds
		else if ("H".equalsIgnoreCase(category))
			return finalNumber*60*60*1000;        	//every hour is equal to 3600000 milliseconds
		else if ("D".equalsIgnoreCase(category))
			return finalNumber*24*60*60*1000;      	//every day is equal to 86400000 milliseconds
		else if ("W".equalsIgnoreCase(category))
			return finalNumber*7*24*60*60*1000;     //every week is equal to 604800000 milliseconds
		
		return finalNumber;
		
	}
}



