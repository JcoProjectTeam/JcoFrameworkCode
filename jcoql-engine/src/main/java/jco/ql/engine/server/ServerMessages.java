package jco.ql.engine.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.ServerConfiguration;

/**
 * Rappresenta i messaggi che invia il server al clietnt
 * @author Savino Lechiancole
 *
 */
public class ServerMessages {

	public ServerMessages() {}
	
	public String getMsgSuccessExecuteJCO() {
		return "##SUCCESS##";
	}
	
	public String getMsgErrorExecuteJCO(String errorMsg) {
		String prefix = "##BEGIN-ERROR##\n";
		String suffix = "\n##END-ERROR##";
		return prefix + errorMsg + suffix;
	}
	
	public String getWarningMsg(List<String> msgs) {
		String prefix = "##BEGIN-WARNING-MESSAGE##\n";
		String suffix = "##END-WARNING-MESSAGE##";
		String result = "";
		for(String msg: msgs) {
			result = result + msg + "\n##END SUB-WARNING##\n";
		}
		return prefix + result + suffix;
	}

	public String getIOMsg(List<String> msgs) {
		String prefix = "##BEGIN-IO-WARNING##\n";
		String suffix = "##END-IO-WARNING##";
		String result = "";
		for(String msg: msgs) {
			result = result + msg + "\n##END SUB-WARNING##\n";
		}
		return prefix + result + suffix;
	}

	public String getParserMsg(List<String> errorMsgs) {
		String prefix = "##BEGIN-PARSER-ERROR##\n";
		String suffix = "##END-PARSER-ERROR##";
		String result = "";
		for(String msg: errorMsgs) {
			result = result + msg + "\n##END SUB-MESSAGE##\n";
		}
		return prefix + result + suffix;
	}

	public String getAck() {
		return "##ACK##";
	}
	
	public String getMsgCollection(String documents) {
		String prefix = "##BEGIN-COLLECTION##\n";
		String suffix = "\n##END-COLLECTION##";
		return prefix + documents + suffix;
	}
		
	public String getMsgProcess(List<String> instructions) {
		String prefix = "##BEGIN-PROCESS##\n";
		String suffix = "##END-PROCESS##";
		
		String result = "";
		for(String s: instructions) {
			result = result + s + "//##END INSTRUCTION###\n\n";
		}
		return prefix + result + suffix;
	}
	
	public String getMsgIRList(Collection<String> irlist) {
		String prefix = "##BEGIN-IR-LIST##\n";
		String suffix = "\n##END-IR-LIST##";
		
		StringBuilder output = new StringBuilder("{\n");
		StringJoiner joiner = new StringJoiner(",\n");
		
		StringJoiner joiner2 = new StringJoiner(",");
		
		joiner.add("\t\"total\": " + irlist.size());
		Iterator<String> it = irlist.iterator();
		while(it.hasNext()) {
			joiner2.add("\"" + it.next() + "\"");
		}
		
		joiner.add("\t\"IRList\": " + "[" + joiner2.toString() + "]");
		
		
		output.append(joiner.toString());
		output.append("\n}");
		return prefix + output.toString() + suffix;
	}
	
	
	public String getMsgServerConf(List<ServerConfiguration> conf) {
		String prefix = "##BEGIN-SERVER-CONF##\n";
		String suffix = "\n##END-SERVER-CONF##";

        ObjectMapper mapper = new ObjectMapper();
 
        // enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        StringBuilder output = new StringBuilder("{\n");
		StringJoiner joiner = new StringJoiner(",\n");
		
		output.append("\"total\": " + conf.size() + ",\n");
		output.append("\"servers\": [");
       
		for(ServerConfiguration c: conf) {
        	String doc;
			try {
				doc = mapper.writeValueAsString(c);
				joiner.add(doc);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }
        output.append(joiner.toString());
        output.append("\n]");
        output.append("\n}");
       
		return prefix + output.toString() + suffix;
	}

	// PF 2023.10.14
	public String getSettingConfiguration() {
		String msg = "##BEGIN-SETTING-CONFIGURATIO##";
		msg += "\nProcessors:\n" + EngineConfiguration.getNProcessors();
		msg += "\nTracker:\n" + EngineConfiguration.isTrackTimes();
		msg += "\nSpatial Index:\n" + EngineConfiguration.isSpatialIndexing();
		msg += "\nBacktrack:\n" + EngineConfiguration.isBacktrack();
		msg += "\nMsg in Docs:\n" + EngineConfiguration.isMsgInDoc();
		msg += "\nRemove MongoDb Id:\n" + EngineConfiguration.isRemoveMondgoId();
		return msg;
	}


}
