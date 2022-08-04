package jco.ql.db.ds.core.datatype.json;

import java.io.IOException;

import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import jco.ql.model.value.GeometryValue;

public class GeoJsonValueSerializer extends StdSerializer<GeometryValue>{
	private static final long serialVersionUID = 1L;
	private final GeoJSONWriter geoJsonWriter;

	public GeoJsonValueSerializer() {
		this(null);
	}

	protected GeoJsonValueSerializer(Class<?> vc) {
		super(vc, false);
		geoJsonWriter = new GeoJSONWriter();
	}

	@Override
	public void serialize(GeometryValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Geometry geometry = geoJsonWriter.write(value.getGeometry());
		gen.writeStartObject();
		gen.writeFieldName("type");
		gen.writeString(value.getType().name());
		gen.writeFieldName("value");
		gen.writeObject(geometry);
		gen.writeEndObject();
	}

}
