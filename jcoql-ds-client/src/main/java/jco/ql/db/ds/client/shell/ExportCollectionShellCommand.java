package jco.ql.db.ds.client.shell;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import jco.ql.db.ds.core.DocumentDefinitionUtils;
import jco.ql.db.ds.core.client.service.ClientConnectionManager;
import jco.ql.db.ds.core.message.IMessageData;
import jco.ql.db.ds.core.message.request.GetCollectionMessage;

@JCoShellCommand
public class ExportCollectionShellCommand {
	private static final Logger logger = LoggerFactory.getLogger(ExportCollectionShellCommand.class);
	
	private final ClientConnectionManager connectionManager;
	
	@Autowired
	public ExportCollectionShellCommand(ClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod(key = "export-collection", value = "Export the content of a collection in a JSON file")
	public String execute(@ShellOption(help = "Database name") String database, 
			@ShellOption(help = "Collection name") String collection, 
			@ShellOption(help = "Name of the output file") String toFile,
			@ShellOption(defaultValue = "-1", help = "Maximum number of documents to retrieve") Integer limit,
			@ShellOption(defaultValue = "0", help = "The first document to retrieve") Integer offset,
			@ShellOption(defaultValue = "false", help = "Wether to return the raw DocumentDefinition format or the JSON representation") Boolean toDocumentDefinition,
			@ShellOption(defaultValue = "false", help = "Wether to pretty print the output") Boolean pretty,
			@ShellOption(defaultValue = "500", help = "Number of document to export per batch") Integer documentsPerBatch) {
		String response = "";
		long startTime = System.currentTimeMillis();
		if(database == null || database.trim().isEmpty()) {
			response = "Please specify the database name";
		} else if(collection == null || collection.trim().isEmpty()) {
			response = "Please specify a name for the new collection";
		} else {
			try {
				GetCollectionMessage message = new GetCollectionMessage(database, collection, limit, offset);
				DataInputStream is = connectionManager.getInputStream();
				DataOutputStream os = connectionManager.getOutputStream();
				ObjectMapper jsonMapper = DocumentDefinitionUtils.getDocumentMapper();
				int documentCount = 0;

				logger.info("Sent message {}", message);
				IMessageData responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
				logger.info("Received response {}", responseMessage);
				
				if(!toFile.trim().endsWith(".json")) {
					toFile += ".json";
				}
				File outputFile = new File(toFile);
				if(!outputFile.exists()) {
					outputFile.createNewFile();
				}
				
				BufferedWriter writer = Files.newWriter(outputFile, StandardCharsets.UTF_8);
				writer.append("[");
				
				while(responseMessage != null) {
					Map<String, Object> responseBody = responseMessage.getBody();
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");
					if(documents != null) {
						Iterator<String> iterator = null;
						if(toDocumentDefinition) {
							if(pretty) {
								iterator = documents
									.stream()
									.map(DocumentDefinitionUtils::fromPlainJSON)
									.map(DocumentDefinitionUtils::prettyPrint)
									.iterator();
							} else {
								iterator = documents
									.stream()
									.map(DocumentDefinitionUtils::fromPlainJSON)
									.map(d -> {
										try {
											return jsonMapper.writeValueAsString(d);
										} catch (JsonProcessingException e) {
											logger.warn("Error converting to JSON :{}", e.getMessage());
										}
										return null;
										
									})
									.iterator();
							}
						} else {
							if(pretty) {
								iterator = documents.stream()
									.map(DocumentDefinitionUtils::prettyPrintJSON)
									.iterator();
							} else {
								iterator = documents.stream()
								.map(d -> {
									try {
										return jsonMapper.writeValueAsString(d);
									} catch (JsonProcessingException e) {
										logger.warn("Error converting to JSON :{}", e.getMessage());
									}
									return null;
									
								})
								.iterator();
							}
						}
						
						boolean complete = (Boolean) responseBody.get("complete");
						if(iterator != null) {
							while(iterator.hasNext()) {
								writer.append(iterator.next());
								if(iterator.hasNext() || !complete) {
									writer.append(",");
									writer.newLine();
								}
								documentCount++;
							}
						}
						
						if(Boolean.FALSE.equals(complete)) {
							int remaining = (Integer) responseBody.get("remaining");
							int partialOffset = (Integer) responseBody.get("partialOffset");
							message = new GetCollectionMessage(database, collection, remaining, partialOffset);
							logger.info("Retrieving next segment");
							responseMessage = connectionManager.sendAndReceiveMessage(message, os, is);
							logger.info("Received response {}", responseMessage);
						} else {
							break;
						}
					} else {
						response = "Error getting collection";
						break;
					}
				}
				writer.append("]");
				writer.flush();
				writer.close();
				
				is.close();
				os.close();
				connectionManager.disconnect();
				
				response = "Collection exported. Total: " + documentCount + " documents";
			} catch (IOException e) {
				logger.error("Error executing get collection", e);
			}
		}
		logger.info("Execution time: {} ms", (System.currentTimeMillis() - startTime));
		
		return response;
	}
}

