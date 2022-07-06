package jco.ql.db.ds.server.collectionsDescriptor;

import java.util.ArrayList;
import java.util.List;

//This is the class which the JSON document is converted into
//Its attributes are the name of the database associated with the JSON document, 
//and a list of subclasses that represent collections of the database

public class CollectionsDescriptor {

	public String databaseName;
	
    public List<collectionDescriptor> collections = new ArrayList<collectionDescriptor>();

    public List<collectionDescriptor> getCollections() {
        return collections;
    }
    
    public String getdatabaseName() {
    	return databaseName;
    }
    
    public void setdatabaseName(String databaseName) {
    	this.databaseName = databaseName;
    }


    public void setCollections(List<collectionDescriptor> collections) {
        this.collections = collections;
    }
    
}
