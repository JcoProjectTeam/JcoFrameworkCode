package jco.ql.db.ds.server.collectionsDescriptor;

import java.util.ArrayList;

//this class describes a single collection inside the JSON document.
//Its attributes are the name of the collection, the type which can be static,virtual or dynamic, and the list of Url
//that describes a virtual o dynamic collection.
//Url is a subclass.

import java.util.List;

import jco.ql.model.engine.JMH;

public class collectionDescriptor {
	private String name;
	private String type;
	protected List<Url> urls = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public List<Url> getUrl() {
		return urls;
	}
	
	public void setUrl(List<Url> url) {
		this.urls = url;
	}
	  
	public void initializeUrl(){
		this.urls = new ArrayList<Url>();
	}
	  
	public void addUrl(Url url) {
		if(this.urls == null)
			initializeUrl();
		this.urls.add(url);
	}
	  
	public void removeUrl(int index) {
		if((index+1) > this.urls.size())
			JMH.addJCOMessage("Generic error on Collection description: index out of bounds");
		else
			this.urls.set(index,null);
	}
   
}
