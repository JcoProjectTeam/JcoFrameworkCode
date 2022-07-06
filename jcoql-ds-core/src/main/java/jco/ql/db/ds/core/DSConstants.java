package jco.ql.db.ds.core;

public interface DSConstants {

	public static final String SETTINGS_CONFIG_PATH			= "config";
	public static final String SETTINGS_CONFIG_FILE			= "settings.properties";
	public static final String SETTINGS_SERVER_PORT			= "server.port";
	public static final String SETTINGS_SERVER_DATA_PATH	= "server.data-path";
	public static final String DEFAULT_SETTINGS_SERVER_PORT = "44446";

	public static final String INSTANCE_METADATA_FILE	= "instance.metadata";
	public static final String DATABASE_METADATA_FILE 	= "database.metadata";
	public static final String COLLECTION_FILE 			= "collection.data";
	public static final String COLLECTION_BAK_FILE		= "collection.data.bak";
	public static final String COLLECTION_INDEX_FILE 	= "collection.idx";
	
	public static final String VIRTUAL_COLLECTION_TYPE	= "virtual";
	public static final String STATIC_COLLECTION_TYPE	= "static";
	public static final String DYNAMIC_COLLECTION_TYPE	= "dynamic";

	//Maximum response message size (20 MB)
	public static final int MAX_MESSAGE_SIZE 	= 1024 * 1024 * 20;
	public static final int MAX_PER_BATCH 		= 5000;
//	public static final int DEFAULT_FREQUENCY 	= 6* 60 * 60 * 1000; // 6h
	public static final int DEFAULT_FREQUENCY 	= 60 * 1000; // 1 min
	public static final int DEFAULT_UPDATE_TYPE = 0;
	
}
