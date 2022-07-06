package jco.ql.db.ds.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jco.ql.db.ds.core.datatype.json.GeoJsonValueSerializer;
import jco.ql.db.ds.core.datatype.json.JcoValueDeserializer;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public final class DocumentDefinitionUtils implements JCOConstants {
	private static ObjectMapper jsonMapper = getDocumentMapper();

	public static ObjectMapper getDocumentMapper() {
		ObjectMapper jsonMapper = new ObjectMapper();
		SimpleModule valueModule = new SimpleModule();
		valueModule.addSerializer(GeometryValue.class, new GeoJsonValueSerializer());
		valueModule.addDeserializer(JCOValue.class, new JcoValueDeserializer());
		jsonMapper.registerModule(valueModule);
		
		return jsonMapper;
	}
	
	public static String prettyPrintJSON(List<Map<String, Object>> documents) {
		return prettyPrintObject(documents);
	}

	public static String prettyPrintJSON(Map<String, Object> document) {
		return prettyPrintObject(document);
	}

	public static String prettyPrint(List<DocumentDefinition> documents) {
		return prettyPrintObject(documents);
	}
	
	public static String prettyPrint(DocumentDefinition document) {
		return prettyPrintObject(document);
	}
	
	private static String prettyPrintObject(Object object) {
		try {
			return jsonMapper
					.writerWithDefaultPrettyPrinter()
					.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static List<Map<String, Object>> toPlainJSON(List<DocumentDefinition> documents) {
		if(documents == null) {
			return null;
		}
		
		return documents.parallelStream()
					.map(d -> toPlainJSON(d))
					.collect(Collectors.toList());
	}
	
	public static Map<String, Object> toPlainJSON(DocumentDefinition document) {
		Map<String, Object> out = new HashMap<String, Object>();
	
		if(document!=null){
			document.getFields().parallelStream().forEach(f -> {
				final JCOValue value = f.getValue();
				
				if (value != null) {
					if (value.getType() == EValueType.DOCUMENT) {
						out.put(f.getName(), toPlainJSON((DocumentDefinition) value.getValue()));
					} else if (value.getType() == EValueType.ARRAY) {
						out.put(f.getName(), jsonArrayFromArray((ArrayValue) value));
					} else if (value instanceof GeometryValue) {
						String geojsonString = new GeoJSONWriter().write(((GeometryValue) value).getGeometry()).toString();
						try {
							out.put(f.getName(), jsonMapper.readValue(geojsonString, new TypeReference<Map<String, Object>>() {}));
						} catch (JsonProcessingException e) {
							out.put(f.getName(), null);
						}
					} else if(value.getType() == EValueType.DATE){
						out.put(f.getName(), (Date)value.getValue());
					} else if (value.getType() == EValueType.INTEGER) {
						out.put(f.getName(), ((Long) value.getValue()));
					} else if (value.getType().equals(EValueType.DECIMAL)) {
						out.put(f.getName(), ((Double) value.getValue()));
					} else if (value.getType() == EValueType.NULL) {
						out.put(f.getName(), null);
					} else if (value.getType() == EValueType.BOOLEAN) {
						out.put(f.getName(), (Boolean)value.getValue());
					} else {
						out.put(f.getName(), value.getValue());
					}
				}
	
			});
		
		}
		return out;
	}
	
	public static List<DocumentDefinition> fromPlainJSON(List<Map<String, Object>> jsonDocuments) {
		if(jsonDocuments == null) {
			return null;
		}
		
		return jsonDocuments.stream()
			.map(DocumentDefinitionUtils::fromPlainJSON)
			.collect(Collectors.toList());
	}
	
	public static DocumentDefinition fromPlainJSON(Map<String, Object> jsonDocument) {
		return new DocumentDefinition(jsonDocument.entrySet()
						.stream()
						.map(e -> fieldFromObject(e.getKey(), e.getValue()))
						.collect(Collectors.toList())
				);
	}
	
	private static FieldDefinition fieldFromObject(String key, Object value) {
		return new FieldDefinition(key, valueFromObject(key, value));
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> jsonArrayFromArray(ArrayValue value) {
		return value.getValues().stream().map(v -> {
			if (v.getType() == EValueType.DOCUMENT) {
				return toPlainJSON((DocumentDefinition) v.getValue());
			} else if (v.getType() == EValueType.ARRAY) {
				return jsonArrayFromArray(new ArrayValue(ArrayList.class.cast(v.getValue())));
			} else if (v.getType() == EValueType.INTEGER) {
				return ((Long)v.getValue());
			} else if (v.getType().equals(EValueType.DECIMAL)) {
				return ((Double)v.getValue());
			}else if (v.getType() == EValueType.NULL) {
				 return null;
			} else if (v.getType() == EValueType.GEOMETRY) {
				return ((GeometryValue)v.getValue());
			}
			return v.getValue();
		}).collect(Collectors.toList());
	}

	public static JCOValue valueFromObject(Object object) {
		return valueFromObject(null, object);
	}

	@SuppressWarnings("unchecked")
	public static JCOValue valueFromObject(String key, Object object) {
		JCOValue value = null;
		if (object instanceof String) {
			value = new SimpleValue((String) object);
		} else if (object instanceof Double) {
			value = new SimpleValue(new BigDecimal((Double) object));
		} else if (object instanceof Integer) {
			value = new SimpleValue((Integer) object);
		} else if (object instanceof Boolean) {
			value = new SimpleValue((Boolean) object);
		} else if(object instanceof Long){
			value = new SimpleValue((Long) object);
		} else if (object instanceof Float) {
			value = new SimpleValue(new BigDecimal((Float) object));
		} else if (object instanceof BigDecimal) {
			value = new SimpleValue((BigDecimal) object);
		} else if(object instanceof Date){
			value = new SimpleValue((Date)object);
		} else if (object instanceof Map) {
			if (GEOMETRY_FIELD_NAME.equals(key)) {
				try {
					value = new GeometryValue(new GeoJSONReader().read(
								jsonMapper.writeValueAsString(((Map<String, Object>) object)))
							);
				} catch (JsonProcessingException e) {
					value = null;
				}
			} else {
				value = new DocumentValue(((Map<String, Object>) object).entrySet().parallelStream()
						.map(e -> new FieldDefinition(e.getKey(), valueFromObject(e.getKey(), e.getValue())))
						.collect(Collectors.toList()));
			}
		} /* aggiunto 30-06-2017 */
		else if (object instanceof ArrayList) {
			ListIterator<JCOValue> litr = ArrayList.class.cast(object).listIterator();
			List<JCOValue> values = new ArrayList<JCOValue>();
			litr.forEachRemaining(v -> {
				values.add(valueFromObject(litr.next()));
			});
			value = new ArrayValue(values);
			/* aggiunto 5-07-2017 */
		} else if (object instanceof LinkedList) {
			LinkedList<Object> litr = ((LinkedList<Object>)object);
			List<JCOValue> values = new ArrayList<JCOValue>();
			for(Object v : litr) {
				values.add(valueFromObject(v));
			}
			value = new ArrayValue(values);
		} else if (object == null) {
			value = new SimpleValue();
		}
	
		return value;
	}
}
