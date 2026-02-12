package jco.ql.db.mongodb;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;

import java.util.ArrayList;
import java.util.Arrays;

public class MongoDbDatabase implements IDatabase {

	private String dbname;
	private String host;
	private int port;

	private static final int BATCHSIZE = 500;

	public MongoDbDatabase(MongoDatabase mongoDatabase) {
		this.dbname = "";
	}

	public MongoDbDatabase(MongoClient client, String databaseName) {
		this.dbname = databaseName;
	}

	public MongoDbDatabase(String host, int port, String dbname) {
		this.dbname = dbname;
		this.host = host;
		this.port = port;
	}

	@Override
	public IDocumentCollection getCollection(String name) {
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(host, port))))
				.build();
		MongoClient client = MongoClients.create(settings);
		MongoDatabase mongoDatabase = client.getDatabase(dbname);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(name);
		MongoDbCollection result = new MongoDbCollection(name, mongoCollection);

		client.close();
		return result;
	}

	@Override
	public String getName() {
		return dbname;
	}

	@Override
	public void addCollection(IDocumentCollection collection) {
		addCollection(collection, collection.getName());
	}

	public void addCollection(IDocumentCollection collection, String collectionName) {

		MongoClientSettings settings = MongoClientSettings.builder()
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(host, port))))
				.applyToSocketSettings(builder -> builder
						.connectTimeout(30000000, TimeUnit.MILLISECONDS)
						.readTimeout(30000000, TimeUnit.MILLISECONDS) 
				)
				.applyToServerSettings(builder -> builder
						.minHeartbeatFrequency(25, TimeUnit.MILLISECONDS)
						.heartbeatFrequency(3000000, TimeUnit.MILLISECONDS) 
				)
				.build();
		MongoClient client = MongoClients.create(settings);
		MongoDatabase mongoDatabase =  client.getDatabase(dbname);

		MongoCollection<Document> coll = mongoDatabase.getCollection(collectionName);

		// ZUN - Modified with ChatGPT on 2026-01-26
		boolean exists = mongoDatabase
							.listCollectionNames()
							.into(new ArrayList<>())
							.contains(collectionName);
		if (exists) 
		    coll.drop();
		
		mongoDatabase.createCollection(collectionName);

		List<DocumentDefinition> docs = collection.getDocumentList();

		if (docs == null || docs.isEmpty())
			JMH.addIOMessage("Saving to MongoDB dabase " + dbname + " an empty collection: " + collectionName);
		else {

			int size = docs.size();
			int from = 0;
			int to;
			boolean isOver = true;

			/*
			 * Algoritmo per l'inserimento dei json nel database L'inserimento
			 * viene fatto a lotti perche' e' piu' � efficiente nell'inserimento di
			 * collezioni composte da molti oggetti e contemporaneamente e' piu'
			 * veloce rispetto al semplice inserimento singolo dato da un ciclo
			 * for
			 */

			while (isOver) {

				if ((size - from) <= BATCHSIZE) {
					isOver = false;
					to = size;
				} else
					to = from + BATCHSIZE;

				List<DocumentDefinition> subList = docs.subList(from, to);

				coll.insertMany(
						subList.stream().map(DocumentUtils::bsonFromDocumentDefinition).collect(Collectors.toList()));

				from = to;

			}
		}

		client.close();
	}

}
