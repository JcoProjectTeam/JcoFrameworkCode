package jco.ql.engine.serverweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.context.annotation.ComponentScan;

import jco.ql.engine.parser.ParserLauncher;
import jco.ql.model.engine.IDocumentCollection;



@ComponentScan(basePackages = "jco.ql.engine")
	public class ServerWeb extends WebSocketServer{

	    private Hashtable<WebSocket,ParserLauncher> dictWS;
	    protected ServerMessagesWeb msg;
	    
	      public ServerWeb(int port) throws IOException {
	        super(new InetSocketAddress(port));
	        //p = new Parser();
	        dictWS=new Hashtable<>();
	        msg = new ServerMessagesWeb();
	      }
	    
	      public ServerWeb(InetSocketAddress address) throws IOException {
	        super(address);
	        //p = new Parser();
	        msg = new ServerMessagesWeb();
	      }
	    
	      public ServerWeb(int port, Draft_6455 draft) throws IOException {
	        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
	        //p = new Parser();
	        msg = new ServerMessagesWeb();
	      }
	    
	      @Override
	      public void onOpen(WebSocket conn, ClientHandshake handshake) {
	    	ParserLauncher p=null;
			try {
				p = new ParserLauncher();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(p!=null) {
				dictWS.put(conn, p);
				conn.send(msg.getMsgServerConf(p.getConfigurations())); //This method sends a message to the new client
				System.out.println(
						conn + " entered the room with IP: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() +" !");
				System.out.println(
						handshake);
			}
	      }
	    
	      @Override
	      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
	        //broadcast(conn + " has left the room!");
	        System.out.println(conn + " has left the room for "+ reason+" !");
	        dictWS.remove(conn);
	      }
	    
	      @Override
	      public void onMessage(WebSocket conn, String message) {
	        decoder(message,conn);
	        System.out.println(conn + ": " + message);
	      }
	    
	      @Override
	      public void onMessage(WebSocket conn, ByteBuffer message) {
	        conn.send(message.array());
	        System.out.println(conn + ": " + message);
	      }
	    
	      
	      public static void main(String[] args) throws InterruptedException, IOException {
	    	  startWeb();
	            
	      }
	      
	      public static void startWeb() throws IOException {
	    	  	int port = 44445; // 843 flash policy port
		        ServerWeb s = new ServerWeb(port);
		        s.start();
		        System.out.println("Server started on port: " + s.getPort());
	      }
	    
	      @Override
	      public void onError(WebSocket conn, Exception ex) {
	        ex.printStackTrace();
	        if (conn != null) {
	          // some errors like port binding failed may not be assignable to a specific websocket
	        }
	      }
	    
	      @Override
	      public void onStart() {
	        System.out.println("Server started!");
	        setConnectionLostTimeout(0);
	        setConnectionLostTimeout(100);
	      }

	      private void decoder(String s, WebSocket conn) {
	  		BufferedReader br = new BufferedReader(new StringReader((String) /*jsonObj.get("msg")*/s));
	  		String text;
	  		try {
	  			text = br.readLine();
	  			if (text.equals("##BEGIN-PROCESS##")) {

	  				// togli inizio e fine del messaggio
	  				int firstindex = text.length();
	  				int lastindex = s.lastIndexOf("##END-PROCESS##");
	  				String istr = s.substring(firstindex, lastindex-1);

	  // PF - parser launching
	  				dictWS.get(conn).parse(istr);
	  				conn.send(msg.getMsgSuccessExecuteJCO());
	  				conn.send(msg.getMsgProcess(dictWS.get(conn).getProcess()));

	  			} else if (text.equals("##BACKTRACK##")) {
	  				dictWS.get(conn).backtrack();
	  				conn.send(msg.getAck());
	  				conn.send(msg.getMsgProcess(dictWS.get(conn).getProcess()));

	  			} else if (text.equals("##GET-TEMPORARY-COLLECTION##")) {
	  				IDocumentCollection collection = dictWS.get(conn).getTemporaryCollection();
	  				System.out.println(collection.getName());
	  				conn.send(msg.getMsgCollection(collection.getName(),collection.toString()));

	  			} else if (text.equals("##GET-PROCESS##")) {
	  				List<String> process = dictWS.get(conn).getProcess();
	  				conn.send(msg.getMsgProcess(process));

	  			} else if (text.equals("##GET-IR-LIST##")) {
	  				Collection<String> irlist = dictWS.get(conn).getIRList();
	  				conn.send(msg.getMsgIRList(irlist));

	  			} else if (text.equals("##GET-IR-COLLECTION##")) {
	  				String collectionName = br.readLine();
	  				IDocumentCollection collection = dictWS.get(conn).getIRCollection(collectionName);
	  				conn.send(msg.getMsgCollection(collectionName,collection.toString()));

	  			} else if (text.equals("##ADD-SERVER-CONF##")) {
	  				int firstindex = text.length();
	  				int lastindex = s.lastIndexOf("##END-SERVER-CONF##");
	  				dictWS.get(conn).addServer(s.substring(firstindex, lastindex));
	  				conn.send(msg.getMsgServerConf(dictWS.get(conn).getConfigurations()));
	  			}

	  		} catch (Exception e) {
	  			conn.send(msg.getMsgErrorExecuteJCO(e.getMessage()));
	  			conn.send(msg.getMsgProcess(dictWS.get(conn).getProcess()));
	  		} finally {
	  			try {
	  				br.close();
	  			} catch (IOException e) {
	  				e.printStackTrace();
	  			}
	  		}
	  	}

}
