package jco.ql.db.ds.client.shell;

import java.util.List;
import java.util.stream.Collectors;

import jco.ql.model.value.JCOValue;

public final class ShellUtils {

	public static String formatValueOptions(List<JCOValue> options) {
		String output = "";
		if(options != null) {
			output = formatStringOptions(options.stream()
											.map(JCOValue::getStringValue)
											.collect(Collectors.toList())
						);
		}
			
		return output;
	}
	public static String formatStringOptions(List<String> options) {
		String output = "";
		if(options != null) {
			for(String option : options) {
				output += "- " + option + "\n";
			}
		}
		return output;
	}
}
