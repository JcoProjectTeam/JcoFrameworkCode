package jco.ql.model.engine;

import java.io.File;

public interface JCOConstants {
	// Values for method compareTo
	public static final int UNCOMPARABLE 	= -2;	
	public static final int LESS_THAN		= -1;	
	public static final int EQUAL 			= 0;	
	public static final int GREATER_THAN	= 1;	
	
	public static final String TRUE_UCST	= "TRUE";			
	public static final String FALSE_UCST 	= "FALSE";			
	public static final String TRUE_LCST 	= "true";			
	public static final String FALSE_LCST 	= "false";	
	
	public static final String DATE_FORMAT				 = "yyyy-MM-dd'T'HH:mm:ss";	
	public static final String DATE_FORMAT_EXT			 = "yyyy-MM-dd'T'HH:mm:ss.SSS";	
	public static final String CASE_UNSENSITIVE_SUFFIX 	= ".#CASE.UNSESITIVE.DICTIONARY#_";	

	public static final String DOT 						= ".";
	public static final String DOT_REGEX				= "\\.";		// to be used for string splitting
	public static final String FIELD_SEPARATOR			= ".";			
	public static final String FIELD_SEPARATOR_REGEX	= "\\.";		// to be used for string splitting

	public static final String TIMESTAMP_FIELD_NAME 	= "timestamp";
	public static final String URL_FIELD_NAME 			= "url";
	public static final String DATA_FIELD_NAME 			= "data";
	public static final String SOURCE_FIELD_NAME 		= "source";

	public static final String MONGODB_ID_FIELD_NAME 	= "_id";
	public static final String GEOMETRY_FIELD_NAME 		= "~geometry";
	public static final String GEOMETRY_FIELD_NAME_DOT	= ".~geometry";
	public static final String FUZZYSETS_FIELD_NAME 	= "~fuzzysets";
	public static final String FUZZYSETS_FIELD_NAME_DOT	= ".~fuzzysets";
	public static final String ITEM_FIELD_NAME 			= "item";
	public static final String ITEM_FIELD_NAME_DOT		= ".item";
	public static final String POSISTION_FIELD_NAME 	= "position";
	public static final String POSISTION_FIELD_NAME_DOT	= ".position";

	public static final String LEFT_DOCUMENT_ALIAS 		= "~~left~Alias";
	public static final String RIGHT_DOCUMENT_ALIAS 	= "~~right~Alias~~";

	public static final String FROMWEB_COLLECTION_NAME			= "FromWebCollection";
	public static final String FILTER_COLLECTION_NAME			= "FilterCollection";
	public static final String JOIN_COLLECTION_NAME				= "JoinCollection";
	public static final String GROUP_COLLECTION_NAME			= "GroupCollection";
	public static final String LOOKUPFROMWEB_COLLECTION_NAME	= "LookupFromWebCollection";
	public static final String TEMPORARY_COLLECTION_NAME		= "temporary";

	public static final String JCO_FRAMEWORK_TEMP_DIR			= "JCoFramework" + File.separator + "temp_";

	// Geometric constants
	public static final double EARTH_RADIUS_KM 				= 6371;
	public static final double EARTH_RADIUS_M 				= 6371 * 1000;
	public static final double EARTH_SURFACE_M 				= 4 * Math.PI * EARTH_RADIUS_M * EARTH_RADIUS_M ;	
	public static final double KM_2_MILE 					= 1.609344;
	public static final double KM_2_M 						= 1000;
	public static final double KM_2_M_SQUARE 				= 1000*1000;
	public static final double M_2_MILE_SQUARE 				= 1609.344*1609.344; 	
}
