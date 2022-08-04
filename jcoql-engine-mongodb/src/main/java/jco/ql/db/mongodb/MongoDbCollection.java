package jco.ql.db.mongodb;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;



public class MongoDbCollection implements IDocumentCollection {
	
	private String name;

	private List<DocumentDefinition> documents;
	
	public MongoDbCollection(String name, MongoCollection<Document> mongoCollection) {
		try {
			Stream<Document> mongoStream = StreamSupport.stream(mongoCollection.find().spliterator(), false);
			//this.collection = mongoStream.map(DocumentUtils::mapDocumentDefinitionFromBson);
			this.documents = mongoStream.map(DocumentUtils::mapDocumentDefinitionFromBson).collect(Collectors.toList());
			this.name = name;
		} 
		catch (Exception e){
			JMH.addExceptionMessage("[MongoDb Driver] - Unable to load collection " + name + " from MongoDb\n" +
					"Cause:\t" + e.getMessage());
		}
	}
	
	@Override
	public List<DocumentDefinition> getDocumentList() {
		//return this.collection.collect(Collectors.toList());
		return this.documents;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addDocument(DocumentDefinition document) {
		documents.add(document);
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
