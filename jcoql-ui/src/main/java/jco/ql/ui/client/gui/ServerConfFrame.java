package jco.ql.ui.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import jco.ql.ui.client.Client;

public class ServerConfFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4801696697771685340L;

	private JPanel contentPane;
	private JTextField txtFileName;
	private JTextArea textArea;
	private JLabel lblNumberOfServer;
	private Client client;

	public ServerConfFrame(String serverConf, Client c) {
		this.client = c;
		createAndShowGUI();
		showConfigurations(serverConf);
	}
	
	public void createAndShowGUI() {
		setVisible(true);
		
		setTitle("Server configurations");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 600);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.GRAY);
		separator.setBounds(12, 433, 558, 2);
		contentPane.add(separator);

		JLabel lblAddServerConfiguration = new JLabel("Add server configuration");
		lblAddServerConfiguration.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblAddServerConfiguration.setBounds(12, 448, 198, 22);
		contentPane.add(lblAddServerConfiguration);

		JLabel lblNewLabel = new JLabel("File name:\r\n");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel.setBounds(12, 483, 91, 22);
		contentPane.add(lblNewLabel);


		JButton btnAdd = new JButton("Add");
		btnAdd.setEnabled(false);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.addServer(txtFileName.getText());
			}
		});
		btnAdd.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnAdd.setBounds(320, 482, 97, 25);
		contentPane.add(btnAdd);

		
		txtFileName = new JTextField();
		txtFileName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				btnAdd.setEnabled(true);
				if(txtFileName.getText().equals(""))
					btnAdd.setEnabled(false);
			}
		});
		txtFileName.setFont(new Font("Tahoma", Font.PLAIN, 18));
		txtFileName.setBounds(110, 483, 198, 22);
		contentPane.add(txtFileName);
		txtFileName.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 48, 558, 370);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 20));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblAvailableServerConfigurations = new JLabel("Available server configurations:");
		lblAvailableServerConfigurations.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblAvailableServerConfigurations.setBounds(12, 13, 261, 22);
		contentPane.add(lblAvailableServerConfigurations);

		lblNumberOfServer = new JLabel("");
		lblNumberOfServer.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNumberOfServer.setBounds(272, 12, 110, 24);
		contentPane.add(lblNumberOfServer);
	}

	public void showConfigurations(String serverConf) {
		textArea.setText("");
		try {

			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = f.createParser(serverConf);
			JsonToken current = jp.nextToken();

			// il documento JSON deve iniziare con {
			if (current != JsonToken.START_OBJECT) {
				System.out.println("Error: root should be object: quiting.");
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jp.getCurrentName();
				current = jp.nextToken();
				JsonNode node = jp.readValueAsTree();

				if (fieldName.equals("total")) {
					if (node.asInt() == 1)
						lblNumberOfServer.setText(node.asInt() + " element");
					else
						lblNumberOfServer.setText(node.asInt() + " elements");
				}else {
					for (int i = 0; i < node.size(); i++) {
						String text = textArea.getText();
						text = text + (i+1) + ")\n" + setTextAttributes(node.get(i)) + "\n";
						textArea.setText(text);
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String setTextAttributes(JsonNode node) {
		String server = "Server: " + node.get("server").asText() + "\n";
		String host = "Host: " + node.get("host").asText() + "\n";
		String port = "Port: " + node.get("port").asLong() + "\n";
		String server_type = "Server type: " + node.get("type").asText() + "\n";
		String def = "Default: " + node.get("default").asBoolean() + "\n";
		return server + host + port + server_type + def;
	}
}
