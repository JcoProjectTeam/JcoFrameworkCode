package jco.ql.db.elasticsearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.wololo.jts2geojson.GeoJSONReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class ElastichSearchJSONHandler implements JCOConstants {

	// modificare opportunamente in base agli aggiornamenti di Elasticsearch
	// Secondo la documentazione attuale � previsto uno standard di questo tipo:
	// {index}/_doc
	// Attualmente per� la API non permette di inserire '_' nel nome del type
	private String type = "doc";

	private Map<String, String> valueType;

	public ElastichSearchJSONHandler() {
		valueType = new TreeMap<>();
	}

	public IDocumentCollection createCollection(String mappings, String collection, String collectionName) {

		List<DocumentDefinition> list = new ArrayList<>();

		try {
			// var contiene coppie [nome_campo, tipo]
			valueType = readMappings(mappings);

			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = f.createParser(collection);
			JsonToken current = jp.nextToken();

			// il documento JSON deve iniziare con {
			if (current != JsonToken.START_OBJECT) {
				System.out.println("Error: root should be object: quiting.");
				return null;
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jp.getCurrentName();
				current = jp.nextToken();
				JsonNode node = jp.readValueAsTree();

				if (fieldName.equals("hits")) {
					if (node.get("total").asInt() == 0)
						// non ci sono documenti
						return new SimpleDocumentCollection(collectionName);
					else {
						JsonNode hits = node.get("hits");
						for (int i = 0; i < hits.size(); i++) {
							JsonNode doc = hits.get(i);
							// se serve leggere id del documento: 
							//Value v = new SimpleValue(doc.get("_id").asText());

							// leggo il contenuto del documento
							JsonNode source = doc.get("_source");
							List<FieldDefinition> fields = createDocument(source);
							DocumentDefinition document = new DocumentDefinition(fields);
							list.add(document);
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new SimpleDocumentCollection(collectionName, list);
	}

	private List<FieldDefinition> createDocument(JsonNode source) {
		Iterator<String> fields = source.fieldNames();

		JCOValue v = new SimpleValue();
		List<FieldDefinition> list = new ArrayList<>();
		// list.add(id);
		while (fields.hasNext()) {
			String fname = fields.next();
			String type = valueType.get(fname);

			if (type.equals("document")) {
				if (GEOMETRY_FIELD_NAME.equals(fname)) {
					v = new GeometryValue(new GeoJSONReader().read(source.get(fname).toString()));
				}
				else 
					v = new DocumentValue(createDocument(source.get(fname)));
			
			}else if (source.get(fname).isArray())
				v = new ArrayValue(createArray(source.get(fname), type));
			else
				v = getValue(type, source.get(fname));

			FieldDefinition field = new FieldDefinition(fname, v);
			list.add(field);
		}
		return list;
	}

	private List<JCOValue> createArray(JsonNode array, String type) {
		List<JCOValue> result = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			JCOValue v = getValue(type, array.get(i));
			result.add(v);
		}
		return result;
	}

	private JCOValue getValue(String type, JsonNode value) {
		JCOValue v = null;
		switch (type) {
		case "text":
			v = new SimpleValue(value.asText());
			break;
		case "long":
			v = new SimpleValue(value.asLong());
			break;
		case "date":
			try {
				//esempio data: "2018-01-26T08:51:41+0100"
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
				OffsetDateTime odt = OffsetDateTime.parse(value.asText(), formatter);
				Date d = Date.from(odt.toInstant());
				v = new SimpleValue(d);
			} catch (DateTimeParseException e) {
				// il formato della data non � quello considerato di default quindi la considero
				// come una stringa
				v = new SimpleValue(value.asText());
			}
			break;
		case "float":
			BigDecimal bd = new BigDecimal(value.asDouble());
			v = new SimpleValue(bd);
			break;
		case "boolean":
			v = new SimpleValue(value.asBoolean());
			break;
		}
		return v;
	}

	private Map<String, String> readMappings(String mappings) throws IOException {
		JsonFactory f = new MappingJsonFactory();
		JsonParser jp = f.createParser(mappings);
		JsonToken current = jp.nextToken();

		// il documento JSON deve iniziare con {
		if (current != JsonToken.START_OBJECT) {
			System.out.println("Error: root should be object: quiting.");
			return null;
		}

		Map<String, String> result = new TreeMap<>();

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			current = jp.nextToken();
			JsonNode node = jp.readValueAsTree();
			// JsonNode map = node.get("mappings").get(type);
			JsonNode properties = node.get("mappings").get(type).get("properties");
			result = readProperties(properties);
		}
		return result;
	}

	private Map<String, String> readProperties(JsonNode properties) {
		Map<String, String> result = new TreeMap<>();
		Iterator<String> fields = properties.fieldNames();

		for (int i = 0; i < properties.size(); i++) {
			String fieldName = fields.next();

			if (properties.get(fieldName).get("type") != null)
				result.put(fieldName, properties.get(fieldName).get("type").asText());
			else if (properties.get(fieldName).get("properties") != null) {
				result.put(fieldName, "document");
				Map<String, String> temp = readProperties(properties.get(fieldName).get("properties"));
				result.putAll(temp);
			}
		}
		return result;
	}
}
