package jco.ql.db.ds.server.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;


import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;

import jco.ql.db.ds.core.DSConstants;
import jco.ql.db.ds.core.datatype.CollectionWrapper;
import jco.ql.db.ds.core.datatype.json.GeoJsonValueSerializer;
import jco.ql.db.ds.core.datatype.json.JcoValueDeserializer;
import jco.ql.db.ds.server.Server;
import jco.ql.db.ds.server.collectionsDescriptor.CollectionsDescriptorManager;
import jco.ql.db.ds.server.collectionsDescriptor.Url;
import jco.ql.db.ds.server.collectionsDescriptor.collectionDescriptor;
import jco.ql.db.ds.server.observer.Observer;
import jco.ql.db.ds.server.service.DataSourceService;
import jco.ql.db.ds.server.util.DirectoryFileFilter;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;

/**
 * 21-08 ROSSONI ALBERTO
 * Added new methods for functions createVirtualCollection, createDynamicCollection, addUrl, removeUrl, listUrl, setFrequency, setUpdateType and stopUpdate.
 * Added new private methods to support the new scope of database.metadata as descriptor.
 * Added new private methods to support the new Observer thread for dynamic collections.
 */

@Service
@Profile("server")
public class DataSourceServiceImpl implements DataSourceService, DSConstants {
	
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	private final Properties settings;
	private final Properties instanceMetadata;
	private final ObjectMapper jsonMapper;
	
