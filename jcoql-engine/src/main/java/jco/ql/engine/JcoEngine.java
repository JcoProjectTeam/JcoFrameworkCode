package jco.ql.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.IExecutor;
import jco.ql.engine.registry.ExecutorRegistry;
import jco.ql.model.command.ICommand;
import jco.ql.model.engine.JMH;

@Configuration
@ComponentScan
public class JcoEngine implements IEngine {
	final Logger logger = LoggerFactory.getLogger(JcoEngine.class);

	protected ExecutorRegistry executorRegistry;
	protected Pipeline pipeline;
	private Properties settings;

	@Autowired
	public JcoEngine(ExecutorRegistry executorRegistry) {
		super();
		this.executorRegistry = executorRegistry;
		loadSettings ();
		pipeline = new Pipeline();
	}

	@Override
	public void execute(ICommand command) throws ExecuteProcessException, ScriptException {
		if(command == null) {
			throw new ExecuteProcessException("No commands to execute");
		} else {
			IExecutor<ICommand> executor = getExecutor(command.getClass());
			if(executor == null) {
				logger.error("No executor found for command of type %s", command.getClass());
				throw new ExecuteProcessException("No executor found for command of type %s", command.getClass());
			}
			logger.debug("Executing command: " + command.toString());
			
			// PF - Exectute command
			executor.execute(pipeline, command);
		}
	}

	@Override
	public void execute(List<ICommand> commands) throws ExecuteProcessException, ScriptException {
		if(commands == null || commands.isEmpty()) {
			throw new ExecuteProcessException("No commands to execute");
		} else 	
			for(ICommand command : commands) 
				execute(command);
	}
	
	
	// PF - Inserted to handle settings --- TODO move inside EngineConfiguration
	private void loadSettings() {
		settings = new Properties();
		try {
			InputStream fis = new FileInputStream(Paths.get(EngineConfiguration.SETTINGS_CONFIG_PATH, EngineConfiguration.SETTINGS_CONFIG_FILE).toFile());
			if(fis != null) {
				settings.load(fis);
				EngineConfiguration.getInstance().setSettings(settings);
			}
		} catch (IOException e) {
			logger.error("Error loading settings from the settings.properties file", e);
			JMH.addConfigurationMessage("Error loading settings from the settings.properties file\n" + e.toString());
		}
	
	}
	
	private IExecutor<ICommand> getExecutor(Class<? extends ICommand> clazz) {
		return executorRegistry.getExecutor(clazz);
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

}
