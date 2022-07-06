package jco.ql.db.ds.server.collectionsDescriptor;

import java.util.List;


//This class is an extention of "CollectionsDescriptor" which contains all the method to manage the list of "collectionDescriptor", 
//the subclass that describe a single collection

public class CollectionsDescriptorManager extends CollectionsDescriptor{

	 public List<collectionDescriptor> getAllCollections() {
        return collections;
    }

    public collectionDescriptor getCollection(String collectionName)
    {
// PF. 2021.10.20 sembrano inutili	    	
//	        int index = 0;
//	        boolean exist = false;

        
    	for(collectionDescriptor k: collections) {
    		if(k != null) 
    			if(k.getName().equals(collectionName)) 
	                return k;
        }
    	return null;
    }

    
    public void setCollections(List<collectionDescriptor> collections) {
        this.collections = collections;
    }
	
    
    public void addCollection(collectionDescriptor collection)  {  
    	collections.add(collection);  
    }
    
    public void removeCollection(String collectionName){
        int index = 0;
        boolean exist = false;
        
        for(collectionDescriptor k: collections) {
        	if(k != null) {
				if(k.getName().equals(collectionName)) {
					exist = true;
					break;
				}
    		}
            index++;
        }
        
        if(exist)
            collections.set(index, null);
    }
	
}

