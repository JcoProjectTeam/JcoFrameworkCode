package jco.ql.tester;


import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Test extends JFrame {
    private JTextArea textArea;  // area dove mostriamo il contenuto caricato
    private String variabileDaSalvare = "Questo è il contenuto della variabile da salvare.";

    public Test() {
        super("Gestione File di Testo");

        // Layout base
        setLayout(new BorderLayout());

        // Area testo per visualizzare file caricati
        textArea = new JTextArea(15, 40);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Pannello pulsanti
        JPanel panelBottoni = new JPanel();

        JButton btnCarica = new JButton("Carica File");
        JButton btnSalva = new JButton("Salva Variabile");

        panelBottoni.add(btnCarica);
        panelBottoni.add(btnSalva);

        add(panelBottoni, BorderLayout.SOUTH);

        // Azione bottone Carica
        btnCarica.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    textArea.setText(""); // resetta area testo
                    String line;
                    while ((line = reader.readLine()) != null) {
                        textArea.append(line + "\n");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore nella lettura del file", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Azione bottone Salva
        btnSalva.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(variabileDaSalvare);
                    JOptionPane.showMessageDialog(this, "File salvato con successo!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore nel salvataggio del file", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Configurazione finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // centra la finestra
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Test::new);
    }
}
