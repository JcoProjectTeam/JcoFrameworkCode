package jco.ql.db.ds.core.datatype.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class JcoValueDeserializer extends StdDeserializer<JCOValue>{
	private static final Logger logger = LoggerFactory.getLogger(JcoValueDeserializer.class);

	private static final long serialVersionUID = 1L;
	private final GeoJSONReader geoJsonReader;

	public JcoValueDeserializer() {
		this(null);
	}

	protected JcoValueDeserializer(Class<?> vc) {
		super(vc);
		geoJsonReader = new GeoJSONReader();
	}

	@Override
	public JCOValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		return decodeValue(node);
	}
	
	private JCOValue decodeValue(JsonNode node) {
		JCOValue value = null;
		String valueType = null;
		try {
			valueType = node.get("type").asText();
			if(valueType != null && !valueType.isEmpty()) {
				EValueType type = EValueType.valueOf(valueType);
				
				switch (type) {
				case ARRAY:
					List<JCOValue> values = new ArrayList<JCOValue>();
					if(node.get("value").isArray()) {
						node.get("value").forEach(v -> {
							values.add(decodeValue(v));
						});
						value = new ArrayValue(values);
					}
					break;
					
				case DOCUMENT:
					if(node.get("value").isObject()) {
						DocumentDefinition document = new DocumentDefinition();
						JsonNode nameNode = node.findValue("name");
						JsonNode propertiesNode = node.findValue("map");
						if(nameNode != null && !nameNode.isNull()) {
							document.addField(new FieldDefinition("name", new SimpleValue(nameNode.asText())));
						}
						if(propertiesNode != null && !propertiesNode.isNull()) {
							Iterator<Entry<String, JsonNode>> fields = propertiesNode.fields();
							while(fields.hasNext()) {
								Entry<String, JsonNode> fieldEntry = fields.next();
								document.addField(new FieldDefinition(fieldEntry.getKey(), decodeValue(fieldEntry.getValue())));
							}
						}
						value = new DocumentValue(document);
					}
					break;
					
				case GEOMETRY:
					JsonNode nodeValue = node.get("value");
					if(nodeValue != null && !nodeValue.isNull()) {
						try {
							String nodeValueString = nodeValue.toString();
							value = new GeometryValue(geoJsonReader.read(nodeValueString));
						} catch (Exception e) {
							logger.error("Invalid geometry value", e);
						}
					}
					break;
					
				case STRING:
					value = new SimpleValue(node.get("value").asText());
					break;
				case INTEGER:
					value = new SimpleValue(node.get("value").asInt());
					break;
				case BOOLEAN:
					value = new SimpleValue(node.get("value").asBoolean());
					break;
				case DATE:
					value = new SimpleValue(new Date(node.get("value").asLong()));
					break;
				case DECIMAL:
					value = new SimpleValue(new BigDecimal(node.get("value").asText()));
					break;
				case NULL:
					value = new SimpleValue();
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error while decoding value of type " + valueType, e);
		}
		return value;
	}

}
