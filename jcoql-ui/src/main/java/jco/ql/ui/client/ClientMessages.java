package jco.ql.ui.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Rappresenta i messaggi inviati dal client al server
 * 
 * @author Savino Lechiancole
 *
 */
public class ClientMessages {

	public ClientMessages() {
	}

	/**
	 * Prende la lista delle istruzioni e crea il messaggio da inviare al server:
	 * <br>
	 * ##BEGIN-PROCESS## <br>
	 * ..istruzioni.. <br>
	 * ##END-PROCESS##
	 * 
	 * @param msg
	 *            istruzioni JCO da eseguire sul server
	 */

	public String executeJCO(String msg) {
		String prefix = "##BEGIN-PROCESS##\n";
		String suffix = "\n##END-PROCESS##";
		return prefix + msg + suffix;
	}

	/**
	 * Crea il messaggio per effettuare il backtrack
	 * 
	 */
	public String backtrack() {
		return "##BACKTRACK##";
	}

	public String getMsgTemporaryCollection() {
		return "##GET-TEMPORARY-COLLECTION##";
	}

	public String getMsgGetProcess() {
		return "##GET-PROCESS##";
	}

	public String getMsgIRLIst() {
		return "##GET-IR-LIST##";
	}

	public String getMsgIRCollection(String collectionName) {
		String prefix = "##GET-IR-COLLECTION##\n";
		String suffix = "\n##END-IR-COLLECTION##";

		return prefix + collectionName + suffix;
	}

	/**
	 * 
	 * @param fileName
	 *            il nome del file dove l'utente ha inserito il file di
	 *            configurazione
	 * @return la stringa con il messaggio da inviare al server
	 */
	public String getMsgAddServerConf(String fileName) {
		String prefix = "##ADD-SERVER-CONF##\n";
		String suffix = "\n##END-SERVER-CONF##";

		// lettura del file di configurazione
		String result = "";
		try {

			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				result = result + currentLine + "\n";
			}

			br.close();
			fr.close();
		} catch (IOException e) {
			final JPanel panel = new JPanel();

			JOptionPane.showMessageDialog(panel, "File " + fileName + " not found", "Error", JOptionPane.ERROR_MESSAGE);
		}

		return prefix + result + suffix;
	}
}
