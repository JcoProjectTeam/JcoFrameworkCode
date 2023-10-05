package jco.ql.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;

/*
 * PF. 2023.09.15 
 * Some configuration can be set in file settings.properties
 * other configuration can set by the jco.ql.engine.parser.ParserLauncher (by means of a user interface)
 * */
public class EngineConfiguration implements JCOConstants {
	public static final String SETTINGS_CONFIG_PATH 	= "config";
	public static final String SETTINGS_CONFIG_FILE 	= "settings.properties";
	public static final String SETTINGS_N_PROCESSORS 	= "nProcessors";
	public static final String SETTINGS_MONGO_ID 		= "removeMongoId";
	public static final String SETTINGS_TRACK_TIMES 	= "trackTimes";
	public static final String SETTINGS_SPATIAL_INDEX 	= "spatialIndexing";
	public static final String SETTINGS_MSG_IN_DOC 		= "msgInDoc";
	public static final String SETTINGS_BACKTRACK 		= "backtrack";
	

	final Logger logger = LoggerFactory.getLogger(JcoEngine.class);

	private Properties settings;
	private String tempDirectory;
	private int nProcessors = 1;
	private boolean removeMongoId = true;
	private boolean trackTimes = true;
	private boolean spatialIndexing = false;
	private boolean msgInDoc = false;
	private boolean backtrack = false;

	private static EngineConfiguration only = new EngineConfiguration();
	
	public EngineConfiguration () {
		Date today = new Date();

		settings = null;
		nProcessors = 1;		
		removeMongoId = true;
		trackTimes = true;
		msgInDoc = false;
		spatialIndexing = false;
		backtrack = false;

		tempDirectory = System.getProperty("java.io.tmpdir");
		if (!tempDirectory.endsWith(File.separator))
			tempDirectory  += File.separator;
		tempDirectory +=  JCO_FRAMEWORK_TEMP_DIR + today.getTime() + File.separator;


    	File file = new File(tempDirectory);
        // true if the directory was created, false otherwise
        if (file.mkdirs()) {
        	JMH.addConfigurationMessage("Temporary directory:\t" + tempDirectory);
        }
        else {
        	JMH.addConfigurationMessage("Cannot create temporary directory:\t" + tempDirectory);
        }
	}
	
	public void loadSettings () {
		only.settings = new Properties();
		try {
			InputStream fis = new FileInputStream(Paths.get(EngineConfiguration.SETTINGS_CONFIG_PATH, EngineConfiguration.SETTINGS_CONFIG_FILE).toFile());
			if(fis != null) {
				only.settings.load(fis);
				if (settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS) != null)
					only.nProcessors = Integer.parseInt(settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS));
				if (settings.getProperty(EngineConfiguration.SETTINGS_MONGO_ID) != null)
					only.removeMongoId = !"false".equals(settings.getProperty(EngineConfiguration.SETTINGS_MONGO_ID));
				if (settings.getProperty(EngineConfiguration.SETTINGS_TRACK_TIMES) != null)
					only.trackTimes = !"false".equals(settings.getProperty(EngineConfiguration.SETTINGS_TRACK_TIMES));

				if (settings.getProperty(EngineConfiguration.SETTINGS_SPATIAL_INDEX) != null)
					only.msgInDoc = "true".equals(settings.getProperty(EngineConfiguration.SETTINGS_SPATIAL_INDEX));
				if (settings.getProperty(EngineConfiguration.SETTINGS_MSG_IN_DOC) != null)
					only.spatialIndexing = "true".equals(settings.getProperty(EngineConfiguration.SETTINGS_MSG_IN_DOC));
				if (settings.getProperty(EngineConfiguration.SETTINGS_BACKTRACK) != null)
					only.backtrack = "true".equals(settings.getProperty(EngineConfiguration.SETTINGS_BACKTRACK));
			}
		} catch (IOException e) {
			logger.error("Error in loading settings from the settings.properties file", e);
			JMH.addConfigurationMessage("Error in loading settings from the settings.properties file\n" + e.toString());
		}	

		reportConfiguration();
	}
	private void reportConfiguration () {
    	JMH.addConfigurationMessage("Available processors:\t" + only.nProcessors);		
    	JMH.addConfigurationMessage("Remove MongoDB Id:\t" + only.removeMongoId);		
    	JMH.addConfigurationMessage("Track instruction execution times:\t" + only.trackTimes);		
    	JMH.addConfigurationMessage("Store messages in documents:\t" + only.msgInDoc);		
    	JMH.addConfigurationMessage("Use spatial indexing:\t" + only.spatialIndexing);		
    	JMH.addConfigurationMessage("Activate backtrack:\t" + only.backtrack);		
	}
	
    public static EngineConfiguration getInstance() {
        return only;
    }
    public static Properties getProperties() {
    	return only.settings;
    }
    public static String getTempDirectory () {
    	return only.tempDirectory;
    }
 	
    public static int getNProcessors() {
    	return only.nProcessors;
    }
    public static void setNProcessors(int np) {
    	if (np > 0)
    		only.nProcessors = np;
    }

    public static boolean isRemoveMondgoId() {
    	return only.removeMongoId;
    }
    public static void setRemoveMondgoId(boolean b) {
    	only.removeMongoId = b;
    }

    public static boolean isTrackTimes() {
    	return only.trackTimes;
    }
    public static void setTrackTimes(boolean b) {
    	only.trackTimes = b;
    }

    public static boolean isMsgInDoc() {
    	return only.msgInDoc;
    }
    public static void setMsgInDoc(boolean b) {
    	only.msgInDoc = b;
    }

    public static boolean isSpatialIndexing() {
    	return only.spatialIndexing;
    }
    public static void setSpatialIndexing(boolean b) {
    	only.spatialIndexing = b;
    }

    public static boolean isBacktrack() {
    	return only.backtrack;
    }
    public static void setBacktrack(boolean b) {
    	only.backtrack = b;
    }

}
