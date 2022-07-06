package jco.ql.db.ds.server.collectionsDescriptor;

//this class describes a single Url

public class Url {
private String url;
private Integer frequency;
private Integer updateType;
  
  
	public Url(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	  
	public Integer getFrequency() {
		return frequency;
	}
	
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	  
	public int getUpdateType() {
		return updateType;
	}
	
	public void setUpdateType(int updateType) {
		  this.updateType = updateType;
	}

}
