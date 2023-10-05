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
}
