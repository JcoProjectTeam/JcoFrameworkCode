package jco.ql.engine;

import java.util.Properties;


public class EngineConfiguration {
	public static final String SETTINGS_CONFIG_PATH 	= "config";
	public static final String SETTINGS_CONFIG_FILE 	= "settings.properties";
	public static final String SETTINGS_N_PROCESSORS 	= "nProcessors";

	private Properties settings;
	private int nProcessors = 1;
	
	public EngineConfiguration () {
		settings = null;
		nProcessors = 1;		
	}
    private static EngineConfiguration only = new EngineConfiguration();

    public static EngineConfiguration getInstance() {
        return only;
    }
    public void setSettings(Properties settings) {
		this.settings = settings;
		if (settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS) != null)
			this.nProcessors = Integer.parseInt(settings.getProperty(EngineConfiguration.SETTINGS_N_PROCESSORS));

	}
    public int getNProcessors() {
    	return this.nProcessors;
    }
    public Properties getProperties() {
    	return this.settings;
    }
    
	
	

	
}
