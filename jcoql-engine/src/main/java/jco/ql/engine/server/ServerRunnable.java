package jco.ql.engine.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.pattern.IntegerPatternConverter;

import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.parser.ParserLauncher;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;

public class ServerRunnable implements Runnable {

	protected Socket clientSocket;
	private DataInputStream din;
	private DataOutputStream dout;
	private ParserLauncher pl;
	protected ServerMessages msg;

	public ServerRunnable(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		pl = new ParserLauncher();
		msg = new ServerMessages();
	}

	@Override
	public void run() {

		try {
			din = new DataInputStream(clientSocket.getInputStream());
			dout = new DataOutputStream(clientSocket.getOutputStream());

			// invio al client la lista dei server disponibili in formato JSON
			sendMessage(msg.getMsgServerConf(pl.getConfigurations()));

			while (true) {
				if (din.available() > 0) {
					// leggo la prima riga
					decoder(din.readUTF());

				} else {
					Thread.sleep(500);
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}


	private void decoder(String s) {
		BufferedReader br = new BufferedReader(new StringReader(s));
		String text;
		try {
			text = br.readLine();
			if (text.equals("##BEGIN-PROCESS##")) {

				// togli inizio e fine del messaggio
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-PROCESS##");
				String istr = s.substring(firstindex, lastindex-1);

// PF - parser launching
				pl.parse(istr);
// se arrivo qui non ci sono stati errori nell'esecuzione
  				if (JMH.reportNoErrors()) {
  					sendMessage(msg.getMsgSuccessExecuteJCO());
	  				sendMessage(msg.getMsgProcess(pl.getProcess()));
	  				if (JMH.getChannel(JMH.MAIN_CHANNEL).size() > 0)
  						sendMessage(msg.getWarningMsg(JMH.getChannel(JMH.MAIN_CHANNEL)));
	  				if (JMH.getChannel(JMH.IO_CHANNEL).size() > 0)
  						sendMessage(msg.getIOMsg(JMH.getChannel(JMH.IO_CHANNEL)));
  				}
  				else {
	  				if (JMH.getChannel(JMH.PARSER_CHANNEL).size() > 0)
  						sendMessage(msg.getParserMsg(JMH.getChannel(JMH.PARSER_CHANNEL)));
  				}
			} 
			else if (text.equals("##BACKTRACK##")) {
				pl.backtrack();
				// se arrivo qui non ci sono stati errori nell'esecuzione
				sendMessage(msg.getAck());
				sendMessage(msg.getMsgProcess(pl.getProcess()));

			} 
			else if (text.equals("##GET-TEMPORARY-COLLECTION##")) {
				IDocumentCollection collection = pl.getTemporaryCollection();
				sendMessage(msg.getMsgCollection(collection.toString()));

			} 
			else if (text.equals("##GET-PROCESS##")) {
				List<String> process = pl.getProcess();
				sendMessage(msg.getMsgProcess(process));

			} 
			else if (text.equals("##GET-IR-LIST##")) {
				Collection<String> irlist = pl.getIRList();
				sendMessage(msg.getMsgIRList(irlist));

			} 
			else if (text.equals("##GET-IR-COLLECTION##")) {
				String collectionName = br.readLine();
				IDocumentCollection collection = pl.getIRCollection(collectionName);
				sendMessage(msg.getMsgCollection(collection.toString()));

			} 
			else if (text.equals("##ADD-SERVER-CONF##")) {
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-SERVER-CONF##");
				pl.addServer(s.substring(firstindex, lastindex));
				// rispondo con la nuova lista dei server disponibili
				sendMessage(msg.getMsgServerConf(pl.getConfigurations()));
			} 
			else if (text.equals("##GET-SETTINGS##")) {
				sendMessage(msg.getSettingConfiguration());
			} 
			else if (text.equals("##BEGING-SETTINGS##")) {
				String setting = br.readLine();
				String value = br.readLine();
				System.out.println(text);
				System.out.println(setting);
				System.out.println(value);
				if ("Processors:".equals(setting)) 
					EngineConfiguration.setNProcessors(Integer.parseInt(value));
				else if ("Tracker:".equals(setting)) 
					EngineConfiguration.setTrackTimes("true".equals(value));
				else if ("Spatial Index::".equals(setting)) 
					EngineConfiguration.setSpatialIndexing("true".equals(value));					
				else if ("Backtrack:".equals(setting)) 
					EngineConfiguration.setBacktrack("true".equals(value));					
				else if ("Msg in Docs:".equals(setting)) 
					EngineConfiguration.setBacktrack("true".equals(value));					
				else if ("Remove MongoDb Id:".equals(setting)) 
					EngineConfiguration.setRemoveMondgoId("true".equals(value));					
			}
			else {				
				System.out.println(text);
				while ((text=br.readLine())!= null)
					System.out.println(text);
			}

		} catch (Exception e) {
			sendMessage(msg.getMsgErrorExecuteJCO(e.getMessage()));
			sendMessage(msg.getMsgProcess(pl.getProcess()));
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(String msg) {
		try {
			byte[] data = msg.getBytes("UTF-8");
			dout.writeInt(data.length);
			dout.write(data);

			//dout.writeUTF(msg);
			dout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
