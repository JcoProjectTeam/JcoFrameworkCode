package jco.ql.engine;

import java.util.Map;
import java.util.TreeMap;

import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;

public class SimpleDatabase implements IDatabase {

	private String name; 
	
	private Map<String, IDocumentCollection> collections;
	
	public SimpleDatabase(String name) {
		super();
		this.name = name;
		this.collections = new TreeMap<String, IDocumentCollection>();
	}
	
	public SimpleDatabase(IDocumentCollection collection) {
		this("", collection);
	}
	
	public SimpleDatabase(String name, IDocumentCollection collection) {
		this(name);
		this.collections.put(collection.getName(), collection);
	}

	@Override
	public IDocumentCollection getCollection(String name) {
		return collections.get(name);
	}
	
	@Override
	public void addCollection(IDocumentCollection collection) {
		this.collections.put(collection.getName(), collection);
	}

	@Override
	public String getName() {
		return name;
	}

}
