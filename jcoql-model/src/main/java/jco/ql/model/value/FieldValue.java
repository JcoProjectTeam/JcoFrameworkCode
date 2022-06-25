package jco.ql.model.value;

import jco.ql.model.FieldName;
import jco.ql.model.reference.FieldReference;
import jco.ql.parser.model.util.Field;

// ZUN CHECK... valutare se eliminare... fieldValue è un FieldReference!!!!
public class FieldValue implements JCOValue {
	
	private FieldReference fieldReference;
	
	// PF 10.02.2022 to manage fields as alias.field1.field2...fieldN
	public FieldValue(Field field) {
		this.fieldReference = new FieldReference(field.toString());
	}

	public FieldValue(FieldReference fieldReference) {
		this.fieldReference = fieldReference;
	}

	public FieldValue(String collectionAlias, String fieldName) {
		this.fieldReference = new FieldReference(collectionAlias, fieldName);
	}

	public FieldValue(String collectionAlias, FieldName fieldName) {
		this.fieldReference = new FieldReference(collectionAlias, fieldName);
	}

	
	@Override
	public EValueType getType() {
		return EValueType.FIELD;
	}

	public FieldReference getFieldReference() {
		return fieldReference;
	}
	
	@Override
	public String getStringValue() {
		return toString();
	}

	@Override
	public Object getValue() {
		return fieldReference;
	}

	@Override
	public String toString() {
		return fieldReference.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldReference == null) ? 0 : fieldReference.hashCode());
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
		
		FieldValue other = (FieldValue) obj;
		if (fieldReference == null) {
			if (other.fieldReference != null) {
				return false;
			}
		} else if (!fieldReference.equals(other.fieldReference)) {
			return false;
		}
		return true;
	}

}
