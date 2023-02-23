package jco.ql.engine;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;


public class EngineConfiguration implements JCOConstants {
	public static final String SETTINGS_CONFIG_PATH 	= "config";
	public static final String SETTINGS_CONFIG_FILE 	= "settings.properties";
	public static final String SETTINGS_N_PROCESSORS 	= "nProcessors";
	

	private Properties settings;
	private int nProcessors = 1;
	private String tempDirectory;
	
	public EngineConfiguration () {
		Date today = new Date();

		settings = null;
		nProcessors = 1;		
    	JMH.addConfigurationMessage("Available processors:\t" + nProcessors);

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
    private static EngineConfiguration only = new EngineConfiguration();

    public static EngineConfiguration getInstance() {
        return only;
    }
    public void setSettings(Properties settings) {
		this.settings = settings;
		if (settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS) != null)
			this.nProcessors = Integer.parseInt(settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS));
    	JMH.addConfigurationMessage("Available processors:\t" + nProcessors);
	}
    public int getNProcessors() {
    	return this.nProcessors;
    }
    public Properties getProperties() {
    	return this.settings;
    }
    public String getTempDirectory () {
    	return tempDirectory;
    }
 	
}
