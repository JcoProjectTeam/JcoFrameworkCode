package jco.ql.engine.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;


import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.json.JSONHandler;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.GetCollectionCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.GetCollection;

@Executor(GetCollectionCommand.class)
public class GetCollectionExecutor implements IExecutor<GetCollectionCommand>, JCOConstants {

	private DatabaseRegistry databaseRegistry;

	@Autowired
	public GetCollectionExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	@Override
	public void execute(Pipeline pipeline, GetCollectionCommand command) throws ExecuteProcessException {
		IDocumentCollection outCollection = null;
		
		if (command.getType() == GetCollection.DB_TYPE) {
			if (command.getDbName() != null) {
				IDatabase database = databaseRegistry.getDatabase(command.getDbName());
		        if (database != null) {
		            outCollection = database.getCollection(command.getCollectionName());
			        JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "]:\t connection to database: " + command.getDbName());
		        }
		        else 
					JMH.addExceptionMessage("[GET COLLECTION]: Invalid database " + command.getDbName());				
			} 
			// temporary collection
			else
				outCollection = pipeline.getCollection(command.getCollectionName());
		}
		// PF. Get Collection from URL
		else if (command.getType() == GetCollection.URL_APEX_TYPE || command.getType() == GetCollection.URL_QUOTED_TYPE) {
			DocumentDefinition newDoc = getDocumentFromWeb(command.getResourceStr(), null);
			outCollection = new SimpleDocumentCollection(FROMWEB_COLLECTION_NAME);
			outCollection.addDocument(newDoc);
	        JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "]:\t collection retrieved from WEB");
		}
		// PF. Get Collection from FILE
		else  {
			JSONHandler jh = new JSONHandler();
			try {
				outCollection = jh.readCollectionFromFile(FROMFILE_COLLECTION_NAME, command.getResourceStr());			
			    JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "]:\t collection retrieved from file");				
			} catch (IOException e) {
				JMH.addExceptionMessage("[GET COLLECTION] I/O error: " + e.getMessage());								
			}			
		}

		if (outCollection == null || outCollection.getDocumentList() == null) {
			JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t no documents loaded " + command.getCollectionName());
			outCollection = new SimpleDocumentCollection(command.getCollectionName());
		}
		else 
			JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents loaded");

		pipeline.addCollection(outCollection);
	}

	
	
    public static DocumentDefinition getDocumentFromWeb (String urlSt, DocumentDefinition sourceDoc) {
        int timeout = 5000;

        urlSt = urlSt.replace(" ", "%20");
        urlSt = urlSt.replace(">", "%3E");
        urlSt = urlSt.replace("<", "%3C");
        try {
            RequestConfig config = RequestConfig.custom()
            		.setConnectTimeout(timeout)
            		.setConnectionRequestTimeout(timeout)
            		.setSocketTimeout(timeout).build();
            CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            HttpGet request = new HttpGet(urlSt);

            // add request header
            String USER_AGENT = "Mozilla/5.0";
            request.addHeader("User-Agent", USER_AGENT);

            HttpResponse response = client.execute(request); // conn= response
            
            String strCurrentLine;
            StringBuffer outStBuf = new StringBuffer();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((strCurrentLine = rd.readLine()) != null) {
            	outStBuf.append(strCurrentLine+"\n");
            }
            
	    	rd.close();
	    	client.close();

        	SimpleDateFormat formatter = new SimpleDateFormat(JCOConstants.DATE_FORMAT_EXT);
        	Date date = new Date(System.currentTimeMillis());
        	String dateSt = formatter.format(date);
        	String prefix = "{  \"" + JCOConstants.DATA_FIELD_NAME + "\" : ";
        	outStBuf.insert(0, prefix);
            outStBuf.append(" }");

			Document bson = Document.parse(outStBuf.toString());
			DocumentDefinition newDoc = DocumentUtils.mapDocumentDefinitionFromBson(bson);
			newDoc.addField(new FieldDefinition(JCOConstants.TIMESTAMP_FIELD_NAME, new SimpleValue (dateSt)));
			newDoc.addField(new FieldDefinition(JCOConstants.URL_FIELD_NAME, new SimpleValue (urlSt.replace("\"", "\\\""))));
			if (sourceDoc != null)
				newDoc.addField(new FieldDefinition(JCOConstants.SOURCE_FIELD_NAME, new DocumentValue (sourceDoc)));

			return newDoc;
        } catch (Exception e) {
			JMH.addExceptionMessage("[GET COLLECTION FROM WEB] exception.\t Error in url:\t" + urlSt + "\n" + e.getMessage());
        }
	
        return null;
    }

    public static DocumentDefinition getDocumentFromFile (String filePathName, DocumentDefinition sourceDoc) {
/*
        try {
    	    if (filePathName == null || filePathName.isEmpty()) {
    	        throw new IllegalArgumentException("File path is null or empty");
    	    }
    	    // Normalize path separators (/ and \)
    	    Path path = Paths.get(filePathName.replace('\\', '/')).normalize();
    	    if (!Files.exists(path)) {
    	        throw new IOException("File not found: " + path);
    	    }
    	    if (!Files.isRegularFile(path)) {
    	        throw new IOException("Path does not refer to a regular file: " + path);
    	    }
    	    if (!Files.isReadable(path)) {
    	        throw new IOException("File is not readable: " + path);
    	    }

    	    // --- Text file heuristic check (initial sampling) ---
    	    int sampleSize = 64 * 1024;
    	    byte[] buffer = new byte[sampleSize];
    	    int read;

    	    try (InputStream probe = Files.newInputStream(path)) {
    	        read = probe.read(buffer);
    	    }

    	    if (read <= 0) {
    	        throw new IOException("File is empty");
    	    }

    	    int suspicious = 0;

    	    for (int i = 0; i < read; i++) {
    	        int b = buffer[i] & 0xFF;

    	        // NUL byte strongly indicates a binary file
    	        if (b == 0x00) {
    	            throw new IOException("File appears to be binary (NUL byte detected)");
    	        }

    	        // Allowed whitespace characters
    	        if (b == 9 || b == 10 || b == 13 || b == 12) {
    	            continue;
    	        }

    	        // Printable ASCII range
    	        if (b >= 32 && b <= 126) {
    	            continue;
    	        }

    	        // Possible UTF-8 multibyte sequence
    	        if (b >= 128) {
    	            continue;
    	        }

    	        suspicious++;
    	    }

    	    // If too many suspicious bytes, consider it non-text
    	    if ((double) suspicious / read > 0.05) {
    	        throw new IOException("File does not appear to be a text file");
    	    }


    	    // Final streaming reader (large buffer, constant memory usage)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(
                            Files.newInputStream(path),
                            StandardCharsets.UTF_8
                    ),
                    64 * 1024
            );
            

			Document bson = Document.parse(outStBuf.toString());
			DocumentDefinition newDoc = DocumentUtils.mapDocumentDefinitionFromBson(bson);
			newDoc.addField(new FieldDefinition(JCOConstants.TIMESTAMP_FIELD_NAME, new SimpleValue (dateSt)));
			newDoc.addField(new FieldDefinition(JCOConstants.URL_FIELD_NAME, new SimpleValue (filepathname.replace("\"", "\\\""))));
			if (sourceDoc != null)
				newDoc.addField(new FieldDefinition(JCOConstants.SOURCE_FIELD_NAME, new DocumentValue (sourceDoc)));

			return newDoc;
        } catch (Exception e) {
			JMH.addExceptionMessage("[GET COLLECTION FROM FILE] exception.\t Error in file:\t" + filepathname + "\n" + e.getMessage());
        }
	*/
        return null;
    }


}
