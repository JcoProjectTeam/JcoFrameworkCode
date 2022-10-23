package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.condition.ConditionAnd;
import jco.ql.parser.model.condition.ConditionNot;
import jco.ql.parser.model.condition.ConditionOr;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class ConditionEvaluator implements JCOConstants {

	public static boolean matchCondition (Condition condition, Pipeline pipeline) {
		boolean b = false;
		JCOValue sv = evaluate (condition, pipeline);
		if (!JCOValue.isNull(sv)) {
			if (sv.getType() == EValueType.BOOLEAN)
				b = "true".equals(sv.getStringValue());
			else if (JCOValue.isNumericValue(sv))
				b = (JCOValue.getDoubleValue(sv) != 0);
			else if (sv.getType() == EValueType.STRING)
				b = !(sv.getStringValue().isEmpty()) ;			
			else if (sv.getType() == EValueType.ARRAY)
				b = ((ArrayValue)sv).getValues().size() > 0 ;			
			else if (sv.getType() == EValueType.DOCUMENT)
				b = ((DocumentValue)sv).getFields().size() > 0;
		}
		return b;
	}
	

	public static JCOValue evaluate (Condition condition, Pipeline pipeline) {
		JCOValue value = new SimpleValue(); // null Value
		
		if (condition == null)
			return value;

		if (condition.getType() == Condition.OR_CONDITION)
			value = evaluateConditionOr ((ConditionOr)condition, pipeline);

		else if (condition.getType() == Condition.AND_CONDITION)
			value = evaluateConditionAnd ((ConditionAnd)condition, pipeline);
		
		else if (condition.getType() == Condition.NOT_CONDITION)
			value = evaluateConditionNot ((ConditionNot)condition, pipeline);
		
		else if (condition.getType() == Condition.PREDICATE_CONDITION)
			value = PredicateEvaluator.evaluate(condition.getPredicate(), pipeline);

		return value;
	}


	public static SimpleValue fuzzyEvaluate (Condition condition, Pipeline pipeline) {
		SimpleValue value = new SimpleValue();	// null Value

		if (condition == null)
			return value;

		if (condition.getType() == Condition.OR_CONDITION)
			value = fuzzyEvaluateConditionOr ((ConditionOr)condition, pipeline);
		
		else if (condition.getType() == Condition.AND_CONDITION)
			value = fuzzyEvaluateConditionAnd ((ConditionAnd)condition, pipeline);
		
		else if (condition.getType() == Condition.NOT_CONDITION)
			value = fuzzyEvaluateConditionNot ((ConditionNot)condition, pipeline);
		
		else if (condition.getType() == Condition.PREDICATE_CONDITION)
			value = PredicateEvaluator.fuzzyEvaluate (condition.getPredicate(), pipeline);

		return value;
	}


	/* ************ Classic Evaluation *********************************************** */
	// evaluation in short-circuit
	private static SimpleValue evaluateConditionOr (ConditionOr condition, Pipeline pipeline) {
		for (Condition c : condition.getSubConditions())
			if (matchCondition(c, pipeline))
				return new SimpleValue(true);

		return new SimpleValue(false);		
	}
	

	// evaluation in short-circuit
	private static SimpleValue evaluateConditionAnd (ConditionAnd condition, Pipeline pipeline) {
		for (Condition c : condition.getSubConditions())
			if (!matchCondition(c, pipeline))
				return new SimpleValue(false);

		return new SimpleValue(true);		
	}
	
	
	private static SimpleValue evaluateConditionNot (ConditionNot condition, Pipeline pipeline) {
		if (matchCondition(condition.getSubCondition(), pipeline))
			return new SimpleValue(false);

		return new SimpleValue(true);		
	}
	

	/* ************ Fuzzy Evaluation *********************************************** */
	private static SimpleValue fuzzyEvaluateConditionOr (ConditionOr condition, Pipeline pipeline) {
		SimpleValue value = new SimpleValue (0);
		for (Condition c : condition.getSubConditions())
			value = fuzzyMax (value, fuzzyEvaluate (c, pipeline));

		return value;		
	}
	

	private static SimpleValue fuzzyEvaluateConditionAnd (ConditionAnd condition, Pipeline pipeline) {
		SimpleValue value = new SimpleValue (1);
		for (Condition c : condition.getSubConditions())
			value = fuzzyMin(value, fuzzyEvaluate (c, pipeline));

		return value;		
	}
	
	
	private static SimpleValue fuzzyEvaluateConditionNot (ConditionNot condition, Pipeline pipeline) {
		SimpleValue value = fuzzyEvaluate(condition.getSubCondition(), pipeline);

		return fuzzyComplement(value);
	}
	

	private static SimpleValue fuzzyMax (SimpleValue v1, SimpleValue v2) {
		if (v1 == null || v1.getType() == EValueType.NULL || v2 == null || v2.getType() == EValueType.NULL)
			return new SimpleValue ();

		if (v1.compareTo(v2) == GREATER_THAN)
			return v1;
		return v2;
	}

	private static SimpleValue fuzzyMin (SimpleValue v1, SimpleValue v2) {
		if (v1 == null || v1.getType() == EValueType.NULL || v2 == null || v2.getType() == EValueType.NULL)
			return new SimpleValue ();

		if (v1.compareTo(v2) == GREATER_THAN)
			return v2;
		return v1;
	}
	
	private static SimpleValue fuzzyComplement (SimpleValue value) {
		if (value == null || value.getType() == EValueType.NULL)
			return new SimpleValue ();
		
		return new SimpleValue (1 - Double.parseDouble(value.getStringValue()));
	}
}