	private File dataDirectory;

	
	@Autowired
	public DataSourceServiceImpl() {
		this.settings = new Properties();
		this.instanceMetadata = new Properties();
		
		jsonMapper = new ObjectMapper();
	}
	
	
	@PostConstruct
	protected void init() {
		loadSettings();
		startupDatabase();
		initSerializer();
	}

	
	private void loadSettings() {
		try {
			InputStream fis = new FileInputStream(Paths.get(SETTINGS_CONFIG_PATH, SETTINGS_CONFIG_FILE).toFile());
			if(fis != null) 
				settings.load(fis);
		} catch (IOException e) {
			logger.error("Error loading settings from the instance.metadata file", e);
		}
		
	}
	
	
	private void startupDatabase() {
		String dataPath = settings.getProperty(SETTINGS_SERVER_DATA_PATH, "data");
		
		File dataDirectory = new File(dataPath);
		//Checking data directory existence or create it
		if(!dataDirectory.exists() || !dataDirectory.isDirectory()) {
			logger.info("Data directory not found. Creating at path: {}", dataDirectory);
			if(dataDirectory.mkdirs()) {
				logger.info("Data directory successfully created");
			} else {
				logger.info("Data directory not created");
				throw new RuntimeException("Impossible to create data directory");
			}
		}
		this.dataDirectory = dataDirectory;

		getInstanceMetadata(dataPath);
	}
	
	
	private void initSerializer() {
		SimpleModule valueModule = new SimpleModule();
		valueModule.addSerializer(GeometryValue.class, new GeoJsonValueSerializer());
		valueModule.addDeserializer(JCOValue.class, new JcoValueDeserializer());
		jsonMapper.registerModule(valueModule);
	}

	
	private void getInstanceMetadata(String dataPath) {
		File metadataFile = Paths.get(dataPath, INSTANCE_METADATA_FILE).toFile();
		try {
			if(!metadataFile.exists()) {
				metadataFile.createNewFile();
			} else {
				instanceMetadata.load(new FileInputStream(metadataFile));
			}
			initDefaultMetadata(instanceMetadata);
		} catch (FileNotFoundException e) {
			logger.error("Instance metadata file not found");
			throw new RuntimeException("Missing Instance metadata file");
		} catch (IOException e) {
			logger.error("Impossible to read instance metadata file");
			throw new RuntimeException("Impossible to read instance metadata file");
		}
	}

	
	private void initDefaultMetadata(Properties metadata) {
		metadata.putIfAbsent("storage.format", "JSON");
	}

	
	public Properties getServerSettings() {
		return settings;
	}

	
	public Properties getInstanceMetadata() {
		return instanceMetadata;
	}

	
	/**
	 * Create a new database
	 */
	@Override
	public boolean createDatabase(String name) {
		boolean success = false;
		if(name != null && !name.trim().isEmpty()) {
			File databaseDir = getDatabaseDirectory(name, true);
			try {
				initializeMetadata(name);     //21-08 ROSSONI ALBERTO Setup the descriptor with the name of the database
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if(databaseDir != null && databaseMetadataExists(databaseDir)) {
				success = true;
			}
		}
		return success;
	}

	
	/**
	 * Delete a database
	 */
	@Override
	public boolean deleteDatabase(String name) {
		boolean success = false;
		if(name != null && !name.trim().isEmpty()) {
			File databaseDir = getDatabaseDirectory(name, true);
			
			//21-08 ROSSONI ALBERTO
			//Check if there are dynamic collection whose Observers are still running
			CollectionsDescriptorManager metadata = GetMetadata(name);
			for(collectionDescriptor collection:metadata.getAllCollections()) {     //for every collection inside the database
				if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE)) {
					for (int index = 0; index < collection.getUrl().size(); index++) {
						Url url = collection.getUrl().get(index);                               //for every url inside a dynamic collection
						Observer observer = new Observer(name, collection.getName(), url.getUrl(), index);
						if(observer.isRunning())
							observer.cancel();
					}
				}
			}
			
			if(databaseDir != null && databaseDir.isDirectory()) 
				success = deleteDirectory(databaseDir);
		}
		return success;
	}

	
	/**
	 * Delete a directory
	 * @param databaseDir
	 * @return
	 */
	private boolean deleteDirectory(File databaseDir) {
		return deleteDirectoryContent(databaseDir) && databaseDir.delete();
	}

	
	/**
	 * Delete the content of a directory
	 * @param databaseDir
	 * @return
	 */
	private boolean deleteDirectoryContent(File databaseDir) {
		File[] files = databaseDir.listFiles();
		if(files != null) {
			for(File file : files) {
				if(file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
			return true;
		}
		return false;
	}

	
	/**
	 * List all the available databases
	 */
	@Override
	public List<String> listDatabases() {
		List<String> databases = new LinkedList<>();
		
		File[] databaseDirs = this.dataDirectory.listFiles(new DirectoryFileFilter());
		if(databaseDirs != null && databaseDirs.length > 0) {
			for(File databaseDir : databaseDirs) {
				if(databaseMetadataExists(databaseDir)) {
					databases.add(databaseDir.getName());
				}
			}
		}
		return databases;
	}
	
	
	/**
	 * Check that the database metadata file exists
	 * @param databaseDir
	 * @return
	 */
	private boolean databaseMetadataExists(File databaseDir) {
		return getDatabaseMetadataFile(databaseDir).exists();
	}

	
	/**
	 * Get the database metadata file
	 * @param databaseDir
	 * @return
	 */
	private File getDatabaseMetadataFile(File databaseDir) {
		return Paths.get(databaseDir.getAbsolutePath(), DATABASE_METADATA_FILE).toFile();
	}
	
	
	/**
	 * Return a file pointing to the directory of a database
	 * @param database
	 * @param create
	 * @return
	 */
	private File getDatabaseDirectory(String database, boolean create) {
		File databaseDir = Paths.get(this.dataDirectory.getAbsolutePath(), database).toFile();
		if((databaseDir == null || !databaseDir.exists() || !databaseDir.isDirectory()) && create) {
			if(databaseDir.mkdirs()) {
				File metadataFile = getDatabaseMetadataFile(databaseDir);
				if(!metadataFile.exists()) {
					try {
						metadataFile.createNewFile();
					} catch (IOException e) {
						logger.error("Impossible to create database metadata file", e);
					}
				}
			}
		}
		return databaseDir;
	}

	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * List all the collections inside a database with their type according to database.metadata file
	 */
	@Override
	public List<String> listCollections(String database) {	
		checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		
		List<String> collections = new LinkedList<>();
		for(collectionDescriptor k: metadata.collections) {
			if(k != null)
				collections.add(k.getName() + " " + k.getType() + "\n");
		}
		return collections;
	}

	
	/**
	 * Check the presence of the collection file inside the database directory
	 * @param databaseDir
	 * @return
	 */
// PF. 2021.10.20 sembrano inutili
//	private boolean collectionFileExists(File databaseDir) {
//		return Paths.get(databaseDir.getPath(), COLLECTION_FILE).toFile().exists();
//	}

	/**
	 * Check the presence of the collection index file inside the database directory
	 * @param databaseDir
	 * @return
	 */
// PF. 2021.10.20 sembrano inutili
//	private boolean collectionIndexFileExists(File databaseDir) {
//		return Paths.get(databaseDir.getPath(), COLLECTION_INDEX_FILE).toFile().exists();
//	}
	
	/**
	 * Create a new Static collection in an existing database
	 * 21-08 ROSSONI ALBERTO
	 * Modified method to analyze the database.metadata file and don't overwrite on virtual collections
	 */
	@Override
	public boolean createCollection(String database, String collectionName) {
		checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		if(metadata.getCollection(collectionName) == null) 
			return createNewCollection (database, collectionName);
		else {
			if(metadata.getCollection(collectionName).getType().equals(VIRTUAL_COLLECTION_TYPE)) {
				System.out.println("Collection " + collectionName + " already exist and its virtual");
				return false;				
			}
			else {
				deleteCollection(database, collectionName);
				File collectionFile = getCollectionFile(database, collectionName, true);
				File collectionIndexFile = getCollectionIndexFile(database, collectionName, true);
				RemoveCollectionInfo(database,collectionName);
				AddCollectionInfo(database,collectionName,null, false);
				return collectionFile != null && collectionFile.exists()
						&& collectionIndexFile != null && collectionIndexFile.exists();
			}
		}
	}
	

	// PF. Added on 17.03.2022
	boolean createNewCollection (String database, String collectionName) {
		File collectionFile = getCollectionFile(database, collectionName, true);
		File collectionIndexFile = getCollectionIndexFile(database, collectionName, true);
		AddCollectionInfo(database,collectionName,null, false);
		return collectionFile != null && collectionFile.exists()
				&& collectionIndexFile != null && collectionIndexFile.exists();
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Create a new Virtual collection in an existing database
	 */	
	public boolean createVirtualCollection(String database, String collectionName ,List<Url> url) {
        checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		if(metadata.getCollection(collectionName) != null) {
			if(metadata.getCollection(collectionName).getType().equals(VIRTUAL_COLLECTION_TYPE)) {
				System.out.println("Collection " + collectionName + " already exist and its virtual");
				return false;
			}
			else {
				deleteCollection(database, collectionName);
				AddCollectionInfo(database,collectionName,url,false);
				return true;
			}
		}
		else {
			AddCollectionInfo(database,collectionName,url,false);
			return true;
		}
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Create a new Dynamic collection in an existing database
	 */
	public boolean createDynamicCollection(String database,String collectionName, List<Url>url) {
        checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		if(metadata.getCollection(collectionName) != null) {
			if (metadata.getCollection(collectionName).getType().equals(VIRTUAL_COLLECTION_TYPE) ||
					metadata.getCollection(collectionName).getType().equals(DYNAMIC_COLLECTION_TYPE)) {
				System.out.println("Collection " + collectionName + " already exist and can't be overwritten");
				return false;
			}
			else {
				deleteCollection(database, collectionName);
				// PF. 2021.10.20 sembrano inutili
//				File collectionDir = getCollectionDir(database, collectionName, true);
				RemoveCollectionInfo(database, collectionName);
				AddCollectionInfo(database,collectionName, url, true);
				return true;
			}
		}
		else {
			// PF. 2021.10.20 sembrano inutili
//			File collectionDir = getCollectionDir(database, collectionName, true);
			AddCollectionInfo(database, collectionName, url, true);
			return true;
		}
	}
	
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Add an url to a collection descriptor (not for static)
	 */
	public boolean addUrl(String database,String collectionName,List<Url> url) {
		boolean success = false;
        checkMetadata(database);
        logger.error(collectionName);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		
		if(collection != null) {
			if(collection.getType().equals(STATIC_COLLECTION_TYPE))
				logger.error("Can't add url to a static collection");
			else {
				for(Url k:url) {
					if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE)) {
						k.setFrequency(DEFAULT_FREQUENCY);
						k.setUpdateType(DEFAULT_UPDATE_TYPE);
						metadata.getCollection(collectionName).addUrl(k);
						WriteMetadata (database, metadata);
						createObserver(database, collectionName, metadata.getCollection(collectionName).getUrl().size() - 1);
					}
					else {
						metadata.getCollection(collectionName).addUrl(k);
						WriteMetadata (database, metadata);
					}
				}
				success = true;
			}
		}
		return success;
	}

	
	/**
	 * 	21-08 ROSSONI ALBERTO
	 * Remove an url from a virtual or dynamic collection
	 */
	public boolean removeUrl(String database,String collectionName, Integer index) {
		boolean success = false;
        checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		if(collection != null) {
			if(collection.getType().equals(STATIC_COLLECTION_TYPE))
				logger.error("Can't remove url from a static collection");
			else {
				if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE))
					deleteObserver(database,collectionName,index);
				
				metadata.getCollection(collectionName).removeUrl(index);
				if(WriteMetadata (database, metadata))
					success = true;
			}
		}
		return success;
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Get the list of all the url from virtual and dynamic collection
	 */
	public List<String> listUrl(String database,String collectionName) {
		List<String> url;
        checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		
		if(collection != null) {
			if(collection.getType().equals(STATIC_COLLECTION_TYPE)) {
				logger.error("No url avaiable from static collection");
				return null;
			}
			else {
				url = new LinkedList<>();
				List<Url> list = collection.getUrl();
				int position = 0;
				for (Url k: list) {
					if(k != null) 
						url.add(position + " - " + k.getUrl() + "\n");
					position++;
				}
				return url;	
			}
		}		
		return null;
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Set the frequency of update on a specific url of a dynamic collection
	 */	
	public boolean setFrequency(String database, String collectionName, Integer index , Integer frequency)
	{
		logger.error("database " + database);
		logger.error("collecttion " + collectionName);
		logger.error("index " + index);
		logger.error("frequency " + frequency);
        checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		boolean success = false;
		
		if(collection != null) {
			if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE)) {
				metadata.getCollection(collectionName).getUrl().get(index).setFrequency(frequency);
				deleteObserver(database,collectionName,index);
				createObserver(database,collectionName,index);
				WriteMetadata (database, metadata);
				success = true;
			}
		}
		return success;
	}

	
	/**
	 * 21-08 ROSSONI ALBERTO 
	 * Set the type of update on a specific url of a dynamic collection
	 */
	public boolean setUpdateType(String database, String collectionName, Integer index, Integer type) {
	    checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		boolean success = false;
		if(collection != null) {
			if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE)) {
				metadata.getCollection(collectionName).getUrl().get(index).setUpdateType(type);
				deleteObserver(database,collectionName,index);
				createObserver(database,collectionName,index);
				WriteMetadata (database, metadata);
				success = true;
			}
		}
		return success;
	}

	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Stop a dynamic collection from updating ever again
	 */
	public boolean stopUpdate(String database,String collectionName) {
		checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		boolean success = false;
		
		if(collection != null) {
			if(collection.getType().equals(DYNAMIC_COLLECTION_TYPE)) {
				for (int index = 0; index < collection.getUrl().size(); index++) {
					Url url = collection.getUrl().get(index);
					if(url != null)
						deleteObserver(database, collectionName, index);
				}
				success = true;
			}
		}
		return success;
	}
	
	
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Delete a collection and remove its description from database.metadata file
	 */
	@Override
	public boolean deleteCollection(String database, String collectionName) {
		File collectionDir = getCollectionDir(database, collectionName, false);
		
		boolean success = false;
        checkMetadata(database);
		
		CollectionsDescriptorManager metadata = GetMetadata(database);
		collectionDescriptor collection = metadata.getCollection(collectionName);
		
		if(collection.getType().equals(STATIC_COLLECTION_TYPE)) {
			if(collectionDir != null && collectionDir.isDirectory()) {
				RemoveCollectionInfo(database,collectionName);
				return success = deleteDirectory(collectionDir);
			}
		}
		else {
			if(collection.getType().equals(VIRTUAL_COLLECTION_TYPE)) {
				RemoveCollectionInfo(database,collectionName);
				success = true;
			}
			else {
				if(collectionDir != null && collectionDir.isDirectory()) {
					stopUpdate(database,collectionName);
					RemoveCollectionInfo(database,collectionName);
					return success = deleteDirectory(collectionDir);
				}
			}
		}
		return success;
	}

	
	/**
	 * Retrieve the collection file
	 * @param database
	 * @param collectionName
	 * @param create
	 * @return
	 */
	private File getCollectionFile(String database, String collectionName, boolean create) {
		File collectionDir = getCollectionDir(database, collectionName, create);
		if(collectionDir == null || !collectionDir.exists() || !collectionDir.isDirectory()) 
			collectionDir.mkdirs();

		File collectionFile = Paths.get(collectionDir.getAbsolutePath(), COLLECTION_FILE).toFile();
		if(!collectionFile.exists() &&  create) {
			try {
				collectionFile.createNewFile();
			} catch (IOException e) {
				logger.error("Error creating collection file", e);
			}
		}
		return collectionFile;
	}

	
	/**
	 * Retrieve the collection index file
	 * @param database
	 * @param collectionName
	 * @param create
	 * @return
	 */
	private File getCollectionIndexFile(String database, String collectionName, boolean create) {
		File collectionDir = getCollectionDir(database, collectionName, create);
		if(collectionDir == null || !collectionDir.exists() || !collectionDir.isDirectory()) 
			collectionDir.mkdirs();

		File collectionFile = Paths.get(collectionDir.getAbsolutePath(), COLLECTION_INDEX_FILE).toFile();
		if(!collectionFile.exists() &&  create) {
			try {
				collectionFile.createNewFile();
			} catch (IOException e) {
				logger.error("Error creating collection file", e);
			}
		}
		return collectionFile;
	}

	
	/**
	 * Get the directory of a collection
	 * @param database
	 * @param collectionName
	 * @param create
	 * @return
	 */
	private File getCollectionDir(String database, String collectionName, boolean create) {
		return Paths.get(getDatabaseDirectory(database, create).getAbsolutePath(), collectionName).toFile();
	}

	
	/**
	 * Get the content of an existing collection 
	 */
	@Override
	public CollectionWrapper getCollection(String database, String collection) {
		return getCollection(database, collection, -1, 0, null);
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Get the content of an existing collection returning {limit} maximum documents
	 * starting from {offset} if static
	 * Else get the content from different urls
	 */
	@Override
	public CollectionWrapper getCollection(String database, String collection, Integer limit, Integer offset, Integer batchSize) {
        checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		
		if(metadata.getCollection(collection).getType().equals(VIRTUAL_COLLECTION_TYPE)) {
			CollectionWrapper collectionWrapper = null;;
			try {
				List<Url> list = GetMetadata(database).getCollection(collection).getUrl();
				String buffer;
				List<Map<String, Object>> documents = new LinkedList<>();
				for(Url k: list) {
					if(k != null) {
						buffer = getCollectionFromWeb(k.getUrl());
						Map<String, Object> jsonDocument = jsonMapper.readValue(buffer, new TypeReference<Map<String, Object>>() {});
						documents.add(jsonDocument);
					}
				}
				collectionWrapper = new CollectionWrapper(documents, 0, true, 0,0);
			} catch (IOException e) {
				logger.error("Impossible to deserialize collection from file", e);
			}
			return collectionWrapper;
		}
		else {
			if(metadata.getCollection(collection).getType().equals(STATIC_COLLECTION_TYPE))
				return GetCollectionFromFile( database, collection, limit, offset, batchSize);
			else {
				int index = 0;
				CollectionWrapper collectionWrapper = null;;
				List<Map<String, Object>> documents = new LinkedList<>();
				
// PF. 2021.10.20 why the cycle?			ZUN CHECK
				for(Url url: metadata.getCollection(collection).getUrl()) {
					File collectionDir = getCollectionDir(database, collection, false);
					File collectionFile = Paths.get(collectionDir.getAbsolutePath(), "collection" + index + ".data").toFile();
					File collectionIndexFile = Paths.get(collectionDir.getAbsolutePath(), "collection" + index + ".idx").toFile();
					
					if(collectionFile != null && collectionIndexFile != null) {
						try {
							FileInputStream collectionStream = new FileInputStream(collectionFile);
							FileInputStream collectionIndexStream = new FileInputStream(collectionIndexFile);
							int collectionSize = (int) (collectionIndexFile.length() / 12);
							
							int skip = Optional.ofNullable(offset).orElse(0);
							int remaining = limit != null && limit > 0 ? limit : collectionSize;
							remaining =  remaining > collectionSize ? collectionSize : remaining;
							int maxPerBatch = Optional.ofNullable(batchSize).orElse(MAX_PER_BATCH);
							int size = 0;
							int count = 0;
							
							byte[] offsetBuf = new byte[8];
							byte[] sizeBuf = new byte[4];
							collectionIndexStream.skip(12 * skip);
							collectionIndexStream.read(offsetBuf);
							collectionIndexStream.read(sizeBuf);
							long startPosition = bytesToLong(offsetBuf);
							int documentSize = bytesToInt(sizeBuf);
							//Position at the beginning of the first document to read
							collectionStream.skip(startPosition);
							
							while(remaining > 0) {
								byte[] docBytes = new byte[documentSize];
								//Retrieve the document
								if(collectionStream.read(docBytes) <= 0) {
									//if no more documents exit loop
									remaining = 0;
									break;
								}
								//Process the document
								Map<String, Object> jsonDocument = jsonMapper.readValue(docBytes, new TypeReference<Map<String, Object>>() {});
								documents.add(jsonDocument);
								//Update counts
								count++;
								remaining--;
								size += documentSize;
								
								//Check limits
								if(size > MAX_MESSAGE_SIZE) 
									break;

								if(count >= maxPerBatch) 
									break;
								
								//Check next document position and size
								if(remaining > 0) {
									collectionIndexStream.read(offsetBuf);
									collectionIndexStream.read(sizeBuf);
									startPosition = bytesToLong(offsetBuf);
									documentSize = bytesToInt(sizeBuf);
								}
							}
							
							collectionStream.close();
							collectionIndexStream.close();

							if(index == (metadata.getCollection(collection).getUrl().size() - 1)) 
								collectionWrapper = new CollectionWrapper(documents, count, remaining <= 0, remaining, offset + count);
							
							index++;
							
						} catch (IOException e) {
							logger.error("Impossible to deserialize collection from file", e);
						}
					}
				}
				
				return collectionWrapper;
			}
		}
	}

	
	/**
	 * 21-08 ROSSONI ALBERTO	
	 *Return a collection from its single .data file, only for static (ex getCollection)
	 */
	public CollectionWrapper GetCollectionFromFile(String database, String collection, Integer limit, Integer offset, Integer batchSize)
	{
		CollectionWrapper collectionWrapper = null;;
		List<Map<String, Object>> documents = new LinkedList<>();
		File collectionFile = getCollectionFile(database, collection, false);
		File collectionIndexFile = getCollectionIndexFile(database, collection, false);
		if(collectionFile != null && collectionIndexFile != null) {
			try {
				FileInputStream collectionStream = new FileInputStream(collectionFile);
				FileInputStream collectionIndexStream = new FileInputStream(collectionIndexFile);
				int collectionSize = (int) (collectionIndexFile.length() / 12);
				
				int skip = Optional.ofNullable(offset).orElse(0);
				int remaining = limit != null && limit > 0 ? limit : collectionSize;
				remaining =  remaining > collectionSize ? collectionSize : remaining;
				int maxPerBatch = Optional.ofNullable(batchSize).orElse(MAX_PER_BATCH);
				int size = 0;
				int count = 0;
				
				byte[] offsetBuf = new byte[8];
				byte[] sizeBuf = new byte[4];
				collectionIndexStream.skip(12 * skip);
				collectionIndexStream.read(offsetBuf);
				collectionIndexStream.read(sizeBuf);
				long startPosition = bytesToLong(offsetBuf);
				int documentSize = bytesToInt(sizeBuf);
				//Position at the beginning of the first document to read
				collectionStream.skip(startPosition);
				
				while(remaining > 0) {
					byte[] docBytes = new byte[documentSize];
					//Retrieve the document
					if(collectionStream.read(docBytes) <= 0) {
						//if no more documents exit loop
						remaining = 0;
						break;
					}
					//Process the document
					Map<String, Object> jsonDocument = jsonMapper.readValue(docBytes, new TypeReference<Map<String, Object>>() {});
					documents.add(jsonDocument);
					//Update counts
					count++;
					remaining--;
					size += documentSize;
					
					//Check limits
					if(size > MAX_MESSAGE_SIZE) 
						break;

					if(count >= maxPerBatch) 
						break;
					
					//Check next document position and size
					if(remaining > 0) {
						collectionIndexStream.read(offsetBuf);
						collectionIndexStream.read(sizeBuf);
						startPosition = bytesToLong(offsetBuf);
						documentSize = bytesToInt(sizeBuf);
					}
				}
				
				collectionStream.close();
				collectionIndexStream.close();

				collectionWrapper = new CollectionWrapper(documents, count, remaining <= 0, remaining, offset + count);
			} catch (IOException e) {
				logger.error("Impossible to deserialize collection from file", e);
			}
		}
		return collectionWrapper;
	}

	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Save a list of documents inside a collection (only for static)
	 */
	@Override
	public boolean saveCollection(String database, String collection, List<Map<String, Object>> documents, boolean append) {
		
		checkMetadata(database);
		CollectionsDescriptorManager metadata = GetMetadata(database);
		if(metadata.getCollection(collection) == null) {
			System.out.println("###### ################################ ##########");
			System.out.println("###### HO INSERITO UNA NUOVA COLLEZIONE ##########");
			System.out.println("###### ################################ ##########");
// ZUN CHECK
			createNewCollection (database, collection);
			metadata = GetMetadata(database);
		}			
		
		if((metadata.getCollection(collection).getType().equals(STATIC_COLLECTION_TYPE))) {
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
							logger.error("Impossible to serialize document to JSON", e);
						}
					}
					collectionFileStream.flush();
					collectionFileStream.close();
					indexFileStream.flush();
					indexFileStream.close();
					success = true;
				} catch (FileNotFoundException e) {
					logger.error("Collection file not found", e);
				} catch (IOException e) {
					logger.error("Impossible to serialize collection to JSON", e);
				}
			}
			return success;
	    }
		else {
			logger.error("Can't save over a virtual or dynamic collection");
			return false;
		}
	}

	
	/**
	 * Write the index record for a document
	 * @param indexFileStream
	 * @param objOffset
	 * @param objSize
	 * @throws IOException
	 */
	private void writeIndex(FileOutputStream indexFileStream, long objOffset, int objSize) throws IOException {
		byte[] longToBytes = longToBytes(objOffset);
		indexFileStream.write(longToBytes);
		byte[] intToBytes = intToBytes(objSize);
		indexFileStream.write(intToBytes);
	}

	
	/**
	 * Convert a byte array to long
	 * @param bytes
	 * @return
	 */
	private long bytesToLong(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 56) | 
			   ((bytes[1] & 0xFF) << 48) | 
			   ((bytes[2] & 0xFF) << 40) | 
			   ((bytes[3] & 0xFF) << 32) | 
			   ((bytes[4] & 0xFF) << 24) | 
	           ((bytes[5] & 0xFF) << 16) | 
	           ((bytes[6] & 0xFF) << 8 ) | 
	           ((bytes[7] & 0xFF) << 0 );
	}

	
	/**
	 * Convert a byte array to int
	 * @param bytes
	 * @return
	 */
	private int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | 
	           ((bytes[1] & 0xFF) << 16) | 
	           ((bytes[2] & 0xFF) << 8 ) | 
	           ((bytes[3] & 0xFF) << 0 );
	}

	
	/**
	 * Convert a long to byte array
	 * @param objOffset
	 * @return
	 */
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

	
	/**
	 * Convert an int to byte array
	 * @param objOffset
	 * @return
	 */
	private byte[] intToBytes(long objOffset) {
		return new byte[] {
				(byte) ((objOffset >> 24) & 0xFF),
				(byte) ((objOffset >> 16) & 0xFF),
				(byte) ((objOffset >> 8) & 0xFF),
				(byte) ((objOffset) & 0xFF)
		};
	}
	
	
	/**
	 * 21-08 ROSSONI ALBERTO
	 * Only works with static collections
	 */
	@Override
	public Long getCollectionCount(String database, String collection) {
		CollectionsDescriptorManager metadata = GetMetadata(database);
		long count = 0;
		if(metadata.getCollection(collection).getType().equals(STATIC_COLLECTION_TYPE)) {
			File collectionIndexFile = getCollectionIndexFile(database, collection, false);
			if(collectionIndexFile != null) 
				count = collectionIndexFile.length() / (Long.BYTES + Integer.BYTES);
		}

		return count;
	}
	

	/**
	 * 21-08 ROSSONI ALBERTO
	 * List of methods to support the implementation of json-descripted collection
	 */

	/**
	 * Get a database.metadata file converted into CollectionsDescriptorManager class
	 */
	private CollectionsDescriptorManager GetMetadata(String name)  {
		File databaseDir = getDatabaseDirectory(name, true);
		File metadata = getDatabaseMetadataFile(databaseDir); 
		CollectionsDescriptorManager collectionsInfo = new CollectionsDescriptorManager();
	    Gson gson = new Gson();
	    String json = "{}";
	   
		try {
			json = new String(Files.readAllBytes(Paths.get(metadata.toString())));
			logger.error(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		collectionsInfo = gson.fromJson(json, CollectionsDescriptorManager.class);
		return collectionsInfo;
	}

	
	/**
	 * Overwrite a database.metadata file with an associated CollectionsDescriptorManager class
	 */
	private boolean WriteMetadata (String databaseName, CollectionsDescriptorManager collectionsInfo)  {
		boolean success = false;
		File databaseDir = getDatabaseDirectory(databaseName, true);
		File metadata = getDatabaseMetadataFile(databaseDir); 
		FileWriter Writer;
		Gson gson = new Gson();
		String string = gson.toJson(collectionsInfo);
		try {
			Writer = new FileWriter( metadata.toString());
			Writer.write(string);
			Writer.close();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	
	/**
	 * Add a new collection to a database.metadata file
	 */
	private void AddCollectionInfo(String databaseName, String collectionName,List<Url> url, boolean dynamic) {
		CollectionsDescriptorManager metadata = null;
		collectionDescriptor collection = new collectionDescriptor();
		metadata = GetMetadata(databaseName);
		
		if(url == null) {
			collection.setName(collectionName);
			collection.setType(STATIC_COLLECTION_TYPE);
		}
		else {
			if(!dynamic) {
				collection.setName(collectionName);
				collection.setType(VIRTUAL_COLLECTION_TYPE);
				collection.initializeUrl();
				
				List<Url> list = new ArrayList<Url>();
				for(Url k : url) 
					list.add(k);
				
				collection.setUrl(list);
			}
			else {
				collection.setName(collectionName);
				collection.setType(DYNAMIC_COLLECTION_TYPE);
				collection.initializeUrl();
				
				List<Url> list = new ArrayList<Url>();
				for(Url k : url) {
					k.setFrequency(DEFAULT_FREQUENCY); //default frequency equals to 6 hours
					k.setUpdateType(0); //default type of update into the collection directory (append)
					list.add(k);
				}
				
				collection.setUrl(list);
			}
		}

		metadata.addCollection(collection);
		WriteMetadata(databaseName,metadata);
		
		if(dynamic) {
			int i=0;
// PF. 2021.10.20 why the cycle?	ZUN CHECK
			for(Url k: collection.getUrl()) {
				createObserver(databaseName, collectionName, i);
				i++;
			}
		}
	}

	
	/**
	 * Remove a collection from a database.metadata file
	 */	
	private void RemoveCollectionInfo(String databaseName, String collectionName)  {
		CollectionsDescriptorManager collectionsInfo = GetMetadata(databaseName);
		if(collectionsInfo.getCollection(collectionName).getType().equals(DYNAMIC_COLLECTION_TYPE)) {
			List<Url> list = collectionsInfo.getCollection(collectionName).getUrl();
			int i = 0;
			// PF. 2021.10.20 why the cycle? and i?  ZUN CHECK
			for(Url k: list) 
				deleteObserver(databaseName,collectionName,i);
		}
		collectionsInfo.removeCollection(collectionName);
		WriteMetadata(databaseName,collectionsInfo);
	}
	
	
	/**
	 * Initialize an empty database.metadata file
	 */
	private void initializeMetadata(String name) throws IOException, URISyntaxException {
		CollectionsDescriptorManager collectionsInfo = new CollectionsDescriptorManager();
		collectionsInfo.setdatabaseName(name);
		WriteMetadata(name, collectionsInfo);
	}
	
	
	/**
	 * Check if the collections inside the database matches the collections described into database.metadata file
	 */
	private void checkMetadata(String name) {
		File databaseDir = getDatabaseDirectory(name, true);
		File metadata = getDatabaseMetadataFile(databaseDir); 

		if(metadata.length() == 0) {
			try {
				initializeMetadata(name);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			CollectionsDescriptorManager descriptor = GetMetadata(name);
			for(File file : databaseDir.listFiles()) {
				if(file.isDirectory()) {
					collectionDescriptor newCollection = new collectionDescriptor();
					newCollection.setName(file.getName());
					newCollection.setType(STATIC_COLLECTION_TYPE);
					descriptor.addCollection(newCollection);
				}
			}
			boolean b = WriteMetadata(name,descriptor);
		}
	}

	
	/**
	 * Get a resource from an url in form of a string
	 */	
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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} // conn= response
           
	    	
            if(outSt == null)
            	outSt = "{}";

			return outSt;
	    }
	
	 
	 
	 /**
	  * 21-08 ROSSONI ALBERTO
	  * Initialize a thread which automaticaly get resources from its associated url, with its specific frequency of update
	  */
	 private void createObserver(String database, String collectionName, Integer index) {
		 Timer timer = new Timer();
		 
		 CollectionsDescriptorManager metadata = GetMetadata(database);
		 collectionDescriptor collection = metadata.getCollection(collectionName);
		 Url UrlClass = collection.getUrl().get(index);
		 String url = UrlClass.getUrl();
		 Integer frequency = UrlClass.getFrequency();
// PF. 2021.10.20 pare inutile
//		 Integer type = UrlClass.getUpdateType();
		 TimerTask task = new Observer(database, collectionName, url, index);
		 timer.schedule(task, 1, frequency);
	 }
	 
	 
	 /**
	  * 21-08 ROSSONI ALBERTO	
	  * Cancel the thread associated with a specific url inside a dynamic collection
	  */
	 // ZUN CHECK... perche' una new Observer????
	 private void deleteObserver(String database, String collectionName, Integer index) {
		 CollectionsDescriptorManager metadata = GetMetadata(database);
		 Observer task = new Observer(database, collectionName, 
				 						metadata.getCollection(collectionName).getUrl().get(index).getUrl(), 
				 						index);
		 
		 if(task.isRunning())
			 task.cancel();
	 }
}
