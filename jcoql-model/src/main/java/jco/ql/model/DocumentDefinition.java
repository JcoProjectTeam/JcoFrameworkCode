package jco.ql.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Field;

public class DocumentDefinition implements JCOConstants {

	private String name;

	private Map<String, JCOValue> fields;

	public DocumentDefinition() {
		this.name = null;
		this.fields = new TreeMap<String, JCOValue>();
	}

	// PF - Added on 16.07.2021
	public DocumentDefinition(String name) {
		this.name = name;
		this.fields = new TreeMap<String, JCOValue>();
	}

	public DocumentDefinition(List<FieldDefinition> fields) {
		this.name = null;
		this.fields = initFields(fields);
	}

	public DocumentDefinition(String name, List<FieldDefinition> fields) {
		this.name = name;
		this.fields = initFields(fields);
	}

	private Map<String, JCOValue> initFields(List<FieldDefinition> fields) {
		Map<String, JCOValue> localFields = new TreeMap<String, JCOValue>();
		
		for (FieldDefinition f : fields) {
			localFields.put(f.getName(), f.getValue());
		}
		return localFields;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public List<FieldDefinition> getFields() {
		return fields.entrySet().stream().map(e -> new FieldDefinition(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	// PF. Added on 17.05.2022 Insert recursively a structured field .a.b.c.
	public void insertField(Field field, JCOValue value) {
		FieldDefinition fd;
		String head = field.head();
		
		if (field.size() == 1) {
			fd = new FieldDefinition(head, value);
			addField(fd);
		}
		else {
			DocumentValue subDoc;
			fd = getField(head);
			if (fd == null || fd.getValue() == null || fd.getValue().getType() != EValueType.DOCUMENT) {
				subDoc = new DocumentValue();
				fd = new FieldDefinition(head, subDoc);
				addField(fd);
			}
			subDoc = (DocumentValue)fd.getValue();
			subDoc.document.insertField(field.cloneSuffix(), value);
		}
	}

	public void addField(FieldDefinition field) {
		this.fields.put(field.getName(), field.getValue());
	}

	@JsonIgnore
	public ArrayValue getArrayValue(String fieldName) {
		return getValue(fieldName, ArrayValue.class);
	}

	// added by Balicco
	public void addDocument(String name, List<FieldDefinition> fd) {
		DocumentValue dv = new DocumentValue(fd);
		this.fields.put(name, dv);
	}
	
	// added by Balicco
	public void addDocument(String name, DocumentValue dv) {
		this.fields.put(name, dv);
	}

	@JsonIgnore
	public SimpleValue getSimpleValue(String fieldName) {
		return getValue(fieldName, SimpleValue.class);
	}

	@JsonIgnore
	public <T> T getValue(String fieldName, Class<T> clazz) {
		JCOValue value = getValue(fieldName);
		if(value != null && value.getClass().getTypeName().equals(clazz.getTypeName())) {
			return clazz.cast(value);
		}
		return null;
	}

	@JsonIgnore
	public JCOValue getValue(String fieldName) {
		return getValue(FieldName.fromString(fieldName));
	}

	@JsonIgnore
	public JCOValue getValue(FieldName fieldName) {
		JCOValue value = null;
		if (fieldName.getLevel() == 1)
			value = fields.get(fieldName.getParts()[0]);
		else 
			// aggiunto, deve partire da 1 non da 0!!!!!!!
			value = getValue(fields.get(fieldName.getParts()[0]), fieldName.getParts(), 1);

		return value;
	}

	@JsonIgnore
	private JCOValue getValue(JCOValue value, String[] parts, int i) {
		JCOValue v = null;
		if (i == parts.length) {
			v = value;
		} 
		else if (value instanceof DocumentValue) {
			FieldName fn = FieldName.fromParts(Arrays.copyOfRange(parts, 1, parts.length));
			JCOValue jv = ((DocumentValue) value).getValue(fn);
			v = getValue(jv, parts, parts.length);
		}
		return v;
	}

	public JCOValue removeValue(String fieldName) {
		return removeValue(FieldName.fromString(fieldName));
	}

	public JCOValue removeValue(FieldName fieldName) {
		JCOValue value = null;
		if (fieldName.getLevel() == 1) {
			value = fields.get(fieldName.getParts()[0]);
			fields.remove(fieldName.getParts()[0]);
		} else {
			// aggiunto, deve partire da 1 non da 0!!!!!!!
			value = removeValue(fields.get(fieldName.getParts()[0]), fieldName.getParts(), 1);
		}

		return value;
	}

	private JCOValue removeValue(JCOValue value, String[] parts, int i) {
		JCOValue v = null;
		if (i == parts.length) {
			v = value;

		} else if (value instanceof DocumentValue) {
			v = removeValue(((DocumentValue) value)
					.removeValue(FieldName.fromParts(Arrays.copyOfRange(parts, 1, parts.length))), parts, parts.length);
		}
		return v;
	}

	public boolean hasField(FieldName fieldName) {
		return getValue(fieldName) != null;
	}

	public boolean hasField(String fieldName) {
		return hasField(FieldName.fromString(fieldName));
	}
	public FieldDefinition getField(String fieldName) {
		return new FieldDefinition (fieldName, getValue(fieldName));
	}
	public FieldDefinition getField(Field field) {
		return new FieldDefinition (field.toString(), getValue(field.toString()));
	}

	@JsonIgnore
	public List<JCOValue> getValues(List<FieldName> fieldNames) {
		List<JCOValue> list = new ArrayList<JCOValue>();

		for (FieldName fieldName : fieldNames) 
			list.add(getValue(fieldName));

		return list;
	}

	@Override
	public String toString() {
		// ZUN CHECK
		//rimuovo ID per comodit� perch� in Elasticsearch crea problemi
		//in realt� non dovrebbe esserci questo pezzo
//		if(fields.containsKey(MONGODB_ID_FIELD_NAME))
//			fields.remove(MONGODB_ID_FIELD_NAME);
		StringBuffer output = new StringBuffer ("{\n");
		if (name != null) {
			output.append("\t\"name\": \"" + name + "\",\n");
		}
		StringJoiner joiner = new StringJoiner(",\n");
		Iterator<String> keys = fields.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if(fields.get(key) != null) {
				if (fields.get(key).getType() == EValueType.DATE)
					joiner.add("\t\"" + key + "\": " + convertToISO((Date) fields.get(key).getValue()) + "");
				if (fields.get(key).getType() == EValueType.STRING)
					joiner.add("\t\"" + key + "\": \""
							+ fields.get(key).getStringValue()
							.replace("\r\n", " ").replace("\n", " ")
							+ "\"");
				else
					joiner.add("\t\"" + key + "\": "
							+ fields.get(key).getStringValue()
							.replace("\r\n", " ").replace("\n", " ")
							+ "");
			}
		}
		output.append(joiner.toString());
		output.append("\n}");
		return output.toString();
	}

	private String convertToISO(Date d) {
		//formato ISO 8601
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return "\"" + df.format(d) + "\"";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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

		DocumentDefinition other = (DocumentDefinition) obj;
		if (fields == null) {
			if (other.fields != null) {

				return false;
			}
		} else if (!fields.equals(other.fields)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {

				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Per l'inserimento dei dati in Elasticsearch non possono esserci dei campi che
	 * contengono "_" Questo metodo controlla se sono presenti o meno dei campi con
	 * questa caratteristica
	 *
	 * @return true: se nel documento ci sono dei campi che iniziano con "_" false:
	 *         altrimenti
	 */
	public boolean checkUnderscores() {
		Iterator<String> i = fields.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (key.startsWith("_"))
				return true;
		}
		return false;
	}

	@JsonProperty("fields")
	public Map<String, JCOValue> getMap(){
		Map<String, JCOValue> campi = new TreeMap<>();
		campi.putAll(fields);

		if(campi.containsKey(MONGODB_ID_FIELD_NAME))
			campi.remove(MONGODB_ID_FIELD_NAME);
		return campi;
	}

	public void setMap(Map<String, JCOValue> field){
		this.fields = field;
	}

	// added by Balicco
	public boolean isSimple() {
		if (name == null && fields.size() == 1) {
			return true;
		}
		return false;
	}
}
