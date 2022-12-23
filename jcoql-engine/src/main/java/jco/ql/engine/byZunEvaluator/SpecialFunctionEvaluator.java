package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ArrayFunctionFactor;
import jco.ql.parser.model.predicate.IfErrorFunction;
import jco.ql.parser.model.predicate.MembershipOfFunction;
import jco.ql.parser.model.predicate.SpecialFunctionFactor;
import jco.ql.parser.model.predicate.TranslateFunction;

public class SpecialFunctionEvaluator implements JCOConstants {

	public static JCOValue evaluate(SpecialFunctionFactor function, Pipeline pipeline) {
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.MEMBERSHIP_OF_FUNCTION)
			return getMembershipOfValue ((MembershipOfFunction)function, pipeline);
				
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.TRANSLATE_FUNCTION)
			return getTranslationValue ((TranslateFunction)function, pipeline);

		if (function.getSpecialFuntionType() == SpecialFunctionFactor.IF_ERROR_FUNCTION)
			return getIfErrorValue ((IfErrorFunction)function, pipeline);

		if (function.getSpecialFuntionType() == SpecialFunctionFactor.ARRAY_FUNCTION)
			return ArrayFunctionEvaluator.evaluate ((ArrayFunctionFactor)function, pipeline);

		return new SimpleValue (); // null value
	}


// ***********************************************
	
	private static JCOValue getMembershipOfValue(MembershipOfFunction moFunction, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value
		
		return doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + moFunction.getMemebershipOfFuzzyset());
	}


	// TRANSLATE Strings values if it finds a match. 
	// otherwise assign the DefaultTranslation OR it leaves the value unchanged, 
	private static JCOValue getTranslationValue(TranslateFunction translateFunction, Pipeline pipeline) {
		JCOValue valueToTraslate, defaultValue;
		
		valueToTraslate = ExpressionPredicateEvaluator.calculate(translateFunction.expression2translate, pipeline);
		defaultValue = valueToTraslate;
		if (translateFunction.dictionaryTranslateDefault != null)
			defaultValue = new SimpleValue (translateFunction.dictionaryTranslateDefault);

		String s = pipeline.getDictionaryValue(translateFunction.dictionary, valueToTraslate, translateFunction.dictionaryCaseSensitive);
		if (s == null)
			return defaultValue;
		return new SimpleValue (s);
	}

	
	private static JCOValue getIfErrorValue(IfErrorFunction factor, Pipeline pipeline) {
		JCOValue defaultValue = ExpressionFactorEvaluator.getFactorValue(factor, pipeline);
		JCOValue outValue = null;
		try {
			outValue = ExpressionPredicateEvaluator.calculate (factor.expression2check, pipeline);
			if ((outValue != null) && (outValue.getType() != EValueType.NULL))
					defaultValue = outValue;
		} catch (RuntimeException re) {
			JMH.add("Expression evaluation failed:\t" + factor.expression2check);
		}
		return defaultValue;
	}




}
