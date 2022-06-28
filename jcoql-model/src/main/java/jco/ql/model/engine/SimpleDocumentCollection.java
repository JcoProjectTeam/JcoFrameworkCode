package jco.ql.model.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import jco.ql.model.DocumentDefinition;

public class SimpleDocumentCollection implements IDocumentCollection {

	private String name;

	private List<DocumentDefinition> documents;

	public SimpleDocumentCollection() {
		documents = new ArrayList<>();
	}

	public SimpleDocumentCollection(String name, List<DocumentDefinition> documents) {
		this.name = name;
		this.documents = documents;
	}

	public SimpleDocumentCollection(String name) {
		this(name, new ArrayList<DocumentDefinition>());
	}

	public void addDocument(DocumentDefinition document) {
		this.documents.add(document);
	}

	@Override
	public List<DocumentDefinition> getDocumentList() {
		return documents;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("{\n");
		//output.append("\"name\" : \"" + name + "\",\n");
		output.append("\"documents\" : [");
		if (!documents.isEmpty()) {
			StringJoiner joiner = new StringJoiner(",\n");
			int i = 0;
			for (; i < documents.size() - 1; i++) {
				joiner.add(documents.get(i).toString());
			}
			joiner.add(documents.get(i++).toString());
			output.append(joiner.toString());
		}
		output.append("\n]");
		output.append("\n}");
		return output.toString();
	}

}
