// PF - what is this class for? maybe a test

package jco.ql.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.RestClient;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class ESExecuteJCO {

	public void executeJCOQL(String conf, String option, String input) {

		// Lettura file di configurazione
		Properties prop = new Properties();
		InputStream inputStream = null;
		String host = "";
		int port = 0;

		try {

			inputStream = new FileInputStream(conf);
			prop.load(inputStream);

			host = prop.getProperty("host");
			port = Integer.parseInt(prop.getProperty("port"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		// connessione a Elasticsearch

		// RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new
		// HttpHost(host, port, "http")));

		// GetRequest getRequest = new GetRequest("users");

		// GetResponse getResponse = client.get(getRequest);
		// Map<String, Object> valori = getResponse.getSourceAsMap();
		RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build();

		//Map<String, String> params = Collections.singletonMap("pretty", "true");
		try {
			/*Response response = restClient.performRequest("POST", "/users/_search", params);
			Response mappings = restClient.performRequest("GET", "/users", params);
			//RequestLine requestLine = response.getRequestLine();
			//HttpHost h = response.getHost();
			//int statusCode = response.getStatusLine().getStatusCode();
			//Header[] headers = response.getHeaders();
			String mappingsBody = EntityUtils.toString(mappings.getEntity());

			Response coll = restClient.performRequest("POST", "/users/_search", params);
			String collection = EntityUtils.toString(coll.getEntity());
			System.out.println(collection);
			JSONHandler j = new JSONHandler();
			SimpleDocumentCollection c = (SimpleDocumentCollection) j.createCollection(mappingsBody, collection, "users");

			for(int i = 0; i < c.getDocuments().size(); i++) {
				System.out.println(c.getDocuments().get(i));
				System.out.println();
			}
			*/


			//INSERIMENTO DI UN DOCUMENTO

			JCOValue v = new SimpleValue(100);
			FieldDefinition f = new FieldDefinition("prezzo", v);
			ArrayList<FieldDefinition> listf = new ArrayList<>();
			listf.add(f);

			v = new SimpleValue("ciao");
			f = new FieldDefinition("saluto", v);
			listf.add(f);

			DocumentDefinition doc = new DocumentDefinition(listf);

			HttpEntity entity = new NStringEntity(doc.toString(), ContentType.APPLICATION_JSON);
			Map<String, String> param = Collections.emptyMap();
			restClient.performRequest("POST", "/users/anagrafici", param, entity);

			restClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
