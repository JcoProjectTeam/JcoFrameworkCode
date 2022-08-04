package jco.ql.db.ds.core.message;


/**
 * 21-08 ROSSONI ALBERTO
 * Added createVirtualCollection,createDynamicCollection,addUrl, removeUrl, listUrl, setFrequency, setUpdateType and stopUpdate
 */

public final class MessageCodes {
	//Error Message
	public static final long ERROR_RESPONSE =					   0xF0000000;
	
	//Instance messages
	public static final long PING = 							   0x00000001;
	public static final long PING_RESPONSE = 					   0x00000002;
	
	//Database messages
	public static final long LIST_DATABASE = 					   0x00010001;
	public static final long LIST_DATABASE_RESPONSE = 			   0x00010002;
	public static final long CREATE_DATABASE = 					   0x00010003;
	public static final long CREATE_DATABASE_RESPONSE = 		   0x00010004;
	public static final long DELETE_DATABASE = 					   0x00010005;
	public static final long DELETE_DATABASE_RESPONSE = 		   0x00010006;
	
	//Collection messages
	public static final long LIST_COLLECTIONS = 				   0x00020001;
	public static final long LIST_COLLECTIONS_RESPONSE = 		   0x00020002;
	public static final long CREATE_COLLECTION = 				   0x00020003;
	public static final long CREATE_COLLECTION_RESPONSE = 		   0x00020004;
	public static final long CREATE_VIRTUAL_COLLECTION =           0x00020013;
	public static final long CREATE_VIRTUAL_COLLECTION_RESPONSE =  0x00020014;
	public static final long CREATE_DYNAMIC_COLLECTION =           0x00020023;
	public static final long CREATE_DYNAMIC_COLLECTION_RESPONSE =  0x00020024;
	public static final long DELETE_COLLECTION = 				   0x00020005;
	public static final long DELETE_COLLECTION_RESPONSE = 		   0x00020006;
	public static final long GET_COLLECTION = 					   0x00020007;
	public static final long GET_COLLECTION_RESPONSE = 			   0x00020008;
	public static final long SAVE_COLLECTION = 					   0x00020009;
	public static final long SAVE_COLLECTION_RESPONSE = 		   0x0002000A;
	public static final long GET_COLLECTION_COUNT =				   0x0002000B;
	public static final long GET_COLLECTION_COUNT_RESPONSE =	   0x0002000C;
	
	//Url messages
	public static final long ADD_URL =                             0X00030001;
	public static final long ADD_URL_RESPONSE =                    0X00030002;
	public static final long REMOVE_URL =                          0X00030003;
	public static final long REMOVE_URL_RESPONSE =                 0X00030004;
	public static final long LIST_URL =                            0X00030005;
	public static final long LIST_URL_RESPONSE =                   0X00030006;
	
	//Dynamic messages
	public static final long SET_FREQUENCY =                       0X00040001;
	public static final long SET_FREQUENCY_RESPONSE =              0X00040002;
	public static final long SET_UPDATE_TYPE =                     0X00040003;
	public static final long SET_UPDATE_TYPE_RESPONSE =            0X00040004;
	public static final long STOP_UPDATE =                         0X00040005;
    public static final long STOP_UPDATE_RESPONSE =                0X00040006;
}
