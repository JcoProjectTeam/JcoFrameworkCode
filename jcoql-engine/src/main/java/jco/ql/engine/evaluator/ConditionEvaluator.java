package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.condition.ConditionAnd;
import jco.ql.parser.model.condition.ConditionNot;
import jco.ql.parser.model.condition.ConditionOr;
import jco.ql.parser.model.fuzzy.FuzzyOperatorDefinition;

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


	// added by Balicco
	// ***************** Generic fuzzy set***************************
	public static List<FieldDefinition> genericFuzzyEvaluate (Condition condition, Pipeline pipeline, String fuzzysetModel) {
		List <FieldDefinition> value = new ArrayList<FieldDefinition>();
		
		if (condition == null)
			return value;
		
		if (condition.getType() == Condition.OR_CONDITION)
			value = GenericFuzzyEvaluateConditionOr ((ConditionOr)condition, pipeline, fuzzysetModel);
		
		else if (condition.getType() == Condition.AND_CONDITION)
			value = GenericFuzzyEvaluateConditionAnd ((ConditionAnd)condition, pipeline, fuzzysetModel);
		
		else if (condition.getType() == Condition.NOT_CONDITION)
			value = genericFuzzyEvaluateConditionNot ((ConditionNot)condition, pipeline, fuzzysetModel);
			
		else if (condition.getType() == Condition.PREDICATE_CONDITION)
			value = PredicateEvaluator.fuzzyGenericEvaluate (condition.getPredicate(), pipeline, fuzzysetModel);
		
		return value;
	}

	/* ************ Classical Evaluation *********************************************** */
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


	/* ************ Generic Fuzzy Evaluation *********************************************** */
	// added by Balicco
	private static List<FieldDefinition> GenericFuzzyEvaluateConditionOr(ConditionOr condition, Pipeline pipeline, String fuzzysetModel) {
		List<FieldDefinition> value;
		List<FieldDefinition> value2;
		FuzzyOperatorDefinition  operatorDefinition;

		if (pipeline.getFuzzySetModel(fuzzysetModel).getDefOr() != null) {
			operatorDefinition = pipeline.getFuzzySetModel(fuzzysetModel).getDefOr();
			value = genericFuzzyEvaluate(condition.getSubConditions().get(0), pipeline, fuzzysetModel);
			
			for (int i = 1; i < condition.getSubConditions().size(); i++) {
				value2 = genericFuzzyEvaluate(condition.getSubConditions().get(i), pipeline, fuzzysetModel);
				value = genericEvaluateOrAnd(value, value2, pipeline, operatorDefinition, fuzzysetModel);
			}
			return value;
		}
		JMH.addFuzzyMessage("Operator OR not defined for [" + fuzzysetModel + "] fuzzy set model" );
		return null;
	}
	
	// added by Balicco
	private static List<FieldDefinition> GenericFuzzyEvaluateConditionAnd(ConditionAnd condition, Pipeline pipeline, String type) {
		List<FieldDefinition> value;
		List<FieldDefinition> value2 = null;
		FuzzyOperatorDefinition  operatorDefinition;
		
		if (pipeline.getFuzzySetModel(type).getDefAnd() != null) {
			operatorDefinition = pipeline.getFuzzySetModel(type).getDefAnd();
			value = genericFuzzyEvaluate(condition.getSubConditions().get(0), pipeline, type);
			
			for (int i = 1; i < condition.getSubConditions().size(); i++) 
				value2 = genericFuzzyEvaluate(condition.getSubConditions().get(i), pipeline, type);
				value = genericEvaluateOrAnd(value, value2, pipeline, operatorDefinition, type);
			return value;
		}
		JMH.addFuzzyMessage("Operator AND not definid for [" + pipeline.getFuzzySetModel(type).getFuzzySetModelName()+"] fuzzy set type" );
		return null;
	}

	// added by Balicco
	private static List<FieldDefinition> genericFuzzyEvaluateConditionNot (ConditionNot condition, Pipeline pipeline, String type) {
		if (pipeline.getFuzzySetModel(type).getDefNot() != null) {
			List<FieldDefinition> value = genericFuzzyEvaluate(condition.getSubCondition(), pipeline, type);
			FuzzyOperatorDefinition operatorDefinition = pipeline.getFuzzySetModel(type).getDefNot();
			return genericEvaluateNot(value, pipeline, operatorDefinition, type);
		}
		JMH.addFuzzyMessage( "Operator NOT not definite for [" + pipeline.getFuzzySetModel(type).getFuzzySetModelName()+"] fuzzy set type" );
		return null;
	}
	
	
	// added by Balicco	
	public static List<FieldDefinition> genericEvaluateOrAnd(List<FieldDefinition> degree1, List<FieldDefinition> degree2,
															Pipeline pipeline, FuzzyOperatorDefinition def, String type ) {
		List<FieldDefinition> value = new ArrayList<>();
		SimpleValue val;
		FuzzySetModelCommand ft = pipeline.getFuzzySetModel(type);
		List<FieldDefinition> derivedDegrees;
		
		if (degree1 == null || degree2 == null) 
			return null;
		
		DocumentDefinition doc = new DocumentDefinition();
		doc.addDocument("x", degree1);
		doc.addDocument("y", degree2);
    	Pipeline p = new Pipeline(pipeline);
    	p.setCurrentDoc(doc);
    	
    	for (int i = 0; i < def.degrees.size(); i++) {
    		val = (SimpleValue) ExpressionPredicateEvaluator.calculate(def.expr.get(i), p);
    		value.add( new FieldDefinition( def.degrees.get(i), val ) );
    	}
    	derivedDegrees = FuzzyOperatorEvaluator.derivedDegreesEvaluate(value, ft.getDerivedDegrees(), ft.getDerivedExpr(), pipeline);
    	if (!FuzzyOperatorEvaluator.checkConstraint(value, derivedDegrees, pipeline, ft.getConstraint())) {
			JMH.add("Constraint is not respected for fuzzy set resulting from:" + def.type);
			return null;
		}
    	value.addAll(derivedDegrees);
		return value;
	}
	
	// added by Balicco
	public static List<FieldDefinition> genericEvaluateNot(List<FieldDefinition> degrees,Pipeline pipeline, FuzzyOperatorDefinition def, String type ) {
		List<FieldDefinition> value = new ArrayList<>();
		SimpleValue val;
		FuzzySetModelCommand ft = pipeline.getFuzzySetModel(type);
		List<FieldDefinition> derivedDegrees;
		
		if (degrees == null) 
			return null;
		
		DocumentDefinition doc = new DocumentDefinition();
		doc.addDocument("x", degrees);
    	Pipeline p = new Pipeline(pipeline);
    	p.setCurrentDoc(doc);
    	
    	for (int i = 0; i < def.degrees.size(); i++) {
    		val = (SimpleValue) ExpressionPredicateEvaluator.calculate(def.expr.get(i), p);
    		value.add( new FieldDefinition( def.degrees.get(i), val ) );
    	}
    	
    	derivedDegrees = FuzzyOperatorEvaluator.derivedDegreesEvaluate(value, ft.getDerivedDegrees(), ft.getDerivedExpr(), pipeline);
    	if (!FuzzyOperatorEvaluator.checkConstraint(value, derivedDegrees, pipeline, ft.getConstraint())) {
			JMH.add("Constraint is not respected for fuzzy set resulting from: " + def.type);
			return null;
		}
    	value.addAll(derivedDegrees);
		return value;
	}
	

}
