package jco.ql.db.mongodb.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.wololo.jts2geojson.GeoJSONReader;

import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class ValueUtils implements JCOConstants {

	public static JCOValue fromObject(Object object) {
		return fromObject(null, object);
	}

	public static JCOValue fromObject(String key, Object object) {
		JCOValue value = new SimpleValue(); // initial null value

		if (object != null)
			if (object instanceof ObjectId) 
				value = new SimpleValue(((ObjectId) object).toString());
	
			else if (object instanceof Integer) 
				value = new SimpleValue((Integer) object);
	
			else if(object instanceof Long)
				value = new SimpleValue((Long) object);
			
			else if (object instanceof Double) 
				value = new SimpleValue(new BigDecimal((Double) object));
			
			else if (object instanceof Float) 
				value = new SimpleValue(new BigDecimal((Float) object));
			
			else if (object instanceof BigDecimal) 
				value = new SimpleValue((BigDecimal) object);
			
			else if (object instanceof Boolean) 
				value = new SimpleValue((Boolean) object);
			
			else if(object instanceof Date)
				value = new SimpleValue((Date)object);
			
			else if (object instanceof String) 
				value = new SimpleValue((String) object);
			
			else if (object instanceof Document) {
				if (GEOMETRY_FIELD_NAME.equals(key)) {
					String geoJsonString = ((Document) object).toJson();
					value = new GeometryValue(new GeoJSONReader().read(geoJsonString));
				} 
				else {
					value = new DocumentValue(((Document) object).entrySet().stream()
							.map(e -> new FieldDefinition(e.getKey(), ValueUtils.fromObject(e.getKey(), e.getValue())))
							.collect(Collectors.toList()));
				}
			} 
			
			else if (object instanceof ArrayList) {
				@SuppressWarnings("unchecked")
				ArrayList<JCOValue> array = ArrayList.class.cast(object);
				ListIterator<JCOValue> litr = array.listIterator();
				List<JCOValue> values = new ArrayList<JCOValue>();
				while (litr.hasNext()) 
					values.add(fromObject(litr.next()));
				value = new ArrayValue(values);
			} 

		return value;
	}

}
