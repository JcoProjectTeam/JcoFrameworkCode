package jco.ql.ui.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Element;

import jco.ql.ui.client.Client;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private Client client;
	private JTextArea instructionArea;
	private ConsoleFrame console;
	private ProcessStateFrame processFrame;
	private ServerConfFrame serverconf;

	public MainFrame(Client c) {
		this.client = c;
		console = new ConsoleFrame();
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		setVisible(true);
		setLocationRelativeTo(null);
		setTitle("J-CO-UI: User Interface");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane commandAreaScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		commandAreaScroll.setBounds(12, 360, 600, 280);
		contentPane.add(commandAreaScroll);

		JTextArea commandArea = new JTextArea();
		commandArea.setWrapStyleWord(true);
        commandArea.setLineWrap(true);
		commandAreaScroll.setViewportView(commandArea);
		// PF. Char size - orig:18
		commandArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		// PF. Added
		commandArea.setTabSize(2);
		commandArea.setText("//CommandArea");

		JButton btnExecute = new JButton("Execute");
		btnExecute.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnExecute.setBounds(632, 478, 109, 40);
		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.executeJCO(commandArea.getText());
			}
		});
		contentPane.add(btnExecute);

		JButton btnShowConsole = new JButton();
		btnShowConsole.setLayout(new BorderLayout());
		   JLabel lab1 = new JLabel("Show");
		   lab1.setHorizontalAlignment(SwingConstants.CENTER);
		   lab1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   JLabel lab2 = new JLabel("console");
		   lab2.setHorizontalAlignment(SwingConstants.CENTER);
		   lab2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   btnShowConsole.add(BorderLayout.NORTH,lab1);
		   btnShowConsole.add(BorderLayout.SOUTH,lab2);
		btnShowConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setVisible(true);
			}
		});
		btnShowConsole.setBounds(503, 297, 109, 50);
		contentPane.add(btnShowConsole);

		JButton btnBacktrack = new JButton("Backtrack");
		btnBacktrack.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnBacktrack.setBounds(12, 297, 109, 50);
		btnBacktrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.backtrack();
			}
		});
		contentPane.add(btnBacktrack);

		JButton btnInspectProcessState = new JButton();
		btnInspectProcessState.setLayout(new BorderLayout());
		   JLabel l1 = new JLabel("Inspect");
		   l1.setHorizontalAlignment(SwingConstants.CENTER);
		   l1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   JLabel l2 = new JLabel("state");
		   l2.setHorizontalAlignment(SwingConstants.CENTER);
		   l2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   btnInspectProcessState.add(BorderLayout.NORTH,l1);
		   btnInspectProcessState.add(BorderLayout.SOUTH,l2);
			btnInspectProcessState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				if (processFrame == null)
					processFrame = new ProcessStateFrame(client);
				client.getIRList();
			}
		});
		btnInspectProcessState.setBounds(176, 297, 109, 50);
		contentPane.add(btnInspectProcessState);

		JButton btnSave = new JButton("Save");
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		btnSave.setBounds(632, 120, 109, 40);
		contentPane.add(btnSave);

		JButton btnServerConfiguration = new JButton();
		btnServerConfiguration.setLayout(new BorderLayout());
		   JLabel label1 = new JLabel("Server");
		   label1.setHorizontalAlignment(SwingConstants.CENTER);
		   label1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   JLabel label2 = new JLabel("Config");
		   label2.setHorizontalAlignment(SwingConstants.CENTER);
		   label2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		   btnServerConfiguration.add(BorderLayout.NORTH,label1);
		   btnServerConfiguration.add(BorderLayout.SOUTH,label2);
		btnServerConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverconf = new ServerConfFrame(client.getServerConf(), client);
			}
		});
		btnServerConfiguration.setBounds(340, 297, 109, 50);
		contentPane.add(btnServerConfiguration);

		JScrollPane instructionAreaScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		instructionAreaScroll.setBounds(12, 13, 600, 260);
		contentPane.add(instructionAreaScroll);

		instructionArea = new JTextArea();
		instructionArea.setWrapStyleWord(true);
        instructionArea.setLineWrap(true);
        //PF. orig 18
        instructionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        //PF. added
        instructionArea.setText("//instruciontArea");
        instructionArea.setTabSize(2);
        instructionArea.setEditable(false);

		JTextArea lines = new JTextArea("1");
		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);
		// PF. Dim barra laterale - orig 18
		lines.setFont(new Font("Monospaced", Font.PLAIN, 12));

		instructionArea.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int currentPos = instructionArea.getDocument().getLength();
				Element root = instructionArea.getDocument().getDefaultRootElement();
				String text = "1  " + System.getProperty("line.separator");
				for(int i = 2; i < root.getElementIndex(currentPos) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

			@Override
			public void removeUpdate(DocumentEvent de) {
				lines.setText(getText());
			}

		});
		instructionAreaScroll.setViewportView(instructionArea);
		instructionAreaScroll.setRowHeaderView(lines);
		instructionAreaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


		contentPane.revalidate();
		contentPane.repaint();
	}

	public void printMessage(String msg) {
		console.addText(msg);
	}

	public void printIstruction(String istr) {
		/*if(!instructionArea.getText().equals(""))
			instructionArea.setText(instructionArea.getText() + "\n" + istr);
		else
			instructionArea.setText(instructionArea.getText() + istr);*/
		instructionArea.setText(istr);
	}

	public ProcessStateFrame getProcessStateFrame() {
		return processFrame;
	}

	public void resetInstructionArea() {
		instructionArea.setText("");
	}

	public ServerConfFrame getServerConfFrame() {
		return serverconf;
	}

	private void saveFile() {
		JFrame parentFrame = new JFrame();
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser fileChooser = new JFileChooser();
		TxtFilter txt = new TxtFilter();
		fileChooser.setDialogTitle("Save");
		fileChooser.setFileFilter(txt);
		Component[] comp = fileChooser.getComponents();
		setFileChooserFont(comp);

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			String path = "";
			if (!fileChooser.getSelectedFile().getName().contains("."))
				path = fileChooser.getSelectedFile().getPath() + ".txt";
			else
				path = fileChooser.getSelectedFile().getPath();

			File fileToSave = new File(path);

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileToSave));
				out.write(instructionArea.getText());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void setFileChooserFont(Component[] comp) {
		Font font = new Font("monospaced", Font.PLAIN, 17);
		for (int x = 0; x < comp.length; x++) {
			if (comp[x] instanceof Container)
				setFileChooserFont(((Container) comp[x]).getComponents());
			try {
				comp[x].setFont(font);
			} catch (Exception e) {
			} // do nothing
		}
	}

	public class TxtFilter extends FileFilter{
	    @Override
	    public boolean accept(File f){
	        return f.getName().toLowerCase().endsWith(".txt")||f.isDirectory();
	    }
	    @Override
	    public String getDescription(){
	        return "Text files (*.txt)";
	    }
	}
}
