package jco.ql.model.value;

import java.util.List;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.FieldName;

public class DocumentValue implements JCOValue {

	public DocumentDefinition document;

	public DocumentValue() {
		super();
		this.document = new DocumentDefinition();
	}

	public DocumentValue(DocumentDefinition document) {
		this.document = document;
	}

	public DocumentValue(List<FieldDefinition> fields) {
		this.document = new DocumentDefinition(fields);
	}

	@Override
	public EValueType getType() {
		return EValueType.DOCUMENT;
	}

	public List<FieldDefinition> getFields() {
		return document.getFields();
	}

	public JCOValue getValue(String fieldName) {
		return this.document.getValue(fieldName);
	}

	public JCOValue getValue(FieldName fieldName) {
		return this.document.getValue(fieldName);
	}

	public JCOValue removeValue(FieldName fieldName){
		return this.document.removeValue(fieldName);
	}

	@Override
	public String toString() {
		return document.toString();
	}

	@Override
	public String getStringValue() {
		return toString();
	}

	@Override
	public Object getValue() {
		return document;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((document == null) ? 0 : document.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		DocumentValue other = (DocumentValue) obj;
		if (document == null) {
			if (other.document != null) {
				return false;
			}
		} else if (!document.equals(other.document)) {
			return false;
		}
		return true;
	}

	public DocumentDefinition getDocument() {
		return document;
	}


}
