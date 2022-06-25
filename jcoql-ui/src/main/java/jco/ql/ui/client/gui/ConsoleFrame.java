package jco.ql.ui.client.gui;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4410776370319618594L;

	private JPanel contentPane;
	private JTextArea textArea;

	public ConsoleFrame() {
		setVisible(false);
		setTitle("Console");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(750, 400);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(12, 13, 708, 327);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textArea.setEditable(false);
	}

	public void addText(String text) {
    	SimpleDateFormat sdf = new SimpleDateFormat(); // creo l'oggetto
    	sdf.applyPattern("dd-MM-yy-HH.mm.ss SSS");
    	String dataStr = sdf.format(new Date()); // data corrente (20 febbraio 2014
    //	org.joda.time.DateTime date = org.joda.time.DateTime.now();
		textArea.setText(textArea.getText() + dataStr + " :P: "+ text + "\n");
//		textArea.setText(textArea.getText() + date.toString("yyyy-MM-dd HH:mm:SS") + text + "\n");
	}

}
