package jco.ql.engine.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import jco.ql.db.mongodb.utils.DocumentUtils;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.SimpleDocumentCollection;

/**
 * La classe si occupa della serializzazione di una classe Java in un documento
 * JSON e della deserializzazione del documento JSON in una classe Java.
 *
 * @author Savino Lechiancole
 *
 */

public class JSONHandler {

	public JSONHandler() {}

	/**
	 *
	 * @param collection
	 * @param alias
	 * @return il path del file temporaneo
	 */
	public String createFile(IDocumentCollection collection, String alias) {
		/*
		 * System.getProperty("java.io.tmpdir") --> restituisce il path della cartella
		 * dei file temporanei
		 */
		Date today = new Date();
		String fileName = System.getProperty("java.io.tmpdir");
		if (fileName.endsWith(File.separator))
			fileName = System.getProperty("java.io.tmpdir")  + alias + "_" + today.getTime();
		else
			fileName = System.getProperty("java.io.tmpdir")  + File.separator + alias + "_" + today.getTime();
		String suffix = ".tmp";

		try {

			File file = new File(fileName + suffix);
			file.createNewFile();
			file.deleteOnExit();

			FileWriter fw = new FileWriter(file);
			BufferedWriter b = new BufferedWriter(fw);

			for (DocumentDefinition c : collection.getDocumentList()) {
				b.write(DocumentUtils.bsonFromDocumentDefinition(c).toJson());
				b.newLine();
			}
			b.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName + suffix;
	}




	public IDocumentCollection createCollection(String alias, String fileName) {
		List<DocumentDefinition> list = new ArrayList<>();
		if(fileName == null)
			throw new ExecuteProcessException("[GET COLLECTION]: collection " + alias + " does not exist");
		try {
			// Apro il file, leggo ogni riga e per ognuna creo un documento
			FileReader fr = new FileReader(new File(fileName));
			BufferedReader br = new BufferedReader(fr);

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				Document bson = Document.parse(sCurrentLine);
				list.add(DocumentUtils.mapDocumentDefinitionFromBson(bson));
			}
			br.close();
			fr.close();



		} catch (Exception e) {
			e.printStackTrace();
		}

		return new SimpleDocumentCollection(alias, list);
	}
/*
	private DocumentDefinition createDocument(String input) throws JsonParseException, IOException {

		JsonFactory f = new MappingJsonFactory();
		JsonParser jp = f.createParser(input);
		JsonToken current = jp.nextToken();

		// il documento JSON deve iniziare con {
		if (current != JsonToken.START_OBJECT) {
			return null;
		}

//		String docName = null;
		List<FieldDefinition> list = new ArrayList<>();

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			current = jp.nextToken();

			if (fieldName.equals("name")) {
				JsonNode n = jp.readValueAsTree();
//				if (n != null)
//					docName = n.asText();
			} else if (fieldName.equals("fields")) {
				JsonNode node = jp.readValueAsTree();

				for (int i = 0; i < node.size(); i++) {
					JsonNode name = node.get(i).get("name");
					JsonNode value = node.get(i).get("value");

					Value v = new SimpleValue();

					if (value.get("type").asText().equals("DOCUMENT"))
						v = new DocumentValue(createDocument(value.get("value").toString()));
					else if (value.get("type").asText().equals("ARRAY")) {
						v = new ArrayValue(createArray(value.get("value")));
					}
					else
						v = getValue(value.get("type").asText(), value);

					/*
					 * switch (value.get("type").asText()) { case "STRING": v = new
					 * SimpleValue(value.get("value").asText()); break; case "INTEGER": v = new
					 * SimpleValue(value.get("value").asLong()); break; case "DATE": Date d = new
					 * Date(value.get("value").asLong()); v = new SimpleValue(d); break; case
					 * "DECIMAL": BigDecimal bd = new BigDecimal(value.get("value").asDouble()); v =
					 * new SimpleValue(bd); break; case "DOCUMENT": System.out.println("VALUE= " +
					 * value.get("value")); v = new
					 * DocumentValue(createDocument(value.get("value").toString())); break; }
					 * /
					// NOTA: se si utilizza toString() il campo contiene anche le ""
					// quindi ad esempio se � presente un campo username con toString() risulta
					// "username" e quindi nel documento viene salvato "username" invece di
					// username
					FieldDefinition field = new FieldDefinition(name.asText(), v);
					list.add(field);
				}
			} else {
				jp.skipChildren();
			}

		}
		return new DocumentDefinition(list);
	}
*/
/*
	private List<Value> createArray(JsonNode value){
		List<Value> list = new ArrayList<>();

		for (int i = 0; i < value.size(); i++) {
			Value v = getValue(value.get(i).get("type").asText(), value.get(i));
			list.add(v);
		}
		return list;
	}
*/
/*
	private Value getValue(String type, JsonNode value) {
		Value v = null;
		switch (type) {
			case "STRING":
				v = new SimpleValue(value.get("value").asText());
				break;
			case "INTEGER":
				v = new SimpleValue(value.get("value").asLong());
				break;
			case "DATE":
				Date d = new Date(value.get("value").asLong());
				v = new SimpleValue(d);
				break;
			case "DECIMAL":
				BigDecimal bd = new BigDecimal(value.get("value").asDouble());
				v = new SimpleValue(bd);
				break;
			case "BOOLEAN":
				v = new SimpleValue(value.get("value").asBoolean());
				break;
		}
		return v;
	}

*/
}
