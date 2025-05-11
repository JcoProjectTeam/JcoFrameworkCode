package jcoql.test.engine.postgresql;

import java.sql.SQLException;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.value.SimpleValue;
import jcoql.engine.postgresql.PostgresCollection;
import jcoql.engine.postgresql.PostgresDatabase;
public class PostgresDatabaseTest2 {
    public static void main(String[] args) {
        String DB_URL = "jdbc:postgresql://localhost:5432/testdb";
        String DB_USER = "postgres";
        String DB_PASSWORD = "Matteo123";

        try {
            PostgresDatabase database = new PostgresDatabase(DB_URL, DB_USER, DB_PASSWORD);
            PostgresCollection collection = new PostgresCollection("test_collection");

            DocumentDefinition doc = new DocumentDefinition();
            doc.addField(new FieldDefinition("name", new SimpleValue("Test User")));
            doc.addField(new FieldDefinition("age", new SimpleValue(30)));

            collection.addDocument(doc);
            database.addCollection(collection);

            IDocumentCollection retrievedCollection = database.getCollection("test_collection");
            System.out.println("Documenti recuperati: " + retrievedCollection.toString());

        } catch (SQLException e) {
            System.err.println("Errore nel test: " + e.getMessage());
        }
    }
}