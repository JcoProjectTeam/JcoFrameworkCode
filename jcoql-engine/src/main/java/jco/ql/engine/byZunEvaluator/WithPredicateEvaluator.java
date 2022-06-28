package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.Environment;
import jco.ql.parser.model.predicate.WithPredicate;
import jco.ql.parser.model.util.Field;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class WithPredicateEvaluator {

	public static SimpleValue evaluate(WithPredicate predicate, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();

		// evaluation in short-circuit
		for (Field f : predicate.getFieldsList()) {
			JCOValue value = doc.getValue(f.toString());
			if (value == null)
				 return new SimpleValue(false);
			else if (!checkValueType (value, predicate))
				 return new SimpleValue(false);
		}

		return new SimpleValue(true);
	}

	Environment e;
	private static boolean checkValueType(JCOValue value, WithPredicate predicate) {
		if (predicate.getSelectorType() == WithPredicate.UNDEFINED)
			return true;

		if (predicate.getSelectorType() == WithPredicate.INTEGER)
			return 	(value.getType() == EValueType.INTEGER);

		if (predicate.getSelectorType() == WithPredicate.FLOAT)
			return 	(value.getType() == EValueType.DECIMAL);

		if (predicate.getSelectorType() == WithPredicate.BOOLEAN)
			return 	(value.getType() == EValueType.BOOLEAN);
		
		if (predicate.getSelectorType() == WithPredicate.STRING)
			return 	(value.getType() == EValueType.STRING);
		
		if (predicate.getSelectorType() == WithPredicate.ARRAY)
			return 	(value.getType() == EValueType.ARRAY);

		if (predicate.getSelectorType() == WithPredicate.GEOMETRY)
			return 	(value.getType() == EValueType.GEOMETRY);

		if (predicate.getSelectorType() == WithPredicate.NUMBER)
			return 	(value.getType() == EValueType.DECIMAL) || (value.getType() == EValueType.INTEGER);

		if (predicate.getSelectorType() == WithPredicate.SIMPLE)
			return 	(value.getType() == EValueType.NULL) 	|| (value.getType() == EValueType.STRING) 	|| (value.getType() == EValueType.BOOLEAN) || 
					(value.getType() == EValueType.INTEGER)	|| (value.getType() == EValueType.DECIMAL);

		if (predicate.getSelectorType() == WithPredicate.COMPLEX)
			return 	(value.getType() == EValueType.ARRAY) 	|| (value.getType() == EValueType.DOCUMENT) || (value.getType() == EValueType.GEOMETRY);

		return false;
	}

}
