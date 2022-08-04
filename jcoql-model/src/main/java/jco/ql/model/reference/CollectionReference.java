package jco.ql.model.reference;

import jco.ql.parser.model.util.DbCollection;

public class CollectionReference {
	
	private String databaseName;	
	private String collectionName;
	private String alias;
	
	
	// PF. 09.03.2022
	public CollectionReference (DbCollection dbc) {
		databaseName = dbc.db;
		collectionName = dbc.collection;
		if (dbc.hasAlias())
			alias = dbc.alias;
		else
			alias = dbc.collection;
	}

	public CollectionReference(String databaseName, String collectionName) {
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.alias = collectionName;
	}

	public CollectionReference(String databaseName, String collectionName, String alias) {
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.alias = alias;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public String getAlias() {
		return alias;
	}
	
}
