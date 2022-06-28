package jco.ql.model;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.JCOValue;

// ZUN CHECK* .... sta classe potrebbe essere fatta fuori
public class FieldDefinition implements JCOConstants {

	private String name;
	private JCOValue value;

	public FieldDefinition(String name, JCOValue value) {
		//ZUN CHECK* perché tutti i punti?
		this.name = name.replace(FIELD_SEPARATOR, "");
		this.value = value;
	}

	
	public String getName() {
		return name;
	}

	
	public void setValue(JCOValue v) {
		value = v;
	}

	public JCOValue getValue() {
		return value;
	}

}
