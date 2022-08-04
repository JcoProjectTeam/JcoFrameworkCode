package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ExpressionFactor;
import jco.ql.parser.model.predicate.FunctionFactor;
import jco.ql.parser.model.predicate.SpecialFunctionFactor;
import jco.ql.parser.model.util.Value;


public class ExpressionFactorEvaluator implements JCOConstants {

	public static JCOValue evaluate(ExpressionFactor factor, Pipeline pipeline) {

		if (factor.getType() == ExpressionFactor.SUB_CONDITION)
			return ConditionEvaluator.evaluate(factor.subCondition, pipeline);
		
		if (factor.getType() == ExpressionFactor.SUB_EXPRESSION)
			return ExpressionPredicateEvaluator.calculate(factor.subExpression, pipeline);
		
		if (factor.getType() == ExpressionFactor.VALUE)
			return getFactorValue (factor);

		if (factor.getType() == ExpressionFactor.ID)
			return getIDValue (factor, pipeline);

		if (factor.getType() == ExpressionFactor.FIELDNAME)
			return getFieldValue (factor, pipeline);
		
		if (factor.getType() == ExpressionFactor.FUNCTION)
			return FunctionEvaluator.evaluate ((FunctionFactor)factor, pipeline);

		if (factor.getType() == ExpressionFactor.SPECIAL_FUNCTION)
			return SpecialFunctionEvaluator.evaluate ((SpecialFunctionFactor)factor, pipeline);

		return new SimpleValue ();		// null value
	}

	/* ***************************************************************** */

	
	public static SimpleValue getFactorValue(ExpressionFactor factor) {
		Value v = factor.value;
		if (v == null)
			return new SimpleValue ();		// null value

		if (v.isInt())
			return new SimpleValue (new Integer(v.value));
			
		if (v.isFloat())
			return new SimpleValue (new Double(v.value));

		if (v.isBoolean())
			return new SimpleValue (new Boolean(v.value));
		
		if (v.isQuoted())
			return new SimpleValue (v.value);

		if (v.isApex())
			return new SimpleValue (v.value);

		return new SimpleValue ();		// null value
	}


	// used for parameters for FO Operators or JS Functions
	private static JCOValue getIDValue(ExpressionFactor factor, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value

		return doc.getValue(factor.idName);
	}


	private static JCOValue getFieldValue(ExpressionFactor factor, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value

		return doc.getValue(factor.field.toString());
	}


	

}

