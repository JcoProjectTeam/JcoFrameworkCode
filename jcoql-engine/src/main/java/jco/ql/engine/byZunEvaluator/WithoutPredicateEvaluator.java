package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.WithoutPredicate;
import jco.ql.parser.model.util.Field;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class WithoutPredicateEvaluator {

	public static SimpleValue evaluate(WithoutPredicate predicate, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();

		// evaluation in short-circuit
		for (Field f : predicate.getFieldsList()) 
			if (doc.getValue(f.toString()) != null)
				 return new SimpleValue(false);

		return new SimpleValue(true);
	}

}
