package jco.ql.db.mongodb;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;

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
		MongoClient client = new MongoClient(host, port);
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

		MongoClient client = new MongoClient(new ServerAddress(host , port),MongoClientOptions.builder()
                .socketTimeout(30000000)
                .minHeartbeatFrequency(25)
                .heartbeatSocketTimeout(3000000)
                .socketKeepAlive(true)
                .build() );
		MongoDatabase mongoDatabase =  client.getDatabase(dbname);


		MongoCollection<Document> coll = mongoDatabase.getCollection(collectionName);
		if (coll != null) {
			coll.drop();
		}

		mongoDatabase.createCollection(collectionName);

		List<DocumentDefinition> docs = collection.getDocumentList();

		if (docs == null || docs.isEmpty())
			System.out.println("Error: Out collection is empty");

		else {

			int size = docs.size();
			int from = 0;
			int to;
			boolean isOver = true;

			/*
			 * Algoritmo per l'inserimento dei json nel database L'inserimento
			 * viene fatto a lotti perche' e' piu' ¹ efficiente nell'inserimento di
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
