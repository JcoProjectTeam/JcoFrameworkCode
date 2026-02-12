package jco.ql.ui.client;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import jco.ql.ui.client.gui.Login;
import jco.ql.ui.client.gui.MainFrame;

public class Client {
	private Socket clientSocket;
	static DataInputStream din;
	static DataOutputStream dout;
	private ClientMessages clientMsg;
	private static MainFrame gui;
	private static String serverConf;

	
	public static void main(String[] args) {
		UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 17));
		UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 14));
		Locale.setDefault(Locale.ENGLISH);
		Client c = new Client();

		Login login = new Login(c);

		while(!c.clientSocket.isConnected()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// PF - added just to use login variable.
				System.out.println ("InterruptedException. Login serial:\t" + login.getSerial());
				e.printStackTrace();
			}
		}

		gui = new MainFrame(c);
		c.retrieveSettings();
//		gui.getSettings ();
		while (c.clientSocket.isConnected()) {
			try {
				if (din.available() > 0) {
					int length = din.readInt();
					byte[] data = new byte[length];
					din.readFully(data);
					String s = new String(data,"UTF-8");

					gui.printMessage(decoder(s));
					//gui.printMessage(decoder(din.readUTF()));
				} else {
					Thread.sleep(200);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		c.close();
	}



	
	
	public Client() {
		clientMsg = new ClientMessages();
		clientSocket = new Socket();
		serverConf = "";
	}

	
	public boolean connect(String hostname, int portNumber) {
		try {
			clientSocket = new Socket(hostname, portNumber);
			din = new DataInputStream(clientSocket.getInputStream());
			dout = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			final JPanel panel = new JPanel();
		    JOptionPane.showMessageDialog(panel, "Invalid host or port number","Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	
	public void executeJCO(String msg) {
		sendMessage(clientMsg.executeJCO(msg));
	}

	
	public void backtrack() {
		sendMessage(clientMsg.backtrack());
	}

	
	public void getTemporaryCollection() {
		sendMessage(clientMsg.getMsgTemporaryCollection());
	}

	
	public void getProcess() {
		sendMessage(clientMsg.getMsgGetProcess());
	}

	
	public void getIRList() {
		sendMessage(clientMsg.getMsgIRLIst());
	}

	
	public void getIRCollection(String collectionName) {
		sendMessage(clientMsg.getMsgIRCollection(collectionName));
	}

	
	public void addServer(String fileName) {
		sendMessage(clientMsg.getMsgAddServerConf(fileName));
	}

	
// --------------------- PF 2023.10.13 --- setting messages
	public void setNProcessors(String np) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_PROCESSORS, np));		
	}

	public void setTrackTime(String b) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_TRACKER, b));		
	}


	public void setSpatialIndex(String b) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_SPATIAL_INDEX, b));		
	}


	public void setBacktrack(String b) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_BACKTRACK, b));		
	}


	public void setStoreMsg(String b) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_MSG_IN_DOCS, b));		
	}


	public void setRemoveMongoId(String b) {
		sendMessage(clientMsg.getMsgSetSettings(ClientMessages.SET_MONGO_ID, b));		
	}

	public void retrieveSettings() {
		sendMessage(clientMsg.getMsgAllSettings());				
	}

// --------------------- PF 2023.10.13 --- setting messages - END

	// --------------------- PF 2024.05.05 --- setting messages
	public void resetJMHMessages() {
		sendMessage(clientMsg.getMsgResetJMH());						
	}

	
	
	
	
	public void sendMessage(String msg) {
		try {
			dout.writeUTF(msg);
			dout.flush();
			gui.printMessage(" >>> \n" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static String decoder(String s) {
		BufferedReader br = new BufferedReader(new StringReader(s));
		String text;
		String result = "";
		try {
			text = br.readLine();
			if (text.equals("##SUCCESS##")) {
				result = "Done.";
			} 
			else if (text.equals("##BEGIN-ERROR##")) {
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-ERROR##");
				result = s.substring(firstindex, lastindex);
				final JPanel panel = new JPanel();
			    JOptionPane.showMessageDialog(panel, result, "Error", JOptionPane.ERROR_MESSAGE);
			} 
			else if (text.equals("##ACK##")) {
				result = "Done.";
				gui.resetInstructionArea();
			} 
			else if (text.equals("##BEGIN-COLLECTION##")) {
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-COLLECTION##");
				String collection = s.substring(firstindex, lastindex);
//				gui.showCollection(collection);
				gui.getProcessStateFrame().createTree(collection);
				result = "Done.";
			} 
			else if (text.equals("##BEGIN-PROCESS##")) {
				//System.out.println("TEXT: \n" + s);
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-PROCESS##");
				if(lastindex != (firstindex + 1))
					result = s.substring(firstindex+1, lastindex-1);
				else
					result = "";
				gui.printInstruction(result);
			} 
			else if (text.equals("##BEGIN-IR-LIST##")) {
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-IR-LIST##");
				result = s.substring(firstindex, lastindex);
				gui.getProcessStateFrame().addElementToList(result);
			} 
			else if (text.equals("##BEGIN-SERVER-CONF##")) {
				int firstindex = text.length();
				int lastindex = s.lastIndexOf("##END-SERVER-CONF##");
				result = s.substring(firstindex, lastindex);
				if(!serverConf.equals("")) {
					serverConf = result;
					gui.getServerConfFrame().showConfigurations(serverConf);
				}
				else
					serverConf = result;
			}
			else if (text.equals("##BEGIN-SETTING-CONFIGURATIO##")) {
		        gui.manageSettings(s);	
			}
			else {
				System.out.println("XXXXXXX:\t"+text);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "  <<< \n" + s;
	}

	
	public String getServerConf() {
		return serverConf;
	}

	
	public void close() {
		try {
			clientSocket.close();
			din.close();
			dout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
