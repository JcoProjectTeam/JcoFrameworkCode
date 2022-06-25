package jco.ql.engine;

public class ServerConfiguration {
	
	
	private String server;
	private String host;
	private int port;
	private String type;
	private boolean def;
	
	public ServerConfiguration(String s, String h, int p, String t, boolean d) {
		server = s;
		host = h;
		port = p;
		type = t;
		def = d;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDefault() {
		return def;
	}

	public void setDefault(boolean def) {
		this.def = def;
	}
	
	
	@Override
	public String toString() {
		return "Server: " + getServer() + "\n" +
				"Host: " + getHost() + "\n" +
				"Port: " + getPort() + "\n" +
				"Type: " + getType() + "\n" +
				"Default: " + isDefault();
	}
}
