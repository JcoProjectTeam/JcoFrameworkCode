package jco.ql.model.value;

import org.locationtech.jts.geom.Geometry;

public class GeometryValue implements JCOValue {
	
	private Geometry geometry;
	
	public GeometryValue(Geometry geometry) {
		super();
		this.geometry = geometry;
	}

	@Override
	public EValueType getType() {
		return EValueType.GEOMETRY;
	}

	@Override
	public String getStringValue() {
		return geometry.toString();
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

	@Override
	public Object getValue() {
		return geometry;
	}

	@Override
	public String toString() {
		return geometry != null ? geometry.toString() : "null";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
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
		
		GeometryValue other = (GeometryValue) obj;
		if (geometry == null) {
			if (other.geometry != null) {
				return false;
			}
		} else if (!geometry.equals(other.geometry)) {
			return false;
		}
		return true;
	}

	
}
