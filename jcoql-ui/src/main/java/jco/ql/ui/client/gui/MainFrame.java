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
import javax.swing.JSpinner;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.ButtonGroup;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class MainFrame extends JFrame {
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	

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
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private Color colorActive = new Color (50,200,50);
	private Color colorDeactive = new Color (250,50,0);
	private Font font1 = new Font("Trebuchet MS", Font.PLAIN, 12);
	private int nProcessors = 1;
	private boolean isTracker = true;
	private boolean isBacktrack = false;
	private boolean isRemoveMongoId = true;
	private boolean isSpatialIndex = false;
	private boolean isMsgIndDocs = false;

	public MainFrame(Client c) {
		this.client = c;
		console = new ConsoleFrame();
		createAndShowGUI();
//		getGuiSettings();
	}

	private void createAndShowGUI() {
		setVisible(true);
		setLocationRelativeTo(null);
		setTitle("J-CO-UI: User Interface");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(756, 733);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane commandAreaScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		commandAreaScroll.setBounds(10, 403, 600, 280);
		contentPane.add(commandAreaScroll);

		// Command Area
		JTextArea commandArea = new JTextArea();
		commandArea.setWrapStyleWord(true);
        commandArea.setLineWrap(true);
		commandAreaScroll.setViewportView(commandArea);
		// PF. Char size - orig:18
		commandArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		// PF. Added
		commandArea.setTabSize(2);
		
		JLabel lblCommandArea = new JLabel ("JCO Input area"); //DefaultComponentFactory.getInstance().createTitle("Input Area");
		lblCommandArea.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
		commandAreaScroll.setColumnHeaderView(lblCommandArea);

		// Execute button
		JButton btnExecute = new JButton("Execute");
		btnExecute.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		btnExecute.setBounds(620, 403, 104, 38);
		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.executeJCO(commandArea.getText());
			}
		});
		contentPane.add(btnExecute);

		// Show console button
		JButton btnShowConsole = new JButton();
		btnShowConsole.setLayout(new BorderLayout());
		   JLabel lab1 = new JLabel("Show");
		   lab1.setHorizontalAlignment(SwingConstants.CENTER);
		   lab1.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   JLabel lab2 = new JLabel("console");
		   lab2.setHorizontalAlignment(SwingConstants.CENTER);
		   lab2.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   btnShowConsole.add(BorderLayout.NORTH,lab1);
		   btnShowConsole.add(BorderLayout.SOUTH,lab2);
		btnShowConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				console.setVisible(true);
			}
		});
		btnShowConsole.setBounds(491, 340, 118, 50);
		contentPane.add(btnShowConsole);

		// Backtrack button
		JButton btnBacktrack = new JButton("Backtrack");
		btnBacktrack.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		btnBacktrack.setBounds(10, 340, 118, 50);
		btnBacktrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.backtrack();
			}
		});
		contentPane.add(btnBacktrack);

		// Inspect state button
		JButton btnInspectProcessState = new JButton();
		btnInspectProcessState.setLayout(new BorderLayout());
		   JLabel l1 = new JLabel("Inspect");
		   l1.setHorizontalAlignment(SwingConstants.CENTER);
		   l1.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   JLabel l2 = new JLabel("state");
		   l2.setHorizontalAlignment(SwingConstants.CENTER);
		   l2.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   btnInspectProcessState.add(BorderLayout.NORTH,l1);
		   btnInspectProcessState.add(BorderLayout.SOUTH,l2);
			btnInspectProcessState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				if (processFrame == null)
					processFrame = new ProcessStateFrame(client);
				client.getIRList();
			}
		});
		btnInspectProcessState.setBounds(171, 340, 118, 50);
		contentPane.add(btnInspectProcessState);

		// Save button
		JButton btnSave = new JButton("Save");
		btnSave.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		btnSave.setBounds(620, 33, 104, 38);
		contentPane.add(btnSave);

		// Server Config button
		JButton btnServerConfiguration = new JButton();
		btnServerConfiguration.setLayout(new BorderLayout());
		   JLabel label1 = new JLabel("Server");
		   label1.setHorizontalAlignment(SwingConstants.CENTER);
		   label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   JLabel label2 = new JLabel("Config");
		   label2.setHorizontalAlignment(SwingConstants.CENTER);
		   label2.setFont(new Font("Trebuchet MS", Font.PLAIN, 18));
		   btnServerConfiguration.add(BorderLayout.NORTH,label1);
		   btnServerConfiguration.add(BorderLayout.SOUTH,label2);
		btnServerConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverconf = new ServerConfFrame(client.getServerConf(), client);
			}
		});
		btnServerConfiguration.setBounds(332, 340, 118, 50);
		contentPane.add(btnServerConfiguration);

		JScrollPane instructionAreaScroll = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		instructionAreaScroll.setBounds(10, 33, 600, 285);
		contentPane.add(instructionAreaScroll);

		// Instruction Areas
		instructionArea = new JTextArea();
		instructionArea.setWrapStyleWord(true);
        instructionArea.setLineWrap(true);
        //PF. orig 18
        instructionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
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
		
		JLabel lblInstructionArea = new JLabel ("JCO Instruction Area"); //DefaultComponentFactory.getInstance().createLabel("Instruction Area");
		lblInstructionArea.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
		instructionAreaScroll.setColumnHeaderView(lblInstructionArea);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 784, 22);
		contentPane.add(menuBar);
		
		JMenu mnSettings = new JMenu("Settings");
		mnSettings.setFont(font1);
		menuBar.add(mnSettings);
		mnSettings.setHorizontalAlignment(SwingConstants.LEFT);
		
		JPanel mntPanel = new JPanel();
		mnSettings.add(mntPanel);
		mntPanel.setLayout(new BoxLayout(mntPanel, BoxLayout.X_AXIS));
		
		JLabel lblSpinnerLabel = new JLabel("  # Processors                       ");
		lblSpinnerLabel.setFont(font1);
		mntPanel.add(lblSpinnerLabel);
		
		JSpinner processorSpinner = new JSpinner();
		processorSpinner.setFont(font1);
		processorSpinner.setModel(new SpinnerNumberModel(nProcessors, Integer.valueOf(1), null, Integer.valueOf(1)));
		processorSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				client.setNProcessors(processorSpinner.getModel().getValue().toString());				
 			}
		});
		mntPanel.add(processorSpinner);
		
		JSeparator separator = new JSeparator();
		mnSettings.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		mnSettings.add(separator_1);
		
		JMenuItem mntTrack = new JMenuItem("Track instructions execution time");
		mntTrack.setFont(font1);
		mntTrack.setSelected(true);
		if (isTracker)
			mntTrack.setBackground(colorActive);
		else
			mntTrack.setBackground(colorDeactive);
		mntTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mntTrack.getBackground().equals(colorDeactive)) {
					mntTrack.setBackground(colorActive);
					client.setTrackTime(TRUE);
				}
				else {
					mntTrack.setBackground(colorDeactive);
					client.setTrackTime("false");
				}
			}
		});
		mnSettings.add(mntTrack);
		
		JSeparator separator_2 = new JSeparator();
		mnSettings.add(separator_2);
		
		JMenuItem mntSpatial = new JMenuItem("Spatial Indexing");
		mntSpatial.setFont(font1);
		if (isSpatialIndex)
			mntSpatial.setBackground(colorActive);
		else
			mntSpatial.setBackground(colorDeactive);
		mnSettings.add(mntSpatial);
		mntSpatial.setHorizontalAlignment(SwingConstants.LEFT);
		mntSpatial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mntSpatial.getBackground().equals(colorDeactive)) {
					mntSpatial.setBackground(colorActive);
					client.setSpatialIndex(TRUE);
				}
				else {
					mntSpatial.setBackground(colorDeactive);
					client.setSpatialIndex(FALSE);
				}
			}
		});
		buttonGroup.add(mntSpatial);
		
		JSeparator separator_3 = new JSeparator();
		mnSettings.add(separator_3);
		
		JMenuItem mntBacktrack = new JMenuItem("Backtrack");
		mntBacktrack.setBackground(new Color(240, 240, 240));
		mntBacktrack.setFont(font1);
		if (isBacktrack)
			mntBacktrack.setBackground(colorActive);
		else
			mntBacktrack.setBackground(colorDeactive);
		mntBacktrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mntBacktrack.getBackground().equals(colorDeactive)) {
					mntBacktrack.setBackground(colorActive);
					client.setBacktrack(TRUE);
				}
				else {
					mntBacktrack.setBackground(colorDeactive);
					client.setBacktrack(FALSE);
				}
			}
		});
		mnSettings.add(mntBacktrack);
		mntBacktrack.setSelected(true);
		
		JSeparator separator_4 = new JSeparator();
		mnSettings.add(separator_4);
		
		JMenuItem mntMessages = new JMenuItem("Store messages in document");
		mntMessages.setBackground(new Color(240, 240, 240));
		if (isMsgIndDocs)
			mntMessages.setBackground(colorActive);
		else
			mntMessages.setBackground(colorDeactive);
		mntMessages.setFont(font1);
		mntMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mntMessages.getBackground().equals(colorDeactive)) {
					mntMessages.setBackground(colorActive);
					client.setStoreMsg(TRUE);
				}
				else {
					mntMessages.setBackground(colorDeactive);
					client.setStoreMsg(FALSE);
				}
			}
		});
		mnSettings.add(mntMessages);
		mntMessages.setSelected(true);
		
		JSeparator separator_5 = new JSeparator();
		mnSettings.add(separator_5);
		
		JMenuItem mntMongoId = new JMenuItem("Remove MongoDB  \"_id\"  attribute");
		mntMongoId.setFont(font1);
		if (isRemoveMongoId)
			mntMongoId.setBackground(colorActive);
		else
			mntMongoId.setBackground(colorDeactive);
		mntMongoId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mntMongoId.getBackground().equals(colorDeactive)) {
					mntMongoId.setBackground(colorActive);
					client.setRemoveMongoId(TRUE);
				}
				else {
					mntMongoId.setBackground(colorDeactive);
					client.setRemoveMongoId(FALSE);
				}
			}
		});
		mnSettings.add(mntMongoId);
		
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
