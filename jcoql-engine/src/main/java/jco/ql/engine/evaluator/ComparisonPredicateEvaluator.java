package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ComparisonPredicate;
import jco.ql.parser.model.predicate.InRangePredicate;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class ComparisonPredicateEvaluator implements JCOConstants {

	public static SimpleValue evaluate(ComparisonPredicate predicate, Pipeline pipeline) {
		JCOValue v1 = ExpressionPredicateEvaluator.calculate(predicate.expression1, pipeline);
		JCOValue v2 = ExpressionPredicateEvaluator.calculate(predicate.expression2, pipeline);

		if (v1 == null) {
			JMH.addExceptionMessage("************* V1 null **** TryAgain **********"
					+ "\n" + predicate.expression1.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			v1 = ExpressionPredicateEvaluator.calculate(predicate.expression1, pipeline);
			if (v1 == null) {
				JMH.addExceptionMessage("*************V1 null NOOOOOOOOOOOOOO ########################### ********************"
						+ "\n" + predicate.expression1.toString()
						+ "\n" + pipeline.getCurrentDoc().toString() );
				return new SimpleValue (); // null value as predicate cannot be evaluated
			}
		}
		if (v2 == null) {
			JMH.addExceptionMessage("************* V2 null **** TryAgain **********"
					+ "\n" + predicate.expression2.toString()
					+ "\n" + pipeline.getCurrentDoc().toString());	
			if (v2 == null) {
				JMH.addExceptionMessage("*************V2 null ********************"
						+ "\n" + predicate.expression2.toString()
						+ "\n" + pipeline.getCurrentDoc().toString());
				return new SimpleValue (); // null value as predicate cannot be evaluated
			}
		}

		if (v1 instanceof SimpleValue && v2 instanceof SimpleValue) {
			SimpleValue sv1 = (SimpleValue) v1;
			SimpleValue sv2 = (SimpleValue) v2;

			int comp = sv1.compareTo(sv2);
			if (comp == UNCOMPARABLE)
				return new SimpleValue (); // null value as predicate cannot be evaluated
			
			if (predicate.comparatorType == ComparisonPredicate.COMP_EQ)
				if (comp == EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);

			if (predicate.comparatorType == ComparisonPredicate.COMP_NEQ)
				if (comp != EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
			
			if (predicate.comparatorType == ComparisonPredicate.COMP_GT)
				if (comp > EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
			
			if (predicate.comparatorType == ComparisonPredicate.COMP_GE)
				if (comp >= EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
			
			if (predicate.comparatorType == ComparisonPredicate.COMP_LT)
				if (comp < EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
			
			if (predicate.comparatorType == ComparisonPredicate.COMP_LE)
				if (comp <= EQUAL)
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
		}
		else {			
			if ((predicate.comparatorType == ComparisonPredicate.COMP_EQ) && (v1.getType() == v2.getType())) 
				if (v1.getStringValue().equals(v2.getStringValue()))
					return new SimpleValue (true);
				else
					return new SimpleValue (false);

			if (predicate.comparatorType == ComparisonPredicate.COMP_NEQ) {
				if (v1.getType() != v2.getType()) 
					return new SimpleValue (true);
				else if (!v1.getStringValue().equals(v2.getStringValue()))
					return new SimpleValue (true);
				else
					return new SimpleValue (false);
			}
		}

		return new SimpleValue (); // null value as predicate cannot be evaluated
	}

	
	public static SimpleValue evaluateInRange(InRangePredicate predicate, Pipeline pipeline) {
		JCOValue v = ExpressionPredicateEvaluator.calculate(predicate.expr, pipeline);
		if (!JCOValue.isNumericValue(v))
			return new SimpleValue (); // null value as predicate cannot be evaluated		
		
		double value = Double.parseDouble(((SimpleValue)v).getStringValue());
		double lowerBound = predicate.leftBound;
		double upperBound = predicate.rightBound;
		
		if (value > lowerBound && value < upperBound)
			return new SimpleValue (true);
		if (predicate.includedLeft() && value == lowerBound)
			return new SimpleValue (true);
		if (predicate.includedRight() && value == upperBound)
			return new SimpleValue (true);

		return new SimpleValue (false);
	}
	

	public static boolean compare (JCOValue v1, JCOValue v2, int comparatorType) {
		if (v1 instanceof SimpleValue && v2 instanceof SimpleValue) {
			SimpleValue sv1 = (SimpleValue) v1;
			SimpleValue sv2 = (SimpleValue) v2;

			int comp = sv1.compareTo(sv2);
			if (comp == UNCOMPARABLE)
				return false; // null value as predicate cannot be evaluated
			
			if (comparatorType == ComparisonPredicate.COMP_EQ)
				if (comp == EQUAL)
					return true;
				else
					return false;

			if (comparatorType == ComparisonPredicate.COMP_NEQ)
				if (comp != EQUAL)
					return true;
				else
					return false;
			
			if (comparatorType == ComparisonPredicate.COMP_GT)
				if (comp > EQUAL)
					return true;
				else
					return false;
			
			if (comparatorType == ComparisonPredicate.COMP_GE)
				if (comp >= EQUAL)
					return true;
				else
					return false;
			
			if (comparatorType == ComparisonPredicate.COMP_LT)
				if (comp < EQUAL)
					return true;
				else
					return false;
			
			if (comparatorType == ComparisonPredicate.COMP_LE)
				if (comp <= EQUAL)
					return true;
				else
					return false;
		}
		else {			
			if ((comparatorType == ComparisonPredicate.COMP_EQ) && (v1.getType() == v2.getType())) 
				if (v1.getStringValue().equals(v2.getStringValue()))
					return true;
				else
					return false;

			if (comparatorType == ComparisonPredicate.COMP_NEQ) {
				if (v1.getType() != v2.getType()) 
					return true;
				else if (!v1.getStringValue().equals(v2.getStringValue()))
					return true;
				else
					return false;
			}
		}
		return false;
	}

}
