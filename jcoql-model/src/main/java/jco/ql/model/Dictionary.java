package jco.ql.model;

import java.util.Map;
import java.util.TreeMap;

public class Dictionary {

	private Map<String, String> dictionary;	
	private String name;
	
	public Dictionary(String name) {
		this.name = name;
		dictionary = new TreeMap <String, String> ();		
	}

	public String getName() {
		return name;
	}

	public void put(String key, String value) {
		dictionary.put(key, value);
	}

	public String get(String key) {
		if (key != null && dictionary.containsKey(key))
			return dictionary.get(key);
		return null;
	}
	public String toString() {
		String st = "Dictionary:\t" + name + "\n";
		for(Map.Entry<String,String> entry : dictionary.entrySet()) {
			  String key = entry.getKey();
			  String value = entry.getValue();

			  st += "\t" + key + "\t=>\t" + value +"\n";
			}		
		return st;
	}
}
