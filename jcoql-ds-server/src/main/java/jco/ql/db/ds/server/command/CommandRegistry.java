package jco.ql.db.ds.server.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jco.ql.db.ds.server.annotation.JcoDsCommand;

@Component
@Profile("server")
public class CommandRegistry {
	private static final Logger logger = LoggerFactory.getLogger(CommandRegistry.class);
	
	private final ApplicationContext applicationContext;
	private Map<Long, ICommand> registry;

	@Autowired
	private CommandRegistry(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.registry = new HashMap<Long, ICommand>();
		registerCommands();
	}

	private void registerCommands() {
		Map<String, Object> commands = applicationContext.getBeansWithAnnotation(JcoDsCommand.class);
		for(Entry<String, Object> entry : commands.entrySet()) {
			ICommand command = (ICommand) entry.getValue();
			if(registry.get(command.getCode()) != null) {
				continue;
			}
			registry.put(command.getCode(), command);
			logger.info("Registered command class {} for code {}", command, command.getCode());
		}
	}
	
	public ICommand getCommand(long code) {
		return registry.get(code);
	}
}
