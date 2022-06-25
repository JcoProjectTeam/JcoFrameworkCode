package jco.ql.model.builder;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.wololo.jts2geojson.GeoJSONReader;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.FieldName;
import jco.ql.model.reference.FieldReference;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.FieldValue;
import jco.ql.model.value.GeoJsonValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Field;

public class FieldDefinitionBuilder {

	private DocumentDefinitionBuilder parentDocument;

	private FieldDefinition field;


	public FieldDefinitionBuilder() {
		super();
		parentDocument = null;
		field = null;
	}

	FieldDefinitionBuilder(DocumentDefinitionBuilder parentDocument) {
		this.parentDocument = parentDocument;
	}

	public FieldDefinitionBuilder fromString(String name, String value) {
		field = new FieldDefinition(name, new SimpleValue(value));
		return this;
	}

	public FieldDefinitionBuilder fromBoolean(String name, Boolean value) {
		field = new FieldDefinition(name, new SimpleValue(value));
		return this;
	}

	public FieldDefinitionBuilder fromInteger(String name, Integer value) {
		field = new FieldDefinition(name, new SimpleValue(value));
		return this;
	}

	public FieldDefinitionBuilder fromDecimal(String name, BigDecimal value) {
		field = new FieldDefinition(name, new SimpleValue(value));
		return this;
	} 
	



	
	//EC NUOVO		con il substring elimino il punto "." iniziale al nome del campo
	public FieldDefinitionBuilder fromFieldReference(Field fieldRef, FieldReference value) {
		field = new FieldDefinition(fieldRef.fields.get(0).substring(1), new FieldValue(value));
		return this;
	}

	public FieldDefinitionBuilder fromFieldReference(String name, FieldReference value) {
		field = new FieldDefinition(name, new FieldValue(value));
		return this;
	}

	public FieldDefinitionBuilder fromFieldReference(String name, String collectionAlias, String fieldName) {
		field = new FieldDefinition(name, new FieldValue(collectionAlias, fieldName));
		return this;
	}

	public FieldDefinitionBuilder fromFieldReference(String collectionAlias, String fieldName) {
		final FieldValue value = new FieldValue(collectionAlias, fieldName);
		field = new FieldDefinition(value.getFieldReference().getFieldName().getFirstLevelName(), value);
		return this;
	}

	public FieldDefinitionBuilder fromFieldReference(String name, String collectionAlias, FieldName fieldName) {
		field = new FieldDefinition(name, new FieldValue(collectionAlias, fieldName));
		return this;
	}

	public FieldDefinitionBuilder fromFieldReference(String collectionAlias, FieldName fieldName) {
		field = new FieldDefinition(fieldName.getFirstLevelName(), new FieldValue(collectionAlias, fieldName));
		return this;
	}

	//EC NUOVO
	public FieldDefinitionBuilder fromValue(Field fieldRef, JCOValue fieldValue) {
		field = new FieldDefinition(fieldRef.fields.get(0).substring(1), fieldValue);
		return this;
	}

	public FieldDefinitionBuilder fromValue(String fieldName, JCOValue fieldValue) {
		field = new FieldDefinition(fieldName, fieldValue);
		return this;
	}

	public FieldDefinitionBuilder fromList(String fieldName, List<JCOValue> values) {
		field = new FieldDefinition(fieldName, new ArrayValue(values));
		return this;
	}

	public FieldDefinitionBuilder fromStringList(String fieldName, List<String> stringValues) {
		List<JCOValue> values = new LinkedList<JCOValue>();
		if(stringValues != null) {
			values = stringValues.stream().map(SimpleValue::new).collect(Collectors.toList());
		}
		field = new FieldDefinition(fieldName, new ArrayValue(values));
		return this;
	}

	public FieldDefinitionBuilder fromDocumentList(String fieldName, List<DocumentDefinition> documents) {
		return fromList(fieldName, documents.stream().map(DocumentValue::new).collect(Collectors.toList()));
	}

	//EC NUOVO
	public DocumentDefinitionBuilder fromDocument(Field fieldRef) {
		return new DocumentDefinitionBuilder(fieldRef.fields.get(0).substring(1), this);
	}

	public DocumentDefinitionBuilder fromDocument(String name) {
		return new DocumentDefinitionBuilder(name, this);
	}

	public FieldDefinitionBuilder fromDocument(String name, DocumentValue value) {
		this.field = new FieldDefinition(name, value);
		return this;
	}

	public FieldDefinitionBuilder fromGeoJsonString(String name, String geoJsonString) {
		field = new FieldDefinition(name, new GeoJsonValue(new GeoJSONReader().read(geoJsonString)));
		return this;
	}

	public FieldDefinitionBuilder fromGeoJson(String name, GeoJsonValue geoJson) {
		field = new FieldDefinition(name, geoJson);
		return this;
	}

	public FieldDefinition build() {
		return field;
	}

	public DocumentDefinitionBuilder add() {
		return parentDocument.addFieldDefinition(field);
	}



}
