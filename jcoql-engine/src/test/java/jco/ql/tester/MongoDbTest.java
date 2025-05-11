package jco.ql.tester;

import jco.ql.db.mongodb.MongoDbDatabase;
import jco.ql.model.engine.IDocumentCollection;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class MongoDbTest{
	
	public static String caricaFileJson(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
	
	public static void main(String[] args) {
	    
	    //test getCollection con collezione esistente
	    MongoDbDatabase database = new MongoDbDatabase("localhost", 27018,"PW25");
	    // Verifica se la collezione esiste
	    IDocumentCollection coll = database.getCollection("Sensors");
	    if (coll != null) {
	        System.out.println("Collezione trovata: " + coll.getName());
	        System.out.println(coll);
	    } else {
	        System.out.println("Collezione non trovata.");
	    }
	    database.addCollection(coll,"collezione6");	    

	}
}

	