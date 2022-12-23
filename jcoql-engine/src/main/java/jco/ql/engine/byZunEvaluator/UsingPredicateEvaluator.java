package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.UsingAggregatorPredicate;
import jco.ql.parser.model.predicate.UsingPredicate;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction 
 * - FI modified on 30/10/2022
 * */
public class UsingPredicateEvaluator implements JCOConstants {

	public static SimpleValue fuzzyEvaluate(UsingPredicate usingPredicate, Pipeline pipeline) {
		SimpleValue value = new SimpleValue ();	// null type
		if (usingPredicate.usingType == UsingPredicate.USING_FUZZY_SET)
			value = evaluateFuzzySet(usingPredicate, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_FUZZY_OPERATOR)
			value = FuzzyOperatorEvaluator.evaluate(usingPredicate, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_SUB_CONDITION)
			value = ConditionEvaluator.fuzzyEvaluate(usingPredicate.subUsingCondition, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_IF_FAILS)
			value = evaluateIfFails (usingPredicate, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_FUZZY_AGGREGATOR)//FI added
			value = FuzzyAggregatorEvaluator.evaluate((UsingAggregatorPredicate)usingPredicate, pipeline);
		
		return value;
	}

	
	private static SimpleValue evaluateFuzzySet(UsingPredicate usingPredicate, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value
		
		SimpleValue outValue = (SimpleValue)doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + usingPredicate.fuzzySet);
		if (outValue == null)
			return new SimpleValue ();		// null value			
		return outValue;
	}


	private static SimpleValue evaluateIfFails(UsingPredicate usingPredicate, Pipeline pipeline) {
		SimpleValue defaultValue = new SimpleValue (usingPredicate.getDefaultValue());
		SimpleValue outValue = null;
		try {
			outValue = ConditionEvaluator.fuzzyEvaluate(usingPredicate.subUsingCondition, pipeline);
			if ((outValue != null) && (outValue.getType() != EValueType.NULL))
					defaultValue = outValue;
		} catch (RuntimeException re) {
			JMH.add("Fuzzy Condition evaluation failed:\t" + usingPredicate.subUsingCondition);
		}
		return defaultValue;
	}

}
