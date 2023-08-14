package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ArrayFunctionFactor;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.Field;


public class ArrayFunctionEvaluator implements JCOConstants {
	
	public static JCOValue evaluate(ArrayFunctionFactor function, Pipeline pipeline) {
		ArrayValue sourceArray = getSourceArray (function, pipeline);		

		if (JCOValue.isNull(sourceArray))
			return new SimpleValue ();		// null value

		if (function.fieldType == ArrayFunctionFactor.NUMERIC_TYPE)
			return searchNumeric (sourceArray, function);
		else if (function.fieldType == ArrayFunctionFactor.STRING_TYPE)
			return searchString (sourceArray, function);
		else if (function.fieldType == ArrayFunctionFactor.BOOLEAN_TYPE)
			return searchBoolean (sourceArray, function);
		
		return new SimpleValue ();		// null value
	}
	
	// *******************************************************
		
	private static ArrayValue getSourceArray (ArrayFunctionFactor function, Pipeline pipeline) {
		ArrayValue sourceArray = null;
		if (function.getSourceType() == ArrayFunctionFactor.SOURCE_FIELD) {
			DocumentDefinition doc = pipeline.getCurrentDoc();
			if (doc == null)
				return null;
			JCOValue value = doc.getValue(function.getSourceField().toString());
			if (value instanceof ArrayValue)
				sourceArray = (ArrayValue)value;
		}
		else { //	ArrayFunctionFactor.SOURCE_ARRAY 
			sourceArray = new ArrayValue ();
			for (Expression expr : function.getSourceArray()) {
				JCOValue value = ExpressionPredicateEvaluator.calculate(expr, pipeline);
				sourceArray.add(value);
			}
		}
		
		return sourceArray;
	}
	
	// *******************************************************
		
	private static SimpleValue searchString(ArrayValue sourceArray, ArrayFunctionFactor function) {
		JCOValue compareValue;
		SimpleValue returnValue = new SimpleValue (); // null value;

		for (JCOValue currentValue : sourceArray.getValues()) {
			compareValue = null;
			if (function.searchAll()) 
				if (JCOValue.isStringValue(currentValue))
					compareValue = currentValue;
			if ((compareValue == null) && (currentValue instanceof DocumentValue)) {
				DocumentDefinition tempDoc = ((DocumentValue)currentValue).getDocument();
				for (Field f : function.getSearchingArray()) {
					JCOValue v = tempDoc.getValue(f.toString());
					if (JCOValue.isStringValue(v)) {
						compareValue = v;
						break;
					}
				}
			}
			
			if (JCOValue.isStringValue(compareValue)) {
				SimpleValue cv = (SimpleValue)compareValue;
				if (JCOValue.isNull(returnValue))
					returnValue = cv;
				else if (function.getFunctionType() == ArrayFunctionFactor.MAX_FUNCTION) {
					if (cv.compareTo(returnValue) == GREATER_THAN)
						returnValue = cv;					
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.MIN_FUNCTION) {
					if (cv.compareTo(returnValue) == LESS_THAN)
						returnValue = cv;					
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.SUM_FUNCTION) 
					returnValue = (SimpleValue)ExpressionPredicateEvaluator.addTerms(returnValue, cv, Expression.ADD);				
			}
		}
	
		return returnValue;
	}
	
	
	private static SimpleValue searchNumeric(ArrayValue sourceArray, ArrayFunctionFactor function) {
		JCOValue compareValue;
		SimpleValue returnValue = new SimpleValue (); // null value;
		int nValueableItems = 0;
		double sumValueableItems = 0;

		for (JCOValue currentValue : sourceArray.getValues()) {
			compareValue = null;
			if (function.searchAll()) 
				if (JCOValue.isNumericValue(currentValue))
					compareValue = currentValue;
			if ((compareValue == null) && (currentValue instanceof DocumentValue)) {
				DocumentDefinition tempDoc = ((DocumentValue)currentValue).getDocument();
				for (Field f : function.getSearchingArray()) {
					JCOValue v = tempDoc.getValue(f.toString());
					if (JCOValue.isNumericValue(v)) {
						compareValue = v;
						break;
					}
				}
			}				
			
			if (JCOValue.isNumericValue(compareValue)) {
				SimpleValue cv = (SimpleValue)compareValue;
				if (JCOValue.isNull(returnValue)) {
					returnValue = cv;
					nValueableItems++;
					sumValueableItems = cv.getNumericValue().doubleValue();
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.MAX_FUNCTION) {
					if (cv.compareTo(returnValue) == GREATER_THAN)
						returnValue = cv;					
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.MIN_FUNCTION) {
					if (cv.compareTo(returnValue) == LESS_THAN)
						returnValue = cv;					
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.SUM_FUNCTION) {
					sumValueableItems += cv.getNumericValue().doubleValue();
					returnValue = new SimpleValue (sumValueableItems);										
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.AVG_FUNCTION) {
					nValueableItems++;
					sumValueableItems += cv.getNumericValue().doubleValue();
					returnValue = new SimpleValue (sumValueableItems / nValueableItems);					
				}
			}
		}
	
		return returnValue;
	}
	

	private static SimpleValue searchBoolean(ArrayValue sourceArray, ArrayFunctionFactor function) {
		JCOValue compareValue;
		SimpleValue returnValue = new SimpleValue (); // null value;

		for (JCOValue currentValue : sourceArray.getValues()) {
			compareValue = null;
			if (function.searchAll()) 
				if (JCOValue.isBooleanValue(currentValue))
					compareValue = currentValue;
			if ((compareValue == null) && (currentValue instanceof DocumentValue)) {
				DocumentDefinition tempDoc = ((DocumentValue)currentValue).getDocument();
				for (Field f : function.getSearchingArray()) {
					JCOValue v = tempDoc.getValue(f.toString());
					if (JCOValue.isBooleanValue(v)) {
						compareValue = v;
						break;
					}
				}
			}				
			
			// evaluation in short-cut
			if (JCOValue.isBooleanValue(compareValue)) {
				SimpleValue cv = (SimpleValue)compareValue;
				if (JCOValue.isNull(returnValue))
					returnValue = cv;

				if (function.getFunctionType() == ArrayFunctionFactor.MAX_FUNCTION) {
					if (JCOValue.getBooleanValue(cv))
						return cv;					
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.MIN_FUNCTION) {
					if (!JCOValue.getBooleanValue(cv))
						return cv;										
				}
				else if (function.getFunctionType() == ArrayFunctionFactor.SUM_FUNCTION) {
					if (JCOValue.getBooleanValue(cv))
						return cv;										
				}
			}
		}
	
		return returnValue;
	}

}
