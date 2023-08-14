package jco.ql.model.value;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface JCOValue {
	
	EValueType getType();

	@JsonIgnore
	String getStringValue();

	Object getValue();
	
	public static boolean isNull (JCOValue v) {
		if (v == null)
			return true;
		if (v.getType() == EValueType.NULL)
			return true;
		return false;
	}

	
	public static boolean isNumericValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.INTEGER)
			return true;
		if (v.getType() == EValueType.DECIMAL)
			return true;
		return false;
	}
	
	
	public static boolean isFuzzyValue (JCOValue v) {
		if (isNumericValue(v)) {
			double fv = getDoubleValue (v); 
			if (fv >= 0 && fv <= 1)
				return true;
		}			
		return false;
	}

	public  static boolean isIntValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.INTEGER)
			return true;
		return false;
	}
	
	public static boolean isDoubleValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.DECIMAL)
			return true;
		return false;
	}
	
	public static boolean isBooleanValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.BOOLEAN)
			return true;
		return false;
	}
	
	public static boolean isArrayValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.ARRAY)
			return true;
		return false;
	}
	
	public static boolean isGeometryValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.GEOMETRY)
			return true;
		return false;
	}
	
	public static boolean isDocumentValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.DOCUMENT)
			return true;
		return false;
	}
	
	public static boolean isStringValue (JCOValue v) {
		if (v == null)
			return false;
		if (v.getType() == EValueType.STRING)
			return true;
		return false;
	}
	
	public static int getIntValue (JCOValue v) {
		if (isIntValue(v))
			return Integer.parseInt(v.getStringValue());
		return 0;
	}

	public static double getDoubleValue (JCOValue v) {
		if (isNumericValue(v))
			return Double.parseDouble(v.getStringValue());
		return 0;
	}

	public static boolean getBooleanValue (JCOValue v) {
		if (isBooleanValue(v))
			return "true".equals(v.getStringValue());
		return false;
	}
		
}
