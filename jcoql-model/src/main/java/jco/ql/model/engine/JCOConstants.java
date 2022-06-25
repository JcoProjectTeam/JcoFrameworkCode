package jco.ql.model.engine;

public interface JCOConstants {
	// Values for method compareTo
	public static final int UNCOMPARABLE 	= -2;	
	public static final int LESS_THAN		= -1;	
	public static final int EQUAL 			= 0;	
	public static final int GREATER_THAN	= 1;	
	
	public static final String DATE_FORMAT				 	= "yyyy-MM-dd'T'HH:mm:ss";	
	public static final String DATE_FORMAT_EXT			 	= "yyyy-MM-dd'T'HH:mm:ss.SSS";	
	public static final String CASE_UNSENSITIVE_SUFFIX 		= ".#CASE.UNSESITIVE.DICTIONARY#_";	

	public static final String DOT 							= ".";
	public static final String DOT_REGEX					= "\\.";		// to be used for string splitting
	public static final String FIELD_SEPARATOR				= ".";			
	public static final String FIELD_SEPARATOR_REGEX		= "\\.";		// to be used for string splitting

	public static final String TIMESTAMP_FIELD_NAME 		= "timestamp";
	public static final String URL_FIELD_NAME 				= "url";
	public static final String DATA_FIELD_NAME 				= "data";

	public static final String GEOMETRY_FIELD_NAME 			= "~geometry";
	public static final String GEOMETRY_FIELD_NAME_DOT	 	= ".~geometry";
	public static final String FUZZYSETS_FIELD_NAME 		= "~fuzzysets";
	public static final String FUZZYSETS_FIELD_NAME_DOT		= ".~fuzzysets";
	public static final String ITEM_FIELD_NAME 				= "item";
	public static final String ITEM_FIELD_NAME_DOT			= ".item";
	public static final String POSISTION_FIELD_NAME 		= "position";
	public static final String POSISTION_FIELD_NAME_DOT		= ".position";

	public static final String LEFT_DOCUMENT_ALIAS 			= "~~left~Alias";
	public static final String RIGHT_DOCUMENT_ALIAS 		= "~~right~Alias~~";
/*
	public static final String GENERATE_FUZZY_ALIAS 		= "~~CurrentDoc~Alias~~";
	public static final String FILTER_ALIAS 				= "~~CurrentDoc~Alias~~";	
	public static final String CASE_ALIAS 					= "~~CurrentDoc~Alias~~";	
	public static final String GROUP_ALIAS 					= "~~CurrentDoc~Alias~~";
	public static final String FROMWEB_ALIAS 				= "~~CurrentDoc~Alias~~";
	public static final String JOIN_ALIAS 					= "~~CurrentDoc~Alias~~";
	public static final String SPATIALIJOIN_ALIAS 			= "~~CurrentDoc~Alias~~";
/*
	public static final String GENERATE_FUZZY_ALIAS 		= "~~Generate~Fuzzy~Alias~~";
	public static final String FILTER_ALIAS 				= "~~Filter~Alias~~";	
	public static final String GROUP_ALIAS 					= "~~Group~Alias~~";
	public static final String FROMWEB_ALIAS	 			= "~~From~Web~Alias~~";
	public static final String JOIN_ALIAS	 				= "~~Join~Alias~~";
	public static final String SPATIALIJOIN_ALIAS 			= "~~Spatiali~Join~Alias~~";
*/
	public static final String FROMWEB_COLLECTION_NAME		= "FromWebCollection";
	public static final String FILTER_COLLECTION_NAME		= "FilterCollection";
	public static final String JOIN_COLLECTION_NAME			= "JoinCollection";
	public static final String GROUP_COLLECTION_NAME		= "GroupCollection";
	public static final String TEMPORARY_COLLECTION_NAME	= "temporary";

	public static final String TRUE_UCST	 				= "TRUE";			
	public static final String FALSE_UCST 					= "FALSE";			
	public static final String TRUE_LCST 					= "true";			
	public static final String FALSE_LCST 					= "false";	
	
}
