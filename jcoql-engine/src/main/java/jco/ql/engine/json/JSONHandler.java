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
import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.SimpleDocumentCollection;

import org.bson.json.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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
	 * @param collection
	 * @param alias
	 * @return il path del file temporaneo
	 */
	public String saveToTemporary(IDocumentCollection collection, String alias) {
		/*
		 * System.getProperty("java.io.tmpdir") --> restituisce il path della cartella
		 * dei file temporanei
		 */
		Date today = new Date();
		String fileName = EngineConfiguration.getTempDirectory();
		if (fileName.endsWith(File.separator))
			fileName += alias + "_" + today.getTime();
		else
			fileName += File.separator + alias + "_" + today.getTime();
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

	/**
	 * 2026.01.10 Zun
	 * @param collection
	 * @param filePathName
	 */
	public void saveToFile(IDocumentCollection collection, String filePathName) throws IOException {
		File file = new File(filePathName);
		file.createNewFile();

		FileWriter fw = new FileWriter(file);
		BufferedWriter b = new BufferedWriter(fw);
		
		b.write("[");
		b.newLine();
		int nDoc = 0;
		for (DocumentDefinition c : collection.getDocumentList()) {
			if (nDoc > 0) {
				b.write(", ");				
				b.newLine();				
			}
			b.write(DocumentUtils.bsonFromDocumentDefinition(c).toJson());
			nDoc++;
		}
		b.newLine();
		b.write("]");
		b.close();
	}

	
	/**
	 * Read a collection from the temporary 
	 * */
	public IDocumentCollection readCollectionFromTemporary(String alias, String fileName) {
		List<DocumentDefinition> list = new ArrayList<>();
		if(fileName == null)
			throw new ExecuteProcessException("[GET COLLECTION]: collection " + alias + " does not exist");
		try {
			// Open file, read every line, create a document per line
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

	
	/**
	 * Read a collection from the file 
	 * @throws IOException 
	 * */
	public IDocumentCollection readCollectionFromFile(String alias, String filePathName) throws IOException {
		List<DocumentDefinition> list = new ArrayList<>();
		List<Document> listFromFile = readJsonDocuments(filePathName);
		for (Document d : listFromFile) 
			list.add(DocumentUtils.mapDocumentDefinitionFromBson(d));
		
		return new SimpleDocumentCollection(alias, list);
	}


	List<Document> readJsonDocuments(String filePathName) throws IOException {
	    if (filePathName == null || filePathName.isEmpty()) {
	        throw new IllegalArgumentException("File path is null or empty");
	    }

	    Path path = Paths.get(filePathName.replace('\\', '/')).normalize();
	    if (!Files.exists(path)) {
	        throw new IOException("File not found: " + path);
	    }
	    if (!Files.isRegularFile(path)) {
	        throw new IOException("Path does not refer to a regular file: " + path);
	    }
	    if (!Files.isReadable(path)) {
	        throw new IOException("File is not readable: " + path);
	    }

	    String json;
	    try {
	        byte[] bytes = Files.readAllBytes(path);
	        json = new String(bytes, StandardCharsets.UTF_8).trim();
	    } catch (IOException e) {
	        throw new IOException("Failed to read file content", e);
	    }

	    if (json.isEmpty()) {
	        throw new IllegalArgumentException("JSON file is empty");
	    }

	    List<Document> documents = new ArrayList<Document>();

	    try {
	        // Case 1: array of documents
	        if (json.charAt(0) == '[') {
	            Document wrapper = Document.parse("{\"_tmp\":" + json + "}");
	            Object value = wrapper.get("_tmp");

	            if (!(value instanceof List)) {
	                throw new IllegalArgumentException("Top-level JSON array is invalid");
	            }

	            List<?> list = (List<?>) value;

	            for (int i = 0; i < list.size(); i++) {
	                Object o = list.get(i);
	                if (!(o instanceof Document)) {
	                    throw new IllegalArgumentException(
	                            "JSON array contains a non-document element at index " + i
	                    );
	                }
	                documents.add((Document) o);
	            }
	            return documents;
	        }

	        // Case 2: single document
	        Document doc = Document.parse(json);
	        documents.add(doc);
	        return documents;

	    } catch (JsonParseException e) {
	        throw new IllegalArgumentException("Invalid JSON content: " + e.getMessage(), e);
	    }
	}
	
	
}
