package jco.ql.db.ds.core.datatype;

import java.util.LinkedList;
import java.util.List;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDocumentCollection;

public class JcoDsCollection implements IDocumentCollection {
	
	private final String name;
	private final List<DocumentDefinition> documents;
	
	public JcoDsCollection(String name, List<DocumentDefinition> documents) {
		this.name = name;
		this.documents = documents;
	}
	
	public JcoDsCollection(String name) {
		this(name, new LinkedList<DocumentDefinition>());
	}

	@Override
	public List<DocumentDefinition> getDocumentList() {
		return documents;
	}

	@Override
	public void addDocument(DocumentDefinition document) {
		this.documents.add(document);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "{\n\t\"name\"= " + name + "\",\n\t\"documents\"=" + documents + "\n}";
	}


}
