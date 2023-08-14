package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.ExpressionTerm;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class ExpressionPredicateEvaluator {
	
	public static JCOValue calculate(Expression expr, Pipeline pipeline) {
		JCOValue v1, v2;
		v1 = retrieveTermValue (expr.getTerm(0), pipeline);
		if (Expression.SUB.equals(expr.getOperator(0))) {
			if (JCOValue.isIntValue(v1))
				v1 =  new SimpleValue (-JCOValue.getIntValue(v1));
			else if (JCOValue.isDoubleValue(v1))
				v1 =  new SimpleValue (-JCOValue.getDoubleValue(v1));
			else
				JMH.add("Operator non allowed");
		}
		
		for (int i=1; i<expr.getNTerms(); i++) {
			v2 = retrieveTermValue (expr.getTerm(i), pipeline);
			v1 = addTerms (v1, v2, expr.getOperator(i));
		}

		return v1;
	}

	
	/* ************************************************** */
	private static JCOValue retrieveTermValue (ExpressionTerm term, Pipeline pipeline) {
		JCOValue v1, v2;
		v1 = ExpressionFactorEvaluator.evaluate (term.getFactor(0), pipeline);

		for (int i=1; i<term.getNFactors(); i++) {
			v2 = ExpressionFactorEvaluator.evaluate (term.getFactor(i), pipeline);
			v1 = mulFactor (v1, v2, term.getOperator(i));
		}

		return v1;
	}

	
	public static JCOValue addTerms(JCOValue v1, JCOValue v2, String operator) {
		JCOValue value = new SimpleValue ();   // null value
		if (!JCOValue.isNull(v1) && !JCOValue.isNull(v2))
			if (Expression.ADD.equals(operator)) {
				if (((v1.getType() == EValueType.INTEGER) || (v1.getType() == EValueType.DECIMAL) || (v1.getType() == EValueType.STRING)) &&
					((v2.getType() == EValueType.INTEGER) || (v2.getType() == EValueType.DECIMAL) || (v2.getType() == EValueType.STRING))) {
					// concatenation
					if ((v1.getType() == EValueType.STRING) || (v2.getType() == EValueType.STRING)) 
						value = new SimpleValue (v1.getStringValue() + v2.getStringValue());
	
					// decimal sum
					else if (JCOValue.isDoubleValue(v1) || JCOValue.isDoubleValue(v2)) 
						value = new SimpleValue (JCOValue.getDoubleValue(v1) + JCOValue.getDoubleValue(v2));					
						
					// integer sum
					else 
						value = new SimpleValue (JCOValue.getIntValue(v1) + JCOValue.getIntValue(v2));					
				}
				// operation not allowed
				else 
					JMH.add("Operator non allowed");
			}
			// Expression.SUB
			else {
				if (JCOValue.isNumericValue(v1) && JCOValue.isNumericValue(v2)) {
					// decimal sub
					if (JCOValue.isDoubleValue(v1) || JCOValue.isDoubleValue(v2)) 
						value = new SimpleValue (JCOValue.getDoubleValue(v1) - JCOValue.getDoubleValue(v2));					
						
					// integer sub
					else 
						value = new SimpleValue (JCOValue.getIntValue(v1) - JCOValue.getIntValue(v2));					
				}
				// operation not allowed
				else 
					JMH.add("Operator non allowed");
			}

		return value;
	}

	
	public static JCOValue mulFactor(JCOValue v1, JCOValue v2, String operator) {
		JCOValue value = new SimpleValue ();   // null value
		if (!JCOValue.isNull(v1) && !JCOValue.isNull(v2))
			if (Expression.MUL.equals(operator)) {
				if (JCOValue.isNumericValue(v1) && JCOValue.isNumericValue(v2)) {
					// decimal mul
					if (JCOValue.isDoubleValue(v1) || JCOValue.isDoubleValue(v2)) 
						value = new SimpleValue (JCOValue.getDoubleValue(v1) * JCOValue.getDoubleValue(v2));					
	
					// integer mul
					else 
						value = new SimpleValue (JCOValue.getIntValue(v1) * JCOValue.getIntValue(v2));					
				}
				// operation not allowed
				else {
					JMH.add("Operator non allowed");
				}
			}
			// Expression.DIV
			else {
				if (JCOValue.isNumericValue(v1) && JCOValue.isNumericValue(v2)) {
					if (JCOValue.getDoubleValue(v2) == 0)
						JMH.add("Division by 0");
					// double div
					else 
						value = new SimpleValue (JCOValue.getDoubleValue(v1) /JCOValue.getDoubleValue(v2));					
				}
				// operation not allowed
				else 
					JMH.add("Operator non allowed");
			}

		return value;
	}

}
