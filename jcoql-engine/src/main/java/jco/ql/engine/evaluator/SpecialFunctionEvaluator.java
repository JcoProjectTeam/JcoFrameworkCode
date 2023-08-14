package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ArrayFunctionFactor;
import jco.ql.parser.model.predicate.ExtentFunction;
import jco.ql.parser.model.predicate.IfErrorFunction;
import jco.ql.parser.model.predicate.MembershipArray;
import jco.ql.parser.model.predicate.SpecialFunctionFactor;
import jco.ql.parser.model.predicate.TranslateFunction;

public class SpecialFunctionEvaluator implements JCOConstants {

	public static JCOValue evaluate(SpecialFunctionFactor function, Pipeline pipeline) {
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.MEMBERSHIP_TO_FUNCTION)
			return getMembershipToValue ((ExtentFunction)function, pipeline);

		// added 21.06.2023
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.EXTENT_FUNCTION)
			return getExtentValue ((ExtentFunction)function, pipeline);
		
		// added 10.08.2023
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.MEMBERSHIP_ARRAY)
			return MembershipArrayEvaluator.evaluate ((MembershipArray)function, pipeline);

		// added by Balicco
		if (function.getSpecialFuntionType() == SpecialFunctionFactor.DEGREE_FUNCTION)
			return getDegreeValue ((ExtentFunction)function, pipeline);

		if (function.getSpecialFuntionType() == SpecialFunctionFactor.TRANSLATE_FUNCTION)
			return getTranslationValue ((TranslateFunction)function, pipeline);

		if (function.getSpecialFuntionType() == SpecialFunctionFactor.IF_ERROR_FUNCTION)
			return getIfErrorValue ((IfErrorFunction)function, pipeline);

		if (function.getSpecialFuntionType() == SpecialFunctionFactor.ARRAY_FUNCTION)
			return ArrayFunctionEvaluator.evaluate ((ArrayFunctionFactor)function, pipeline);

		return new SimpleValue (); // null value
	}


// ***********************************************
	
	private static JCOValue getMembershipToValue(ExtentFunction function, Pipeline pipeline) {
		JCOValue fv = new SimpleValue ();		// null value
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return fv;							// null value
		
		String str = FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + function.getFuzzysetName();
		fv = doc.getValue(str);
		if (JCOValue.isFuzzyValue(fv))
			return fv;

		JMH.addFuzzyMessage("MEMBERSHIP_TO Special Function failed:\t" + str + " is not a fuzzyset membership");
		return new SimpleValue ();				// null value
	}


	private static JCOValue getExtentValue(ExtentFunction function, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value
		
		return doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + function.getFuzzysetName());
	}


	// added by Balicco
	private static JCOValue getDegreeValue(ExtentFunction function, Pipeline pipeline) {
		JCOValue fv = new SimpleValue ();		// null value
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return fv;							// null value

		String str = FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + function.getFuzzysetName();
		if (function.getDegreeName() != null)
			str += function.getDegreeName();
		fv = doc.getValue(str);
		if (JCOValue.isFuzzyValue(fv))
			return fv;

		JMH.addFuzzyMessage("DEGREE Special Function failed:\t" + str + " is not a fuzzyset degree");
		return new SimpleValue ();				// null value
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
