package jcoql.engine.postgresql;

import java.sql.Connection;
import org.json.JSONObject;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class PostgresDatabase implements IDatabase {
    private String dbName;
    private Connection connection;
    
    public PostgresDatabase(String url, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, password);
    }
	
    public PostgresDatabase(String fullUrl, String dbName) {
            try {
                this.connection = DriverManager.getConnection(fullUrl);
                System.out.println("✅ Connessione al nuovo database '" + dbName + "' riuscita!");
            } catch (SQLException ex) {
                System.err.println("❌ Errore durante la connessione al nuovo database: " + ex.getMessage());
            }
        }

    private void createDatabase(String adminUrl, String dbName) {
        try (Connection conn = DriverManager.getConnection(adminUrl);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE DATABASE " + dbName;
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("❌ Errore durante la creazione del database '" + dbName + "': " + e.getMessage());
        }
    }

	@Override
    public String getName() {
        return this.dbName;
    }
    
	@Override
	public IDocumentCollection getCollection(String collectionName) {
	    List<DocumentDefinition> docs = new ArrayList<>();
	    boolean hasJsonDoc = false;

	    // 1️⃣ Controllo se esiste la colonna json_doc
	    try {
	        ResultSet rsCol = connection.getMetaData()
	            .getColumns(null, null, collectionName, "json_doc");
	        hasJsonDoc = rsCol.next();
	        rsCol.close();
	    } catch (SQLException e) {
	        // ignoro, poi lo gestisco più avanti
	    }

	    String sql;
	    if (hasJsonDoc) {
	        sql = "SELECT json_doc AS data FROM " + collectionName;
	    } else {
	        sql = "SELECT row_to_json(t) AS data FROM " + collectionName + " t";
	    }

	    ObjectMapper om = new ObjectMapper();
	    try (PreparedStatement ps = connection.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            String json = rs.getString("data");
	            @SuppressWarnings("unchecked")
	            Map<String,Object> map = om.readValue(json, Map.class);
	            docs.add(reconstructDocument(map));
	        }
	    } catch (Exception e) {
	        System.err.println("❌ Errore getCollection(" + collectionName + "): " + e.getMessage());
	    }
	    return new PostgresCollection(collectionName, docs);
	}
	
	private DocumentDefinition reconstructDocument(Map<String, Object> jsonMap) {
	    DocumentDefinition doc = new DocumentDefinition();

	    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
	        JCOValue value = inferJCOQLValue(entry.getValue());
	        FieldDefinition field = new FieldDefinition(entry.getKey(), value);
	        doc.addField(field);
	    }

	    return doc;
	}

	private JCOValue inferJCOQLValue(Object value) {
	    if (value instanceof Integer) {
	        return new SimpleValue((Integer) value); 
	    } else if (value instanceof Double) {
	        return new SimpleValue((Double) value);
	    } else if (value instanceof Boolean) {
	        return new SimpleValue((Boolean) value);
	    } else if (value instanceof String) {
	        return new SimpleValue((String) value);
	    } else if (value instanceof Map) {
	        return new DocumentValue(reconstructDocument((Map<String, Object>) value));
	    } else if (value instanceof List) {
	        List<JCOValue> arrayValues = ((List<?>) value).stream()
	            .map(this::inferJCOQLValue)
	            .collect(Collectors.toList());
	        return new ArrayValue(arrayValues);
	    } else if (value == null) {
	        return new SimpleValue(null, EValueType.NULL);
	    } else {
	        System.err.println("⚠️ Tipo non gestito: " + value.getClass().getSimpleName());
	        return new SimpleValue(value.toString());
	    }
	}

	public void addCollection(IDocumentCollection collection) {
        addCollection(collection, collection.getName());
    }

	public void addCollection(IDocumentCollection collection, String collectionName) {
	    try (Statement stmt = connection.createStatement()) {
	        if (collectionExists(collectionName, connection)) {
	            System.out.println("Collezione " + collectionName + " già esistente. Verifico vincoli...");
	            dropCollection(collectionName, connection);
	        }

	        createCollection(collectionName, connection);
	        insertDocuments(collection, collectionName, connection);

	    } catch (SQLException e) {
	        System.err.println("❌ Errore nella gestione della collezione: " + e.getMessage());
	    }
	}

	private void createCollection(String collectionName, Connection connection) throws SQLException {
	    String sql = "CREATE TABLE " + collectionName + " (id SERIAL PRIMARY KEY, json_doc JSONB NOT NULL)";
	    try (Statement stmt = connection.createStatement()) {
	        stmt.executeUpdate(sql);
	        System.out.println("✅ Tabella " + collectionName + " creata con successo.");
	    }
	}
	
	private void insertDocuments(IDocumentCollection collection,
            String tableName,
            Connection connection) throws SQLException {
		// ora uso tableName, non collection.getName()
			String sql = "INSERT INTO " + tableName + " (json_doc) VALUES (?::jsonb)";
			ObjectMapper objectMapper = new ObjectMapper();
			try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
				for (DocumentDefinition document : collection.getDocumentList()) {	
					Map<String,Object> clean = cleanJCOQLMetadata(document);
					String jsonString = objectMapper.writeValueAsString(clean);	
					pstmt.setString(1, jsonString);
					pstmt.executeUpdate();
					System.out.println("📥 Documento inserito in " + tableName);
				}
			} catch (JsonProcessingException e) {
				System.err.println("❌ Errore serializzazione JSON: " + e.getMessage());
			}
		}
	
	private Map<String, Object> cleanJCOQLMetadata(DocumentDefinition document) {
	    Map<String, Object> result = new HashMap<>();
	    
	    for (FieldDefinition field : document.getFields()) {
	        JCOValue value = field.getValue();

	        if (value instanceof SimpleValue) {
	            result.put(field.getName(), ((SimpleValue) value).getValue()); 
	        } else if (value instanceof DocumentValue) {
	            result.put(field.getName(), cleanJCOQLMetadata(((DocumentValue) value).getDocument()));
	        } else if (value instanceof ArrayValue) {
	            List<Object> array = new ArrayList<>();
	            for (JCOValue item : ((ArrayValue) value).getValues()) {
	                if (item instanceof SimpleValue) {
	                    array.add(((SimpleValue) item).getValue());
	                } else {
	                    array.add(item);
	                }
	            }
	            result.put(field.getName(), array);
	        }
	    }
	    
	    return result;
	}


    private boolean collectionExists(String collectionName, Connection connection) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, collectionName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean(1);
        }
    }

    private void dropCollection(String collectionName, Connection connection) throws SQLException {
        // 1️⃣ Controlla se ci sono vincoli di chiave esterna
        String checkFkConstraints = "SELECT conname FROM pg_constraint " +
                "WHERE confrelid = (SELECT oid FROM pg_class WHERE relname = ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(checkFkConstraints)) {
            pstmt.setString(1, collectionName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { // Se trova vincoli
                System.err.println("❌ Non posso eliminare la tabella " + collectionName + " perché ha vincoli relazionali.");
                return; // ❌ Esce dalla funzione senza eliminare la tabella
            }
        }

        // 2️⃣ Se non ci sono vincoli, elimina la tabella
        String sql = "DROP TABLE IF EXISTS " + collectionName;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabella " + collectionName + " eliminata con successo.");
        }
    }

}

