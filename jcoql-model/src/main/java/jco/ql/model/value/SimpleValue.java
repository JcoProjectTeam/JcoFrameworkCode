package jco.ql.model.value;

import java.math.BigDecimal;
import java.util.Date;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;


public class SimpleValue implements JCOConstants, JCOValue, Comparable<SimpleValue>{
	private EValueType valueType;

	private String stringValue;
	private BigDecimal decimalValue;
	private Boolean booleanValue;
	private Date dateValue;

	
	public SimpleValue() {
		valueType = EValueType.NULL;
	}

	public SimpleValue(String value) {
		valueType = EValueType.STRING;

		if(value.startsWith("\"") && value.endsWith("\"")) 
			stringValue = value.substring(1, value.length()-1);
		else if(value.startsWith("'") && value.endsWith("'")) 
			stringValue = value.substring(1, value.length()-1);
		else 
			stringValue = value;
	}

	public SimpleValue(BigDecimal value) {
		valueType = EValueType.DECIMAL;
		decimalValue = value;
	}

	public SimpleValue(Boolean value) {
		valueType = EValueType.BOOLEAN;
		booleanValue = value;
	}

	public SimpleValue(Integer value) {
		valueType = EValueType.INTEGER;
		decimalValue = BigDecimal.valueOf(value.longValue());
	}

	public SimpleValue(Long value){
		valueType = EValueType.INTEGER;
		decimalValue = BigDecimal.valueOf(value);
	}


	public SimpleValue(Double value) {
		valueType = EValueType.DECIMAL;
		decimalValue = BigDecimal.valueOf(value);
	}

	public SimpleValue(Date value) {
		valueType = EValueType.DATE;
		dateValue = value;
	}

	// Added by Cazzaniga 2024 - handling data from Postgres?
	public SimpleValue(Object value, EValueType type) {
	    this.valueType = type;

	    switch (type) {
	        case STRING:
	            this.stringValue = value.toString();
	            break;
	        case INTEGER:
	            if (value instanceof Integer) {
	                this.decimalValue = BigDecimal.valueOf((Integer) value);
	            } else if (value instanceof Long) {
	                this.decimalValue = BigDecimal.valueOf((Long) value);
	            } else {
	                //throw new IllegalArgumentException("Tipo non supportato per INTEGER: " + value.getClass().getSimpleName());
	            	// ZUN modify 2025
	        		valueType = EValueType.NULL;
	        		JMH.addExceptionMessage("Postgres INTEGER Data not supported: " + value.getClass().getSimpleName());
	            }
	            break;
	        case DECIMAL:
	            if (value instanceof Double) {
	                this.decimalValue = BigDecimal.valueOf((Double) value);
	            } else if (value instanceof BigDecimal) {
	                this.decimalValue = (BigDecimal) value;
	            } else {
//	                throw new IllegalArgumentException("Tipo non supportato per DECIMAL: " + value.getClass().getSimpleName());
	            	// ZUN modify 2025
	        		valueType = EValueType.NULL;
	        		JMH.addExceptionMessage("Postgres DECIMAL Data not supported: " + value.getClass().getSimpleName());
	            }
	            break;
	        case BOOLEAN:
	            if (value instanceof Boolean) {
	                this.booleanValue = (Boolean) value;
	            } else {
//	                throw new IllegalArgumentException("Tipo non supportato per BOOLEAN: " + value.getClass().getSimpleName());
	            	// ZUN modify 2025
	        		valueType = EValueType.NULL;
	        		JMH.addExceptionMessage("Postgres BOOLEAN Data not supported: " + value.getClass().getSimpleName());
	            }
	            break;
	        case DATE:
	            if (value instanceof Date) {
	                this.dateValue = (Date) value;
	            } else {
//	                throw new IllegalArgumentException("Tipo non supportato per DATE: " + value.getClass().getSimpleName());
	            	// ZUN modify 2025
	        		valueType = EValueType.NULL;
	        		JMH.addExceptionMessage("Postgres DATE Data not supported: " + value.getClass().getSimpleName());
	            }
	            break;
	        case NULL:
            	// ZUN modify 2025
        		valueType = EValueType.NULL;
        		JMH.addExceptionMessage("Postgres Data not supported: " + value.getClass().getSimpleName());
	            break;
	        default:
//	            throw new IllegalArgumentException("Tipo non supportato: " + type);
            	// ZUN modify 2025
        		valueType = EValueType.NULL;
        		JMH.addExceptionMessage("Postgres Data not supported: " + value.getClass().getSimpleName());
	    }
	}

	
	@Override
	public EValueType getType() {
		return valueType;
	}

