package jco.ql.model.reference;

import jco.ql.model.FieldName;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.exception.InvalidCollectionAliasException;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;

public class FieldReference implements JCOConstants, JCOValue {

	private String collectionAlias;
	
	private FieldName fieldName;
	
	
	// PF. 24.01.2022 To be used for alias string AND FieldName patterns
	public FieldReference(String collectionAlias, FieldName fieldName) {
		this.collectionAlias = collectionAlias;
		this.fieldName = fieldName;
	}
	
	
	// PF. 24.01.2022 To be used for alias string AND .field1.field2...fieldN string patterns
	public FieldReference(String collectionAlias, String fieldName) {
		this.collectionAlias = collectionAlias;
		this.fieldName = FieldName.fromString(fieldName);
	}
	
	
	// PF. 24.01.2022 To be used for alias.field1.field2...fieldN string patterns
	public FieldReference(String fieldName) {
		if(fieldName.startsWith(FIELD_SEPARATOR)) {
			throw new InvalidCollectionAliasException();
		} else {
			String[] parts = fieldName.split(FIELD_SEPARATOR_REGEX);
			this.collectionAlias = parts[0];
			this.fieldName = FieldName.fromString(fieldName.substring(this.collectionAlias.length()));
		}
	}

	

	public String getCollectionAlias() {
		return collectionAlias;
	}

	
	public FieldName getFieldName() {
		return fieldName;
	}

	
	@Override
	public String toString() {
		return this.collectionAlias + this.fieldName.toString();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collectionAlias == null) ? 0 : collectionAlias.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		
		FieldReference other = (FieldReference) obj;
		if (collectionAlias == null) {
			if (other.collectionAlias != null) {
				return false;
			}
		} else if (!collectionAlias.equals(other.collectionAlias)) {
			return false;
		}
		if (fieldName == null) {
			if (other.fieldName != null) {
				return false;
			}
		} else if (!fieldName.equals(other.fieldName)) {
			return false;
		}
		return true;
	}


	@Override
	public EValueType getType() {
		return EValueType.FIELD;
	}


	@Override
	public String getStringValue() {
		return toString();
	}


	@Override
	public Object getValue() {
		return this;
	}
	
}
