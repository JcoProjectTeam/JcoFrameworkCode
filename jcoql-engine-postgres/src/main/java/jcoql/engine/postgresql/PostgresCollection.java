package jcoql.engine.postgresql;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDocumentCollection;

import java.util.ArrayList;
import java.util.List;

public class PostgresCollection implements IDocumentCollection {
    private final String collectionName;
    private final List<DocumentDefinition> documents;

    public PostgresCollection(String collectionName, List<DocumentDefinition> initialDocuments) {
        this.collectionName = collectionName;
        this.documents = new ArrayList<>(initialDocuments);
    }
    
    public PostgresCollection(String collectionName) {
        this.collectionName = collectionName;
        this.documents = new ArrayList<>();
    }

    @Override
    public List<DocumentDefinition> getDocumentList() {
        return documents;
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public void addDocument(DocumentDefinition document) {
        documents.add(document);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("{\n");
        output.append("\"collectionName\" : \"" + collectionName + "\",\n");
        output.append("\"documents\" : [");
        for (int i = 0; i < documents.size(); i++) {
            output.append(documents.get(i).toString());
            if (i < documents.size() - 1) {
                output.append(",\n");
            }
        }
        output.append("\n]");
        output.append("\n}");
        return output.toString();
    }
}