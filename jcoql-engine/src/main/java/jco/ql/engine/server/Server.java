package jco.ql.engine.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import jco.ql.engine.serverweb.ServerWeb;

public class Server {

	private static ServerSocket serverSocket;
	private static int portNumber = 44444;

	// PF. Launch server
	public static void main(String[] args) throws IOException {
		runServer();
	}

	
	private static void runServer() throws IOException {
		try {
			System.out.print("JCO Engine Server starting...");
			serverSocket = new ServerSocket(portNumber);
			System.out.println(" on port 44444");
			//serverSocket.setSoTimeout(400000);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		ServerWeb.startWeb();
		
		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();
				new Thread(new ServerRunnable(clientSocket)).start();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

	}

}
