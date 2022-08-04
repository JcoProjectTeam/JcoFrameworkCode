package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.NullPredicate;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class NullPredicateEvaluator {

	public static SimpleValue evaluate(NullPredicate predicate, Pipeline pipeline) {
		SimpleValue value = new SimpleValue(false);
		DocumentDefinition doc = pipeline.getCurrentDoc();
		JCOValue v = doc.getValue(predicate.fieldRef.toString());

		if (predicate.getCheckType() == NullPredicate.CHECK_NULL && (v == null || v.getType() == EValueType.NULL)) 
			return new SimpleValue (true);
		else if (predicate.getCheckType() == NullPredicate.CHECK_NOT_NULL && v != null && v.getType() != EValueType.NULL)
			return new SimpleValue (true);
			
		return value;
	}

	
}
