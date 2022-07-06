package jco.ql.db.mongodb.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.BsonNull;
import org.bson.Document;
import org.wololo.jts2geojson.GeoJSONWriter;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;

public final class DocumentUtils {

	public static DocumentDefinition mapDocumentDefinitionFromBson(Document bsonDocument) {
		DocumentDefinition document = new DocumentDefinition(bsonDocument.entrySet()
				.stream()
				.map(e -> new FieldDefinition(e.getKey(), ValueUtils.fromObject(e.getKey(), e.getValue())))
				.collect(Collectors.toList()));
		return document;
	}

	public static Document bsonFromDocumentDefinition(DocumentDefinition document) {
		Document out = new Document();
	
		if(document!=null){
			document.getFields().stream().forEach(f -> {
				final JCOValue value = f.getValue();
				
				if (value != null) {
					if (value.getType() == EValueType.DOCUMENT) {
						out.append(f.getName(), bsonFromDocumentDefinition((DocumentDefinition) value.getValue()));
					} 
					else if (value.getType() == EValueType.ARRAY) {
						out.append(f.getName(), bsonArrayFromArray((ArrayValue) value));
					} 
					else if (value instanceof GeometryValue) {
						String geojsonString = new GeoJSONWriter().write(((GeometryValue) value).getGeometry()).toString();
						out.append(f.getName(), Document.parse(geojsonString));
					} // aggiunta gestione dei numerici:soluzione semplice
					/**/
					else if(value.getType() == EValueType.DATE){
						out.append(f.getName(), (Date)value.getValue());
					}
					else if (value.getType() == EValueType.INTEGER) {
						//out.append(f.getName(), ((BigDecimal) value.getValue()).intValue());
						out.append(f.getName(), ((Long) value.getValue()));
					} 
					else if (value.getType().equals(EValueType.DECIMAL)) {
						//out.append(f.getName(), ((BigDecimal) value.getValue()).doubleValue());
						out.append(f.getName(), ((Double) value.getValue()));
					}
					
					else if (value.getType() == EValueType.NULL) {
						out.append(f.getName(), new BsonNull());
	
					} /**/
					else if (value.getType() == EValueType.BOOLEAN) {
						out.append(f.getName(), (Boolean)value.getValue());
	
					} /**/
					else {
						out.append(f.getName(), value.getValue());
					}
				}
	
			});
		
		}
			return out;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> bsonArrayFromArray(ArrayValue value) {
		return value.getValues().stream().map(v -> {
			if (v.getType() == EValueType.DOCUMENT) {
				return bsonFromDocumentDefinition((DocumentDefinition) v.getValue());
			} 
			else if (v.getType() == EValueType.ARRAY) {
				//return bsonArrayFromArray((ArrayValue) v.getValue());
				return bsonArrayFromArray(new ArrayValue(ArrayList.class.cast(v.getValue())));

			} // aggiunto
			/**/
			else if (v.getType() == EValueType.INTEGER) {
				//return ((BigDecimal) v.getValue()).intValue();
				return ((Long)v.getValue());
			} 
			else if (v.getType().equals(EValueType.DECIMAL)) {
				//return ((BigDecimal) v.getValue()).doubleValue();
				return ((Double)v.getValue());
			}
			else if (value.getType() == EValueType.NULL) {
				 return new BsonNull();
			} 
			/**/
			return v.getValue();
		}).collect(Collectors.toList());
	}
	
}
