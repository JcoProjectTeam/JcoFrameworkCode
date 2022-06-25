package jco.ql.model.builder;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.DocumentValue;

public class DocumentDefinitionBuilder {
	
	private String fieldName;
	
	private List<FieldDefinition> fields;
	private FieldDefinitionBuilder parentField;
	
/*	
 * public static DocumentDefinitionBuilder create() {
		return new DocumentDefinitionBuilder();
	}
	*/

	public DocumentDefinitionBuilder() {
		this(null, null);
	}
	
	public DocumentDefinitionBuilder(String fieldName) {
		this(fieldName, null);
	}
	
	public DocumentDefinitionBuilder(String fieldName, FieldDefinitionBuilder parent) {
		this.fieldName = fieldName;
		this.fields = new ArrayList<FieldDefinition>();
		this.parentField = parent;
	}
	

	public DocumentDefinitionBuilder addFieldDefinition(FieldDefinition fieldDefinition) {
		fields.add(fieldDefinition);
		return this;
	}
	
	public DocumentDefinition build() {
		return new DocumentDefinition(fieldName, fields);
	}
	
	public FieldDefinitionBuilder getNewFieldDefintionBuilder() {
		return new FieldDefinitionBuilder(this);
	}
	
	public FieldDefinitionBuilder buildField() {
		return this.parentField.fromDocument(fieldName, new DocumentValue(fields));
	}
	

	// PF. Added on 08.02.2022
	public FieldDefinition getFieldDefinition (String fieldName) {
		if (fields != null && fieldName != null)
			for (int i=0; i<fields.size(); i++)
				if (fieldName.equals(fields.get(i).getName()))
					return fields.get(i);
		return null;
	}
	

}
