package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.command.FuzzyFunctionCommand;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.command.GenericFuzzyOperatorCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.UsingPredicate;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction 
 * - FI modified on 30/10/2022
 * */
public class UsingPredicateEvaluator implements JCOConstants {

	public static SimpleValue fuzzyEvaluate(UsingPredicate usingPredicate, Pipeline pipeline) {
		if (pipeline.getCurrentDoc() == null)
			return new SimpleValue ();

		SimpleValue value = new SimpleValue ();	// null type
		if (usingPredicate.usingType == UsingPredicate.USING_FUZZY_SET)
			value = evaluateFuzzySet(usingPredicate, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_FUNCTION)
			value = evaluateFuzzyFunction (usingPredicate, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_SUB_CONDITION)
			value = ConditionEvaluator.fuzzyEvaluate(usingPredicate.subUsingCondition, pipeline);
		else if (usingPredicate.usingType == UsingPredicate.USING_IF_FAILS)
			value = evaluateIfFails (usingPredicate, pipeline);
		
		return value;
	}

	
	private static SimpleValue evaluateFuzzySet(UsingPredicate usingPredicate, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		
		// modified by Balicco
		JCOValue checkDoc = doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + usingPredicate.fuzzySet);
		if (JCOValue.isDocumentValue(checkDoc)) {
			JMH.addFuzzyMessage("Fuzzy set [" + usingPredicate.fuzzySet + "] is a generic fuzzy set, expected a classical fuzzy set");
			return new SimpleValue ();
		}
		SimpleValue outValue = (SimpleValue)checkDoc;
		if (outValue == null)
			return new SimpleValue ();		// null value			
		return outValue;
	}

	private static SimpleValue evaluateFuzzyFunction(UsingPredicate usingPredicate, Pipeline pipeline) {
		SimpleValue outValue = new SimpleValue();
		String operator = usingPredicate.fuzzyFunction;
		FuzzyFunctionCommand ffc = pipeline.getFuzzyFunctions().get(operator);

		if (ffc == null)
			JMH.add("Generic Fuzzy Operator " + operator + " not defined");
		else if (ffc.getType() == FuzzyFunctionCommand.GENERIC_OPERATOR) 
			JMH.add("Fuzzy Operator " + operator + " it is not supported for classical fuzzy set ");
		else if (ffc.getType() == FuzzyFunctionCommand.GENERIC_EVALUATOR) 								// GB
			JMH.add("Fuzzy Evaluator" + operator + " it is not supported for classical fuzzy set");
		else if (ffc.getType() == FuzzyFunctionCommand.OPERATOR) {
			FuzzyOperatorCommand foc = (FuzzyOperatorCommand) ffc;
			outValue = FuzzyOperatorEvaluator.evaluate(foc, usingPredicate, pipeline);			
		}
		else if (ffc.getType() == FuzzyFunctionCommand.EVALUATOR || ffc.getType() == FuzzyFunctionCommand.AGGREGATOR) {
			FuzzyEvaluatorCommand fec = (FuzzyEvaluatorCommand) ffc;
			outValue = FuzzyEvaluatorEvaluator.evaluate(fec, usingPredicate, pipeline);			
		}
		
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

	/* ********************* Generic Fuzzy Set ***********************************************************/
	// added by Balicco
	// GB per i valutatori generici ok, non modificato
	public static List<FieldDefinition> genericFuzzyEvaluate(UsingPredicate usingPredicate, Pipeline pipeline, String fuzzysetModel) {
		List <FieldDefinition> value = new ArrayList<>();	// null type

		if (usingPredicate.usingType == UsingPredicate.USING_FUZZY_SET)
			value = evaluateGenericFuzzySet(usingPredicate, pipeline, fuzzysetModel);
		else if (usingPredicate.usingType == UsingPredicate.USING_FUNCTION) 
			value = evaluateGenericFunction (usingPredicate, pipeline, fuzzysetModel);
		else if (usingPredicate.usingType == UsingPredicate.USING_SUB_CONDITION)
			value = ConditionEvaluator.genericFuzzyEvaluate(usingPredicate.subUsingCondition, pipeline, fuzzysetModel);
		else if (usingPredicate.usingType == UsingPredicate.USING_IF_FAILS)
			JMH.add("Predicate " + UsingPredicate.USING_IF_FAILS + " it is not supported for generic fuzzy set");
		
		return value;
	}
	// PF
	// GB aggiunta riga oer valutatori generici
	private static List<FieldDefinition> evaluateGenericFunction (UsingPredicate usingPredicate, Pipeline pipeline, String fuzzysetModel) {
		List <FieldDefinition> value = new ArrayList<>();	// null type
		String operator = usingPredicate.fuzzyFunction;
		FuzzyFunctionCommand ffc = pipeline.getFuzzyFunctions().get(operator);

		if (ffc == null)
			JMH.add("Generic Fuzzy Operator OR Evaluator " + operator + " not defined");
		else if (ffc.getType() == FuzzyFunctionCommand.GENERIC_OPERATOR) {
			GenericFuzzyOperatorCommand gfoc = (GenericFuzzyOperatorCommand)ffc;
			value = FuzzyOperatorEvaluator.evaluateGeneric(gfoc, usingPredicate, pipeline, fuzzysetModel);			
		}
		else if (ffc.getType() == FuzzyFunctionCommand.GENERIC_EVALUATOR) { 			// GB
			FuzzyEvaluatorCommand gfe = (FuzzyEvaluatorCommand)ffc;
			value = FuzzyEvaluatorEvaluator.evaluateGeneric(gfe, usingPredicate, pipeline, fuzzysetModel);
		}
		else if (ffc.getType() == FuzzyFunctionCommand.OPERATOR)
			JMH.add("Fuzzy Operator " + operator + " it is not supported for generic fuzzy set " + fuzzysetModel);
		else if (ffc.getType() == FuzzyFunctionCommand.AGGREGATOR)
			JMH.add("Fuzzy Aggregator " + operator + " it is not supported for generic fuzzy set " + fuzzysetModel);
		else if (ffc.getType() == FuzzyFunctionCommand.EVALUATOR)
			JMH.add("Fuzzy Evaluator " + operator + " it is not supported for generic fuzzy set " + fuzzysetModel);

		return value;		
	}
	
	// added by Balicco
	private static List<FieldDefinition> evaluateGenericFuzzySet(UsingPredicate usingPredicate, Pipeline pipeline, String type) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		List<FieldDefinition> outValue = new ArrayList<FieldDefinition>();
		SimpleValue Value;
		FuzzySetModelCommand ft = pipeline.getFuzzySetModel(type); 
		
		
		//check fuzzy set 
		JCOValue checkDoc = doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + usingPredicate.fuzzySet);
		if (checkDoc == null ) {
			JMH.addFuzzyMessage("Fuzzy set [" + usingPredicate.fuzzySet + "] not found ");
			return null;
		}
		if (!JCOValue.isDocumentValue(checkDoc)) {
			JMH.addFuzzyMessage("Fuzzy set [" + usingPredicate.fuzzySet + "] is not generic fuzzy set ");
			return null;
		}
		
		//Type 
		Value = (SimpleValue)doc.getValue(FUZZYSETS_FIELD_NAME_DOT+FIELD_SEPARATOR+usingPredicate.fuzzySet+FIELD_SEPARATOR+"type");
		if (Value == null) {
			JMH.addFuzzyMessage("Type [" + ft.getFuzzySetModelName() + "] for fuzzy set [" + usingPredicate.fuzzySet + "] not found");
			return null;
		}
		if (!Value.getStringValue().equals(type)  ) {
			JMH.addFuzzyMessage("Wrong type [" + ft.getFuzzySetModelName() + "] for fuzzy set [" + usingPredicate.fuzzySet + "] ");
			return null;
		}
		
		//Degree
		for (int i = 0; i < ft.getDegrees().size(); i++) {
			Value = (SimpleValue)doc.getValue(FUZZYSETS_FIELD_NAME_DOT + FIELD_SEPARATOR + usingPredicate.fuzzySet + FIELD_SEPARATOR 
																										+ ft.getDegrees().get(i).name);
			if (Value == null) {
				JMH.addFuzzyMessage("Degree: [" + ft.getDegrees().get(i).name + "] of fuzzy set: [" + 
																								usingPredicate.fuzzySet + "] not found");
				return null;		// null value	
			}
			outValue.add( new FieldDefinition(ft.getDegrees().get(i).name, Value)  );
		}
		
		//Derived degree
		// GB posso non differenziare con l'utilizzo dei valutatori perché semplicemente usa l'operatore per il controllo
		// dei gradi derivati, il procedimento è lo stesso sia per gli operatori sia per i valutatori
		List<FieldDefinition> derivedDegrees = FuzzyOperatorEvaluator.derivedDegreesEvaluate(outValue, ft.getDerivedDegrees(), ft.getDerivedExpr(), pipeline);
    	if (!FuzzyOperatorEvaluator.checkConstraint(outValue, derivedDegrees, pipeline, ft.getConstraint())) {
			JMH.add("Constraint is not respected for fuzzy set: [" + usingPredicate.fuzzySet + "]");
			return null;
		}
    	outValue.addAll(derivedDegrees);

		return outValue;	
	}

}
