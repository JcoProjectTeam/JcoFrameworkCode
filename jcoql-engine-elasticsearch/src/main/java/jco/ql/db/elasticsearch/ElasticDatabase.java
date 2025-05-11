package jco.ql.db.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;

public class ElasticDatabase implements IDatabase {

	private String dbname;
	private String host;
	private int port;

	// NOTA: from + size <= 10000
	private String from = "0";
	private String size = "10000";

	private static final String type = "doc";

	private static final int BATCHSIZE = 500;

	public ElasticDatabase(String host, int port, String dbname) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
	}

	@Override
	public String getName() {
		return dbname;
	}

	@Override
	public IDocumentCollection getCollection(String name) {
		// apro la connessione
		RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build();
		String query = "/" + name + "/_search";
		SimpleDocumentCollection result = new SimpleDocumentCollection(name);

		try {
			Map<String, String> params = new TreeMap<>();
			params.put("from", from);
			params.put("size", size);

			// leggo il mappings
			Response mappings = restClient.performRequest("GET", "/" + name);
			// leggo i documenti
			Response collections = restClient.performRequest("GET", query, params);
			String mappingsBody = EntityUtils.toString(mappings.getEntity());
			String collectionsBody = EntityUtils.toString(collections.getEntity());

			ElastichSearchJSONHandler json = new ElastichSearchJSONHandler();
			result = (SimpleDocumentCollection) json.createCollection(mappingsBody, collectionsBody, name);

			// chiudo la connessione
			restClient.close();
		} catch (IOException e) {
			if (e instanceof ResponseException) {
				System.out.println("Error: index " + name + " not found");
				System.exit(1);
			}
		}
		return result;
	}

	@Override
	public void addCollection(IDocumentCollection collection) {
		// RestClient restClient = RestClient.builder(new HttpHost(host, port,
		// "http")).build();
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));
		String index = collection.getName();

		try {
			// controllo l'esistenza dell'index, se esiste lo cancello
			Response existsResp = client.getLowLevelClient().performRequest("HEAD", index);
			// 200 = esiste
			// 404 = non esiste
			if (existsResp.getStatusLine().getStatusCode() == 200) {
				DeleteIndexRequest request = new DeleteIndexRequest(index);
				client.indices().deleteIndex(request);
			}

			List<DocumentDefinition> docs = collection.getDocumentList();
			if (docs == null || docs.isEmpty())
				JMH.addIOMessage("Saving to ElasticSearch database " + dbname + " an empty collection: " + collection.getName());
			else {
				// BulkRequest serve per l'inserimento di documenti multipli
				BulkRequest request = new BulkRequest();

				int size = docs.size();
				int from = 0;
				int to;
				boolean isOver = true;

				/*
				 * Algoritmo per l'inserimento dei json nel database L'inserimento viene fatto a
				 * lotti perchè è più efficiente nell'inserimento di collezioni composte da
				 * molti oggetti e contemporaneamente è più veloce rispetto al semplice
				 * inserimento singolo dato da un ciclo for
				 */

				while (isOver) {

					if ((size - from) <= BATCHSIZE) {
						isOver = false;
						to = size;
					} else
						to = from + BATCHSIZE;

					List<DocumentDefinition> subList = docs.subList(from, to);
					for (int i = 0; i < subList.size(); i++) {
						// if (!docs.get(i).checkUnderscores()) {
						request.add(new IndexRequest(index, type).source(docs.get(i).toString(), XContentType.JSON));
						/*
						 * } else { System.out.println(
						 * "Error: in Elasticsearch is impossible to save documents containing fields starting with '_' "
						 * ); System.out.println(docs.get(i).toString()); System.exit(1); }
						 */
					}
				}

				/*
				 * Versione con il low level client, inserimento di un documento alla volta
				 * HttpEntity entity = new NStringEntity(docs.get(i).toString(),
				 * ContentType.APPLICATION_JSON); Map<String, String> param =
				 * Collections.emptyMap(); String endpoint = "/" + index + "/" + type;
				 * restClient.performRequest("POST", endpoint, param, entity);
				 */
				BulkResponse resp = client.bulk(request, new BasicHeader("keep-alive", "120"));
				if (resp.hasFailures())
					System.out.println("Error: impossible to save documents in Elasticsearch");
				
				// chiudo la connessione
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
