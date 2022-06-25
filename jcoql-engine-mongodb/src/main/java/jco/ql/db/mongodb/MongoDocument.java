package jco.ql.db.mongodb;

import java.util.Map;

import org.bson.Document;

import jco.ql.db.mongodb.utils.ValueUtils;
import jco.ql.model.FieldName;
import jco.ql.model.engine.IDocument;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;

public class MongoDocument extends Document implements IDocument {

	private static final long serialVersionUID = 1L;

	@Override
	public void putAll(Map<? extends String, ? extends Object> map) {
		super.putAll(map);
	}

	@Override
	public JCOValue getValue(FieldName fieldName) {
		String[] parts = fieldName.getParts();
		int level = fieldName.getLevel();
		JCOValue value = null;
		value = ValueUtils.fromObject(this.get(parts[0]));
		if (level > 1) {
			if (value.getType() == EValueType.DOCUMENT) {
				/*
				 * { A : { B : { C : 1 } } } A.B.C => 1
				 */
//				value = getValue((DocumentValue) value, parts, level, 1);
			} else {
				value = null;
			}
		}
		return value;
	}
/*
	private Value getValue(DocumentValue parent, String parts, int level, int i) {
		Value value = null;
		if (i < level) {
//			value = parent.getFieldValue(parts[i]);
		}
		return value;
	}
*/
}
