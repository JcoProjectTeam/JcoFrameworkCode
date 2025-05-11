package jcoql.test.engine.postgresql;

import jco.ql.model.engine.IDocumentCollection;
import jcoql.engine.postgresql.PostgresDatabase;
import jcoql.engine.postgresql.PostgresCollection;

import java.sql.SQLException;

//test per la gestione delle tabelle di postgres json e non
public class PostgresDatabaseTest {
    public static void main(String[] args) {
        final String DB_URL = "jdbc:postgresql://localhost:5432/testdb";
        final String DB_USER = "postgres";
        final String DB_PASSWORD = "Matteo123";

        try {
            // Connessione al database
            PostgresDatabase db = new PostgresDatabase(DB_URL, DB_USER, DB_PASSWORD);

            // Prendo la tabella JSON-storage preesistente "collezione_1"
            IDocumentCollection col1 = db.getCollection("collezione_1");
            System.out.println(col1);
            // e la salvo come "collezione_finale1"
            db.addCollection(col1, "collezione_finale1");

            // Prendo la tabella relazionale "studentissimi"
            IDocumentCollection col2 = db.getCollection("collezione_finale2");
            System.out.println(col2);
            // e la salvo in JSON-storage come "collezione_finale2"
            db.addCollection(col2, "collezione_finale3");

            // 4Verifico i contenuti delle due nuove tabelle
            System.out.println("=== collezione_finale1 ===");
            PostgresCollection got1 = (PostgresCollection) db.getCollection("collezione_finale1");
            System.out.println(got1);

            System.out.println("=== collezione_finale2 ===");
            PostgresCollection got2 = (PostgresCollection) db.getCollection("collezione_finale3");
            System.out.println(got2);
        } catch (SQLException e) {
            System.err.println("Errore di connessione o query: " + e.getMessage());
        }
    }
}