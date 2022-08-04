package jco.ql.model.engine;

import java.util.List;

import jco.ql.model.DocumentDefinition;

public interface IDocumentCollection {

	public List<DocumentDefinition> getDocumentList();
	
	public void addDocument(DocumentDefinition document);

	public String getName();
}