	@Override
	public String getStringValue() {
		String stringVal = "";
		
		if (valueType == EValueType.STRING)
			stringVal = stringValue;
		else if (valueType == EValueType.BOOLEAN)
			stringVal = booleanValue.toString();
		else if (valueType == EValueType.INTEGER)
			stringVal = decimalValue.toPlainString();
		else if (valueType == EValueType.DECIMAL)
			stringVal = decimalValue.toPlainString();
		else if (valueType == EValueType.DATE)
			stringVal = dateValue.toString();

		return stringVal;
	}

	
	public BigDecimal getNumericValue() {
		return decimalValue;
	}

	
	@Override
	public Object getValue() {
		Object val = null;
		
		if (valueType == EValueType.STRING)
			val = stringValue;
		else if (valueType == EValueType.BOOLEAN)
			val = booleanValue;
		else if (valueType == EValueType.INTEGER)
			val = decimalValue.longValue();
		else if (valueType == EValueType.DECIMAL)
			val = decimalValue.doubleValue();
		else if (valueType == EValueType.DATE)
			val = dateValue;

		return val;
	}

	
	@Override
	public String toString() {
		return getStringValue();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
		result = prime * result + ((booleanValue == null) ? 0 : booleanValue.hashCode());
		result = prime * result + ((decimalValue == null) ? 0 : decimalValue.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((valueType == null) ? 0 : valueType.hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		SimpleValue other = (SimpleValue) obj;
		if (valueType != other.valueType)
			return false;

		if (valueType == EValueType.BOOLEAN)
			return booleanValue.equals(other.booleanValue);

		if (valueType == EValueType.DECIMAL || valueType == EValueType.INTEGER)
			return decimalValue.equals(other.decimalValue);

		if (valueType == EValueType.STRING)
			return stringValue.equals(other.stringValue);

		if (valueType == EValueType.DATE)
			return dateValue.equals(other.dateValue);

		if (valueType == EValueType.NULL)
			return true;

		return false;
	}

	
	//	return 0 if this=v
	//	return 1 if this>v
	//	return -1 if this<v
	// return -2 if values are Uncomparable 
	@Override
	public int compareTo(SimpleValue v) {
		if (v == null || v.getType() == EValueType.NULL)
			return UNCOMPARABLE;

		if ((valueType == EValueType.STRING) && (v.getType() == EValueType.STRING)) 
			return compareResult (stringValue.compareTo((String)v.getValue()));

		if ((valueType == EValueType.BOOLEAN) && (v.getType() == EValueType.BOOLEAN)) {
			return compareBoolean (v);
		}
			
		if (((valueType == EValueType.INTEGER) 	 || (valueType == EValueType.DECIMAL)) &&
			((v.getType() == EValueType.INTEGER) || (v.getType() == EValueType.DECIMAL))) 
			return compareResult (decimalValue.doubleValue() - v.decimalValue.doubleValue());

		if ((valueType == EValueType.DATE) && (v.getType() == EValueType.DATE))
			return compareResult (dateValue.compareTo(v.dateValue));			

		if (valueType == v.getType())
			if (getStringValue().equals(v.getStringValue()))
				return EQUAL;

		return UNCOMPARABLE;
	}

	private int compareResult (double comp) {
		if (comp > EQUAL)
			return GREATER_THAN;
		if (comp < EQUAL)
			return LESS_THAN;
		return EQUAL;		
	}

	private int compareBoolean(SimpleValue v) {
		int v1=0;
		int v2=0;
		if (booleanValue)
			v1=1;
		if (v.booleanValue)
			v2=1;
		return compareResult (v1-v2);
	}

}

