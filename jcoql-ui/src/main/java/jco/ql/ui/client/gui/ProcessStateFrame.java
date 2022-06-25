package jco.ql.ui.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import jco.ql.ui.client.Client;

public class ProcessStateFrame extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = -1574444051008640971L;

	private JPanel contentPane;
	private Client client;
	private JTree tree;
	private JScrollPane scrollPane;
	private boolean treeExpanded = false;
	private DefaultListModel<String> listModel;
	private JLabel lblElementNumber;
	private String currentCollection;

	public ProcessStateFrame(Client c) {
		this.client = c;
		createAndShowGUI();
		tree = new JTree();
		currentCollection = "";
	}

	private void createAndShowGUI() {
		setVisible(true);
		setLocationRelativeTo(null);
		setTitle("Inspect process state");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(1000, 532);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 0, 341, 472);
		contentPane.add(panel);
		panel.setLayout(null);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 196, 317, 217);
		panel.add(scrollPane_1);

		listModel = new DefaultListModel<>();
		JList<String> list = new JList<>(listModel);
		list.setFont(new Font("Tahoma", Font.PLAIN, 18));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(list);

		JLabel lblIrLisr = new JLabel("IR list:");
		lblIrLisr.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblIrLisr.setBounds(12, 167, 56, 16);
		panel.add(lblIrLisr);

		JButton btnShowIR = new JButton("Show IR");
		btnShowIR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.getIRCollection(list.getSelectedValue());
			}
		});
		btnShowIR.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnShowIR.setBounds(212, 426, 117, 33);
		panel.add(btnShowIR);

		JLabel lblTemporaryCollection = new JLabel("Temporary collection:");
		lblTemporaryCollection.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblTemporaryCollection.setBounds(12, 70, 184, 21);
		panel.add(lblTemporaryCollection);

		JButton btnShowTemporary = new JButton("Show TC");
		btnShowTemporary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.getTemporaryCollection();
			}
		});
		btnShowTemporary.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnShowTemporary.setBounds(212, 65, 117, 31);
		panel.add(btnShowTemporary);

		lblElementNumber = new JLabel("");
		lblElementNumber.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblElementNumber.setBounds(80, 167, 116, 16);
		panel.add(lblElementNumber);

		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(365, 13, 605, 400);
		contentPane.add(scrollPane);

		JButton btnSaveCollection = new JButton("Save");
		btnSaveCollection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});
		btnSaveCollection.setBounds(764, 431, 97, 31);
		contentPane.add(btnSaveCollection);
		btnSaveCollection.setFont(new Font("Tahoma", Font.PLAIN, 18));

		JButton btnExpand = new JButton("Expand");
		btnExpand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tree.getRowCount() > 0) {
					if (!treeExpanded) {
						expandAllNodes(tree, 0, tree.getRowCount());
						treeExpanded = true;
					} else {
						for (int i = 0; i < tree.getRowCount(); i++) {
							tree.collapseRow(i);
							treeExpanded = false;
						}
					}
				}
			}
		});
		btnExpand.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnExpand.setBounds(873, 431, 97, 31);
		contentPane.add(btnExpand);
	}

	public void createTree(String collection) {
		currentCollection = collection;
		DefaultMutableTreeNode root = null;
		try {

			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = f.createParser(collection);
			JsonToken current = jp.nextToken();

			// il documento JSON deve iniziare con {
			if (current != JsonToken.START_OBJECT) {
				System.out.println("Error: root should be object: quiting.");
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jp.getCurrentName();
				current = jp.nextToken();
				JsonNode node = jp.readValueAsTree();

				if (fieldName.equals("documents")) {
					root = new DefaultMutableTreeNode();
					for (int i = 0; i < node.size(); i++) {
						DefaultMutableTreeNode child = new DefaultMutableTreeNode(i + 1);
						createNode(node.get(i), child);
						root.add(child);
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		tree = new JTree(root);
		tree.setFont(new Font("Tahoma", Font.PLAIN, 18));
		tree.setRootVisible(false);

		scrollPane.setViewportView(tree);
	}

	private void createNode(JsonNode node, DefaultMutableTreeNode child) {
		Iterator<String> fields = node.fieldNames();

		while (fields.hasNext()) {
			String name = fields.next();
			if (!node.get(name).isObject()) {
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(name + ": " + node.get(name));
				child.add(treeNode);
			} else if (node.get(name).isArray()) {
				;
// PF - add something ho handle ARRAY case  //System.out.println("ARRAY");
			} else {
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(name);
				createNode(node.get(name), treeNode);
				child.add(treeNode);
			}

		}
	}

	public void addElementToList(String elements) {
		try {

			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = f.createParser(elements);
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
					String text = "";
					if (node.asInt() == 1)
						text = node.asInt() + " element";
					else
						text = node.asInt() + " elements";
					lblElementNumber.setText(text);
				} else if (fieldName.equals("IRList")) {
					for (int i = 0; i < node.size(); i++)
						listModel.addElement(node.get(i).asText());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
		for (int i = startingIndex; i < rowCount; ++i) {
			tree.expandRow(i);
		}

		if (tree.getRowCount() != rowCount) {
			expandAllNodes(tree, rowCount, tree.getRowCount());
		}
	}

	private void saveFile() {
		JFrame parentFrame = new JFrame();
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		 JsonFilter jsonFilter = new JsonFilter();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(jsonFilter);
		fileChooser.setDialogTitle("Save");
		Component[] comp = fileChooser.getComponents();
		setFileChooserFont(comp);

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			String path = "";
			if (!fileChooser.getSelectedFile().getName().contains("."))
				path = fileChooser.getSelectedFile().getPath() + ".json";
			else
				path = fileChooser.getSelectedFile().getPath();

			File fileToSave = new File(path);

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileToSave));
				out.write(currentCollection);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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

	class JsonFilter extends FileFilter{
	    @Override
	    public boolean accept(File f){
	        return f.getName().toLowerCase().endsWith(".json")||f.isDirectory();
	    }
	    @Override
	    public String getDescription(){
	        return "JSON files (*.json)";
	    }
	}
}
