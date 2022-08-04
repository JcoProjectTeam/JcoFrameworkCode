package jco.ql.db.ds.server.service;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import jco.ql.db.ds.core.datatype.CollectionWrapper;
import jco.ql.db.ds.server.collectionsDescriptor.Url;

/**
 * 21-08 ROSSONI ALBERTO
 * Added createVirtualCollection,createDynamicCollection,addUrl,removeUrl,listUrl,setFrequency,setUpdatetype and stopUpdate
 */

public interface DataSourceService {

	Properties getServerSettings();
	Properties getInstanceMetadata();
	
	boolean createDatabase(String name);
	boolean deleteDatabase(String name);
	List<String> listDatabases();

	boolean createCollection(String database, String collectionName);
	boolean createVirtualCollection(String database,String collectionName,List<Url> url);
	boolean createDynamicCollection(String database,String collectionName,List<Url> url);
	
	boolean addUrl(String database,String collectionName, List<Url> url);
	boolean removeUrl(String database,String collectionName,Integer index);
	List<String> listUrl(String database,String collectionName);
	
	boolean setFrequency(String database,String collectionName, Integer index, Integer frequency);
	boolean setUpdateType(String database,String collectionName,Integer index, Integer type);
	boolean stopUpdate(String database,String collectionName);
	
	boolean deleteCollection(String database, String collectionName);
	List<String> listCollections(String database);
	CollectionWrapper getCollection(String database, String collection);
	CollectionWrapper getCollection(String database, String collection, Integer limit, Integer offset, Integer batchSize);
	boolean saveCollection(String database, String collection, List<Map<String, Object>> documents, boolean append);
	Long getCollectionCount(String database, String collection);

}