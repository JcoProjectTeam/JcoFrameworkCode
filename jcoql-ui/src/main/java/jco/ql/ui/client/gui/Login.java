package jco.ql.ui.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import jco.ql.ui.client.Client;

public class Login extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField txtServerAddress;
	private JTextField txtPortNumber;

	public Login(Client c) {
		setVisible(true);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(550, 440);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtServerAddress = new JTextField("localhost");
		txtServerAddress.setBackground(Color.WHITE);
		txtServerAddress.setForeground(Color.BLACK);
		txtServerAddress.setFont(new Font("Tahoma", Font.PLAIN, 17));
		txtServerAddress.setBounds(147, 127, 362, 25);
		contentPane.add(txtServerAddress);
		txtServerAddress.setColumns(10);
		
		JLabel lblServerAddress = new JLabel("Server address:");
		lblServerAddress.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblServerAddress.setBounds(12, 128, 123, 22);
		contentPane.add(lblServerAddress);
		
		JLabel lblPort = new JLabel("Port number:");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblPort.setBounds(12, 200, 123, 16);
		contentPane.add(lblPort);
		
		txtPortNumber = new JTextField("44444");
		txtPortNumber.setFont(new Font("Tahoma", Font.PLAIN, 17));
		txtPortNumber.setBounds(147, 196, 116, 25);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean result;
				try {
					result = c.connect(txtServerAddress.getText(), Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException err) {
					final JPanel panel = new JPanel();
				    JOptionPane.showMessageDialog(panel, "Invalid port number","Error", JOptionPane.ERROR_MESSAGE);
					result = false;
				}
				if(result) 
					dispose();
				
			}
		});
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnConnect.setBounds(200, 281, 123, 40);
		contentPane.add(btnConnect);
		
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	
	// PF. Added on 01.08.2021 just to have a feedback
	public long getSerial () {
		return serialVersionUID;
	}
}
