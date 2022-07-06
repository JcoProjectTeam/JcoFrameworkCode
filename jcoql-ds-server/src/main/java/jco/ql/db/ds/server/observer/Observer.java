package jco.ql.db.ds.server.observer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jco.ql.db.ds.core.DSConstants;
import jco.ql.db.ds.core.DocumentDefinitionUtils;
import jco.ql.model.engine.JCOConstants;


/**
 * 21-08 ROSSONI ALBERTO
 */
public class Observer extends TimerTask implements DSConstants {
	
	
	private String url;
	private Integer type;
	private String databaseTarget;
	private String collectionTarget;
	private StringBuffer outSt = new StringBuffer();
	private StringBuffer lastOutSt = new StringBuffer();
	private Integer index;
	private File dataDirectory;
	
	private boolean running = false;
	
	LinkedList<Map<String, Object>> jsonDocuments = new LinkedList<Map<String, Object>>();
	ObjectMapper jsonMapper = DocumentDefinitionUtils.getDocumentMapper();
	
	String content;
	
	
	private final Properties settings;
	
	
	public Observer(String database, String collectionName, String url, Integer index) {
		this.databaseTarget = database;
		this.collectionTarget = collectionName;
		this.url = url;
		this.type = 0;
		this.index = index;
		
		this.settings = new Properties();
        String dataPath = settings.getProperty(SETTINGS_SERVER_DATA_PATH, "data");
		
		dataDirectory = new File(dataPath);
	}
	
	
	
