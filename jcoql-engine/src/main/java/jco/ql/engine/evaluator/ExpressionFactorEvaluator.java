package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ExpressionFactor;
import jco.ql.parser.model.predicate.FunctionFactor;
import jco.ql.parser.model.predicate.SpecialFunctionFactor;
import jco.ql.parser.model.util.Value;


public class ExpressionFactorEvaluator implements JCOConstants {

	public static JCOValue evaluate(ExpressionFactor factor, Pipeline pipeline) {
		JCOValue jv = new SimpleValue ();

		if (factor.getType() == ExpressionFactor.SUB_CONDITION)
			jv  = ConditionEvaluator.evaluate(factor.subCondition, pipeline);
		
		else if (factor.getType() == ExpressionFactor.SUB_EXPRESSION)
			jv  = ExpressionPredicateEvaluator.calculate(factor.subExpression, pipeline);
		
		//FI modified on 05/11/2022
		else if (factor.getType() == ExpressionFactor.VALUE)
			jv  = getFactorValue (factor, pipeline);

		else if (factor.getType() == ExpressionFactor.ID)
			jv  = getIDValue (factor, pipeline);

		else if (factor.getType() == ExpressionFactor.FIELDNAME)
			jv  = getFieldValue (factor, pipeline);
		
		else if (factor.getType() == ExpressionFactor.FUNCTION)
			jv  = FunctionEvaluator.evaluate ((FunctionFactor)factor, pipeline);

		else if (factor.getType() == ExpressionFactor.SPECIAL_FUNCTION)
			jv  = SpecialFunctionEvaluator.evaluate ((SpecialFunctionFactor)factor, pipeline);
		
		//FI modified on 05/11/2022
		else if(factor.getType() == ExpressionFactor.ARRAY_REF)
			jv  = ArrayReferenceEvaluator.evaluate(factor.reference, pipeline);
		
		//PF added on 08/08/2023
		else if(factor.getType() == ExpressionFactor.ARRAY)
			jv  = getArrayValue(factor, pipeline);
		
		// PF 07/.2023 - after the factor has been evaluated, check if it has an exponent
		else if (factor.hasExponent())
			jv = getExponentialValue (jv, factor, pipeline);
		
		return jv;	
	}


	private static JCOValue getArrayValue(ExpressionFactor factor, Pipeline pipeline) {
		ArrayValue av = new ArrayValue();
		for (ExpressionFactor f: factor.array) 
			av.add(ExpressionFactorEvaluator.evaluate(f, pipeline));

		return av;
	}


	private static JCOValue getExponentialValue(JCOValue jv, ExpressionFactor factor, Pipeline pipeline) {
		if (!JCOValue.isNumericValue(jv)) {
			JMH.add("Operator non allowed for non-numeric base:\t" + jv.toString());
			return new SimpleValue();	// null value
		}
			
		JCOValue exp = ExpressionFactorEvaluator.evaluate(factor.exp, pipeline);
		if (!JCOValue.isNumericValue(exp)) {
			JMH.add("Operator non allowed for non-numeric exponent:\t" + exp.toString());
			return new SimpleValue();	// null value
		}
		
		double b = JCOValue.getDoubleValue(jv);
		double e = JCOValue.getDoubleValue(exp);
		double v = Math.pow(b, e);
		JCOValue value = new SimpleValue (v);
		return value; 
	}

	/* ***************************************************************** */

	//FI modified on 05/11/2022
	public static SimpleValue getFactorValue(ExpressionFactor factor, Pipeline pipeline) {
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
		JCOValue jv = doc.getValue(factor.field.toString());
		if (jv == null)
			jv = new SimpleValue();
		return jv;
	}


	

}

