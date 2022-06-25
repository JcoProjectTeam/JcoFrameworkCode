package jco.ql.model.engine;

public interface IDatabase {
	
	String getName();

	IDocumentCollection getCollection(String name);

	void addCollection(IDocumentCollection collection);
}