	public void run() {
	    File collectionFile = getCollectionFile(databaseTarget, collectionTarget, true);
    	try {
    		//get the resource from an url ( "{}" if url is not valid)
    		outSt.replace(0, outSt.length(), getCollectionFromWeb(url));
			content = (Files.readAllLines(collectionFile.toPath()).toString());
			
			//verify is the new content is the same of the last update
			if((lastOutSt != outSt) && (outSt != null)) 
				//write the content into the collection
				if (type == 0) {
	    		    jsonDocuments.add(jsonMapper.readValue(outSt.toString(), new TypeReference<Map<String, Object>>() {}));
	    		    saveCollection(databaseTarget,collectionTarget,jsonDocuments,true);					
				}
				else if (type == 1) {
	    		    jsonDocuments.add(jsonMapper.readValue(outSt.toString(), new TypeReference<Map<String, Object>>() {}));
	    		    saveCollection(databaseTarget,collectionTarget,jsonDocuments,false);					
				}
	    		lastOutSt = outSt;
	    		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
		
	
	//check if the thread is scheduled	
	public boolean isRunning() {
		return running;
	}
	
	
	//change update type
	public void setUpdateType(Integer type) {
		this.type = type;
	}
	
	
	//Write the content of documents into collection
     private boolean saveCollection(String database, String collection, List<Map<String, Object>> documents, boolean append) {
		boolean success = false;
		File collectionFile = getCollectionFile(database, collection, true);
		File indexFile = getCollectionIndexFile(database, collection, true);
		if(collectionFile != null) {
			try {
				final FileOutputStream collectionFileStream = new FileOutputStream(collectionFile, append);
				final FileOutputStream indexFileStream = new FileOutputStream(indexFile, append);
				final byte[] lineSeparator = "\n".getBytes();
				int sepLen = lineSeparator.length;
				long objOffset = 0;
				if(append) {
					FileInputStream fis = new FileInputStream(indexFile);
					long lastDoc = indexFile.length() - 12;
					if(lastDoc > 0) {
						fis.skip(lastDoc);
					}
					byte[] offsetBytes = new byte[8];
					byte[] sizeBytes = new byte[4];
					fis.read(offsetBytes);
					fis.read(sizeBytes);
					fis.close();
					objOffset = bytesToLong(offsetBytes) + bytesToInt(sizeBytes);
				}
				for(Map<String, Object> d : documents) {
					try {
						byte[] objBytes = jsonMapper.writeValueAsBytes(d);
						int objLen = objBytes.length;
						
						//Write the document to the collection file
						collectionFileStream.write(objBytes);
						collectionFileStream.write(lineSeparator);
						
						//Write the index file
						writeIndex(indexFileStream, objOffset, objLen + sepLen);
						objOffset += objLen + sepLen;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				collectionFileStream.flush();
				collectionFileStream.close();
				indexFileStream.flush();
				indexFileStream.close();
				success = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
    }
		
	
	 private static String getCollectionFromWeb (String urlSt) {
        int timeout = 5000;
        String outSt = null;

        urlSt = urlSt.replace(" ", "%20");
        urlSt = urlSt.replace(">", "%3E");
        urlSt = urlSt.replace("<", "%3C");
        
        RequestConfig config = RequestConfig.custom()
        		.setConnectTimeout(timeout)
        		.setConnectionRequestTimeout(timeout)
        		.setSocketTimeout(timeout).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpGet request = new HttpGet(urlSt);

        // add request header
        String USER_AGENT = "Mozilla/5.0";
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response;
		try {
			response = client.execute(request);
			 
            String strCurrentLine;
            StringBuffer outStBuf = new StringBuffer();
            // reading from url
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((strCurrentLine = rd.readLine()) != null) 
            	outStBuf.append(strCurrentLine+"\n");

        	SimpleDateFormat formatter= new SimpleDateFormat(JCOConstants.DATE_FORMAT_EXT);
        	Date date = new Date(System.currentTimeMillis());
        	String dateSt = formatter.format(date);
        	String prefix = "{ \"" + JCOConstants.TIMESTAMP_FIELD_NAME + "\" : \"" + dateSt + "\", " +
        					" \"" + JCOConstants.URL_FIELD_NAME + "\" : \"" + urlSt.replace("\"", "\\\"") + "\", " +
        					" \"" + JCOConstants.DATA_FIELD_NAME + "\" : ";
        	outStBuf.insert(0, prefix);
            outStBuf.append(" }");
            
            outSt = outStBuf.toString().trim();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // conn= response
       
    	
		if(outSt == null)
        	outSt = "{}";

		return outSt;
    }

	 
	 private File getCollectionFile(String database, String collectionName, boolean create) {
		File collectionDir = getCollectionDir(database, collectionName, create);
	
		if(collectionDir == null || !collectionDir.exists() || !collectionDir.isDirectory()) 
			collectionDir.mkdirs();

		File collectionFile = Paths.get(collectionDir.getAbsolutePath(), "collection" + index + ".data").toFile();
		if(!collectionFile.exists() &&  create) {
			try {
				collectionFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return collectionFile;
	}

	 
	 private File getCollectionIndexFile(String database, String collectionName, boolean create) {
		File collectionDir = getCollectionDir(database, collectionName, create);
		if(collectionDir == null || !collectionDir.exists() || !collectionDir.isDirectory()) {
			collectionDir.mkdirs();
		}
		File collectionFile = Paths.get(collectionDir.getAbsolutePath(), "collection" + index + ".idx").toFile();
		if(!collectionFile.exists() &&  create) {
			try {
				collectionFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return collectionFile;
	}
	 
	 
	 private void writeIndex(FileOutputStream indexFileStream, long objOffset, int objSize) throws IOException {
		 byte[] longToBytes = longToBytes(objOffset);
		 indexFileStream.write(longToBytes);
		 byte[] intToBytes = intToBytes(objSize);
		 indexFileStream.write(intToBytes);
	 }
	 
	 
	 private File getCollectionDir(String database, String collectionName, boolean create) {
		 return Paths.get(getDatabaseDirectory(database, create).getAbsolutePath(), collectionName).toFile();
	 }
	 
	 
	 private File getDatabaseDirectory(String database, boolean create) {
		 File databaseDir = Paths.get(this.dataDirectory.getAbsolutePath(), database).toFile();
		 return databaseDir;
	 }


	 private long bytesToLong(byte[] bytes) {
		 return ((bytes[0] & 0xFF) << 56)	| 
				 ((bytes[1] & 0xFF) << 48)	| 
				 ((bytes[2] & 0xFF) << 40)	| 
				 ((bytes[3] & 0xFF) << 32)	| 
				 ((bytes[4] & 0xFF) << 24)	| 
				 ((bytes[5] & 0xFF) << 16)	| 
				 ((bytes[6] & 0xFF) << 8 )	| 
				 ((bytes[7] & 0xFF) << 0 );
	 }

		
	private int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24)	| 
				((bytes[1] & 0xFF) << 16)	| 
				((bytes[2] & 0xFF) << 8 )	| 
				((bytes[3] & 0xFF) << 0 );
	}
		
		
	private byte[] longToBytes(long objOffset) {
		return new byte[] {
				(byte) (objOffset >> 56),
				(byte) (objOffset >> 48),
				(byte) (objOffset >> 40),
				(byte) (objOffset >> 32),
				(byte) (objOffset >> 24),
				(byte) (objOffset >> 16),
				(byte) (objOffset >> 8),
				(byte) (objOffset)
		};
	}


	private byte[] intToBytes(long objOffset) {
		return new byte[] {
				(byte) ((objOffset >> 24) & 0xFF),
				(byte) ((objOffset >> 16) & 0xFF),
				(byte) ((objOffset >> 8) & 0xFF),
				(byte) ((objOffset) & 0xFF)
		};
	}

}
