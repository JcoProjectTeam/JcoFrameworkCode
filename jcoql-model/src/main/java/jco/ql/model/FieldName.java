package jco.ql.model;

import java.util.Arrays;

import jco.ql.model.engine.JCOConstants;

public class FieldName implements JCOConstants {	

	private int level;
	private String[] parts;
	
	private FieldName() {
		super();
	}
	
	public static FieldName fromString(String fieldNameString) {
		FieldName fieldName = new FieldName();
		String[] parts = null;
		if(fieldNameString.startsWith(".")) {
			parts = fieldNameString.substring(1).split(FIELD_SEPARATOR_REGEX);
		} else {
			parts = fieldNameString.split(FIELD_SEPARATOR_REGEX);
		}
		fieldName.level = parts.length;
		fieldName.parts = parts;

		return fieldName;
	}
	
	public static FieldName fromParts(String[] fieldNameParts) {
		FieldName fieldName = new FieldName();
		fieldName.level = fieldNameParts.length;
		fieldName.parts = fieldNameParts;
		
		return fieldName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String[] getParts() {
		return parts;
	}

	public void setParts(String[] parts) {
		this.parts = parts;
	}

	public String getLevelName(int l) {
		String name = null;
		if(parts != null && parts.length > 0 && l>=0 && l<parts.length) {
			name = parts[l];
		}
		return name;
	}
	public String getFirstLevelName() {
		String name = null;
		if(parts != null && parts.length > 0) {
			name = parts[0];
		}
		return name;
	}

	public String getLastLevelName() {
		String name = null;
		if(parts != null && parts.length > 0) {
			name = parts[parts.length - 1];
		}
		return name;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		for(String part : parts) {
			strBuf.append("." + part);
		}
		return strBuf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + Arrays.hashCode(parts);
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
		
		FieldName other = (FieldName) obj;
		if (level != other.level) {
			return false;
		}
		if (!Arrays.equals(parts, other.parts)) {
			return false;
		}
		return true;
	}
	
}
