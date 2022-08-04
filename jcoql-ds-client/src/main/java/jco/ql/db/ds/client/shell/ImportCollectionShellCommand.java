package jco.ql.db.ds.client.shell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import jco.ql.db.ds.core.DocumentDefinitionUtils;
import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.SaveCollectionMessage;

@JCoShellCommand
public class ImportCollectionShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(ImportCollectionShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public ImportCollectionShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "import-collection", value = "Import the content of a JSON file into a collection")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection, 
			@ShellOption(help = "Name of the input file") String fromFile,
			@ShellOption(help = "Appent do existing collection") Boolean append,
			@ShellOption(help = "Number of documents per batch", defaultValue = "1000") Integer documentsPerBatch) {
		String response = "";
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the new collection";
		} else if(fromFile == null || fromFile.trim().isEmpty()) {
			response = "Please specify the name of the file to import";
		} else {
			long startTime = System.currentTimeMillis();
			try {
				File inputFile = new File(fromFile);
				if(inputFile == null || !inputFile.exists()) {
					response = "Invalid input file";
				} else {
					FileInputStream fis = new FileInputStream(inputFile);
					ObjectMapper jsonMapper = DocumentDefinitionUtils.getDocumentMapper();
					List<Map<String, Object>> jsonDocuments = null;
					
					logger.info("Starting import of file {}", inputFile.getPath());

					int startChar = fis.read();
					fis.close();
					
					DataInputStream is = connectionManager.getInputStream();
					DataOutputStream os = connectionManager.getOutputStream();
					
					int documentsCount = 0;
					boolean firstBatch = true;
					if(startChar == '{') {
						jsonDocuments = new LinkedList<Map<String, Object>>();
						jsonDocuments.add(jsonMapper.readValue(inputFile, new TypeReference<Map<String, Object>>() {}));
						documentsCount += jsonDocuments.size();
					} else if(startChar == '[') {
						//Create a parser to read the file
						JsonParser parser = jsonMapper.getFactory().createParser(inputFile);
						ObjectReader jsonReader = jsonMapper.readerFor(new TypeReference<Map<String, Object>>() {});
						MappingIterator<Map<String, Object>> iterator = jsonReader.readValues(parser);
						//Skip the START_ARRAY token
						while(parser.nextToken() == JsonToken.START_ARRAY) { }
						
						jsonDocuments = new LinkedList<Map<String, Object>>();
						
						//Import documents in batches
						while(iterator.hasNext()) {
							jsonDocuments.add(iterator.next());
							
							documentsCount++;
							if(documentsCount % documentsPerBatch == 0) {
								if(sendDocuments(database, collection, !firstBatch || append, jsonDocuments, is, os)) {
									firstBatch = false;
									logger.info("Imported {} documents", documentsCount);
								} else {
									break;
								}
								
								jsonDocuments = new LinkedList<Map<String, Object>>();
							}
						}
						//jsonDocuments = jsonMapper.readValue(inputFile, new TypeReference<List<Map<String, Object>>>() {});
					}
					
					if(jsonDocuments != null) {
						if(jsonDocuments.size() > 0) {
							if(sendDocuments(database, collection, !firstBatch || append, jsonDocuments, is, os)) {
								response = "Imported " + documentsCount + " documents";
							} else {
								response = "Error importing collection";
							}
						}
					} else {
						response = "Invalid JSON in the input file";
					}
					
					is.close();
					os.flush();
					os.close();
					connectionManager.disconnect();
				}
				
				logger.info("Execution time: {}", System.currentTimeMillis() - startTime);
			} catch (IOException e) {
				logger.error("Error executing get collection", e);
			}
		}
		
		return response;
	}

	private boolean sendDocuments(String database, String collection, Boolean append, 
			List<Map<String, Object>> jsonDocuments, InputStream is, OutputStream os) throws IOException {
		boolean success = false;
		SaveCollectionMessage message = new SaveCollectionMessage(database, collection, jsonDocuments, append);
		logger.info("Sent message {}", message);
		IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
		logger.info("Received response {}", responseMessage);
		if(responseMessage != null) {
			Map<String, Object> responseBody = responseMessage.getBody();
			success = (boolean) responseBody.get("success");
		}
		return success;
	}
}

