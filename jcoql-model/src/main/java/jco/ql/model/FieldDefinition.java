package jco.ql.model;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.JCOValue;

// ZUN CHECK* .... questa classe forse potrebbe essere eliminata
public class FieldDefinition implements JCOConstants {

	private String name;
	private JCOValue value;

	public FieldDefinition(String name, JCOValue value) {
		//ZUN CHECK* perch� tutti i punti? ZZUN 
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
