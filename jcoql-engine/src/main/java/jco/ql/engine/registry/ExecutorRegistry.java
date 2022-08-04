package jco.ql.engine.registry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jco.ql.engine.annotation.Executor;
import jco.ql.engine.executor.IExecutor;
import jco.ql.model.command.ICommand;

@Service
public class ExecutorRegistry {

	private Map<String, IExecutor<ICommand>> registry;
	
	@Autowired
	public ExecutorRegistry(ApplicationContext applicationContext) {
		registry = new TreeMap<String, IExecutor<ICommand>>();
		Map<String, Object> executors = applicationContext.getBeansWithAnnotation(Executor.class);
		for(Entry<String, Object> entry : executors.entrySet()) {
			@SuppressWarnings("unchecked")
			IExecutor<ICommand> executor = (IExecutor<ICommand>) entry.getValue();
			Executor executorAnnotation = executor.getClass().getAnnotation(Executor.class);
			if(!executorAnnotation.overrideStandard() && registry.get(executorAnnotation.value().getName()) != null) {
				continue;
			}
			registry.put(executorAnnotation.value().getName(), executor);
		}
	}
	
	// PF - what is for?
	public void registerExecutor(Class<? extends ICommand> commandClass, IExecutor<ICommand> executor) {
		registry.put(commandClass.getName(), executor);
	}
	
	public IExecutor<ICommand> getExecutor(Class<? extends ICommand> commandClass) {
		return registry.get(commandClass.getName());
	}

}
