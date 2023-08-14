package jco.ql.model.value;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ArrayValue implements JCOValue {
	
	@JsonIgnore
	private List<JCOValue> values;
	
	public ArrayValue() {
		this.values = new ArrayList<JCOValue>();
	}

	public ArrayValue(List<JCOValue> values) {
		this.values = values;
	}

	public void add(JCOValue value) {
		this.values.add(value);
	}

	@Override
	public EValueType getType() {
		return EValueType.ARRAY;
	}

	public List<JCOValue> getValues() {
		return values;
	}

	@Override
	public String getStringValue() {
		return toString();
	}
	
	@Override
	public Object getValue() {
		return values;
	}

	@Override
	public String toString() {
		StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
		for(JCOValue value : values) {
			if (JCOValue.isStringValue(value))
				stringJoiner.add("\"" + value.getStringValue() + "\"");
			else
				stringJoiner.add(value.getStringValue());
		}
		return stringJoiner.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		
		ArrayValue other = (ArrayValue) obj;
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}
}
