package jco.ql.model.engine;

import jco.ql.model.FieldName;
import jco.ql.model.value.JCOValue;

public interface IDocument {

	public JCOValue getValue(FieldName fieldName);
}
