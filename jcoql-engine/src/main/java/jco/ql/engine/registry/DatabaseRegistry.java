package jco.ql.engine.registry;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import jco.ql.model.engine.IDatabase;

@Service
public class DatabaseRegistry {

	private Map<String, IDatabase> registry;

	public DatabaseRegistry() {
		registry = new TreeMap<String, IDatabase>();
	}

	public void registerDatabase(String name, IDatabase database) {
		registry.put(name, database);
	}

	public IDatabase getDatabase(String name) {
		return registry.get(name);
	}

	public void deleteDatabase(String name) {
		registry.remove(name);
	}

}
