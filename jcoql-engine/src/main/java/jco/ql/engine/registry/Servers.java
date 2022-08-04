package jco.ql.engine.registry;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import jco.ql.engine.ServerConfiguration;

@Service
public class Servers {
	
	private ArrayList<ServerConfiguration> list;
	
	public Servers() {
		list = new ArrayList<ServerConfiguration>();
	}
	
	public void addConfiguration(ServerConfiguration c) {
		list.add(c);
	}
	
	public ArrayList<ServerConfiguration> getConfigurations(){
		return list;
	}
}