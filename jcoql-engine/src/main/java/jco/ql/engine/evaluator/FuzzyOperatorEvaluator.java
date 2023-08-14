package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.command.GenericFuzzyOperatorCommand;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.UsingPredicate;
import jco.ql.parser.model.util.Parameter;

public class FuzzyOperatorEvaluator {

    public static SimpleValue evaluate (FuzzyOperatorCommand foc, UsingPredicate usingPredicate, Pipeline pipeline) {
    	if (foc.getParameters().size() != usingPredicate.fuzzyFunctionParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Operator:\t" + foc.getFuzzyOperatorName());
    		return new SimpleValue (); // null
    	}
    	
    	List<JCOValue> actualParameters = getActualParameters (usingPredicate, pipeline);
    	if (!checkParameters (actualParameters, foc.getParameters())) {
    		JMH.addFuzzyMessage("Wrong type of parameters for Fuzzy Operator:\t" + foc.getFuzzyOperatorName());
    		return new SimpleValue (); // null    		
    	}
    	
    	DocumentDefinition fuzzyDoc = createFuzzyDoc (actualParameters, foc.getParameters());   	
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	if (foc.getPrecondition() != null)
    		if (!ConditionEvaluator.matchCondition(foc.getPrecondition(), fuzzyPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Operator:\t" + foc.getFuzzyOperatorName());
	    		return new SimpleValue (); // null    		
	    	}

    	JCOValue eval = ExpressionPredicateEvaluator.calculate(foc.getEvaluate(), fuzzyPipeline);
    	SimpleValue membership = getMembership (eval, foc);
    	
    	return membership;
    }

    /* ****************************************************************************************************** */

    private static List<JCOValue> getActualParameters(UsingPredicate usingPredicate, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    	for (Expression expr : usingPredicate.fuzzyFunctionParameters)
    		actualParameters.add(ExpressionPredicateEvaluator.calculate(expr, pipeline));
		return actualParameters;
	}


	// by contruction actualParameters and parameters have the same number of element
	private static boolean checkParameters(List<JCOValue> actualParameters, List<Parameter> parameters) {
		// TODO - non appena definiti se ci sono tipi standard
		for (int i=0; i<actualParameters.size(); i++) {
			;
		}
		return true;
	}

	
	// by contruction actualParameters and parameters have the same number of element
	private static DocumentDefinition createFuzzyDoc(List<JCOValue> actualParameters, List<Parameter> parameters) {
		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
		for (int i=0; i<actualParameters.size(); i++) {
			FieldDefinition fd = new FieldDefinition(parameters.get(i).name, actualParameters.get(i));
			fields.add(fd);
		}
		DocumentDefinition doc = new DocumentDefinition(fields);
		return doc;
	}


    
	private static SimpleValue getMembership(JCOValue eval, FuzzyOperatorCommand fo) {
		double x0, x1, y0, y1, membership;
		if (!JCOValue.isNumericValue(eval)) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for Fuzzy Operator:\t" + fo.getFuzzyOperatorName());
    		return new SimpleValue (); // null    					
		}
		double ev = 0;
		if (eval.getType() == EValueType.INTEGER) 
			ev = (Integer) eval.getValue();
		else 
			ev = (Double) eval.getValue();
		
		if (ev < fo.getPolyline().get(0).getX())
			return new SimpleValue (fo.getPolyline().get(0).getY().doubleValue());
		for (int i=1; i<fo.getPolyline().size(); i++ )
			if (ev < fo.getPolyline().get(i).getX()) {
				x0 = fo.getPolyline().get(i-1).getX();
				y0 = fo.getPolyline().get(i-1).getY();
				x1 = fo.getPolyline().get(i).getX();
				y1 = fo.getPolyline().get(i).getY();
				membership = ((y1-y0) / (x1-x0)) * (ev-x0) + y0; 
				return new SimpleValue (membership);			
			}
		
		return new SimpleValue (fo.getPolyline().get(fo.getPolyline().size()-1).getY().doubleValue());
	}

	
	/* ******************************Generic fuzzy operator*********************************************** */
	// added by Balicco
	public static List<FieldDefinition> evaluateGeneric (GenericFuzzyOperatorCommand gfoc, UsingPredicate usingPredicate, Pipeline pipeline, String fuzzysetModel) {
		List<FieldDefinition> degrees = new ArrayList<>();
    	List<JCOValue> evalList = new ArrayList<>();
		
    	if (!gfoc.getFuzzysetModelName().equals(fuzzysetModel)) {
    		JMH.addFuzzyMessage("Wrong fuzzyset model: [" + fuzzysetModel + "] for fuzzy operator: [" + usingPredicate.fuzzyFunction + "]");
    		return null;		
    	}

    	FuzzySetModelCommand fsmc = pipeline.getFuzzySetModel(fuzzysetModel);		
    	//fuzzy operator check
    	if (gfoc.getParameters().size() != usingPredicate.fuzzyFunctionParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Operator:\t" + gfoc.getGenericFuzzyOperatorName());
    		return null; // null
    	}
    	
    	//paramiter check
    	List<JCOValue> actualParameters = getActualParameters (usingPredicate, pipeline);
    	if (!checkParameters (actualParameters, gfoc.getParameters())) {
    		JMH.addFuzzyMessage("Wrong type of parameters for Fuzzy Operator:\t" + gfoc.getGenericFuzzyOperatorName());
    		return null; // null    		
    	}
    	
    	//precondition check
    	DocumentDefinition fuzzyDoc = createFuzzyDoc (actualParameters, gfoc.getParameters());
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	if (gfoc.getPrecondition() != null)
    		if (!ConditionEvaluator.matchCondition(gfoc.getPrecondition(), fuzzyPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Operator:\t" + gfoc.getGenericFuzzyOperatorName());
	    		return null; // null    		
    		}

    	//eval
    	for (int i = 0; i < gfoc.getEvaluate().size(); i++) 
			evalList.add(ExpressionPredicateEvaluator.calculate(gfoc.getEvaluate().get(i), fuzzyPipeline));
    	
    	for (int i = 0; i < fsmc.getDegrees().size(); i++) 
    		degrees.add( new FieldDefinition(fsmc.getDegrees().get(i).name, getDegree(evalList.get(i) , gfoc, i) ) );
    	
    	//derived degree
		List<FieldDefinition> derivedDegrees = derivedDegreesEvaluate(degrees, fsmc.getDerivedDegrees(), fsmc.getDerivedExpr(), pipeline);
    	
    	//constraint
		if (!checkConstraint(degrees, derivedDegrees, pipeline, fsmc.getConstraint())) {
			JMH.add("Constraint is not respected for " + gfoc.getFuzzysetModelName());
			return null;
		}
    	
    	degrees.addAll( derivedDegrees );
		
    	return degrees;
    }
	


    
	// added by Balicco
    private static SimpleValue getDegree(JCOValue eval, GenericFuzzyOperatorCommand gfo, int pos) {
		double x0, x1, y0, y1, membership;
		if (eval.getType() != EValueType.INTEGER && eval.getType() != EValueType.DECIMAL) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for Fuzzy Operator:\t" + gfo.getGenericFuzzyOperatorName());
    		return new SimpleValue (); // null    					
		}
		double ev = 0;
		if (JCOValue.isNumericValue(eval)) {
			String s = eval.getStringValue();
			ev = Double.parseDouble(s);
		}
		
		List<FuzzyPoint> p = gfo.getPolylines().get(pos).polyline;
		if (ev < p.get(0).getXvalue())
			return new SimpleValue (p.get(0).getYvalue());
		for (int i=1; i<p.size(); i++ )
			if (ev < p.get(i).getXvalue()) {
				x0 = p.get(i-1).getXvalue();
				y0 = p.get(i-1).getYvalue();
				x1 = p.get(i).getXvalue();
				y1 = p.get(i).getYvalue();	
				membership = ((y1-y0) / (x1-x0)) * (ev-x0) + y0; 
				return new SimpleValue (membership);			
			}
		
		return new SimpleValue (p.get(p.size()-1).getYvalue());
	}
    
	// added by Balicco
    public static List<FieldDefinition> derivedDegreesEvaluate(List<FieldDefinition> degrees,
    																List<String>derivedName, List<Expression>derivedExpr, Pipeline pipeline) {
    	List<FieldDefinition> value = new ArrayList<>();
    	JCOValue j;
    	double ev=0.0;
    	
    	DocumentDefinition fuzzyDoc = new DocumentDefinition (degrees);
    	//JMH.add("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+ fuzzyDoc );
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	for (int i = 0; i < derivedExpr.size(); i++) {
    		j = ExpressionPredicateEvaluator.calculate(derivedExpr.get(i), fuzzyPipeline);
    		if (JCOValue.isNumericValue(j)) {
    			String s = j.getStringValue();
    			ev = Double.parseDouble(s);
    			if (ev<0) 
    				ev=0;
    			else if (ev>1) 
    				ev=1;
			}
    		value.add( new FieldDefinition(derivedName.get(i), new SimpleValue(ev) ));
    	}
    	return value;
	}
        
	// added by Balicco
    public static boolean checkConstraint(List<FieldDefinition>degree, List<FieldDefinition> derivedDegree, 
    											Pipeline pipeline, jco.ql.parser.model.condition.Condition condition) {
    	List<FieldDefinition> d = new ArrayList<>();
    	d.addAll(degree);
    	d.addAll(derivedDegree);
    	DocumentDefinition fuzzyDoc = new DocumentDefinition(d);
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	if (condition != null)
    		if (!ConditionEvaluator.matchCondition((jco.ql.parser.model.condition.Condition) condition, fuzzyPipeline)) 
	    		//JMH.addFuzzyMessage("Constraints not respected for Fuzzy Operator:\t" + gfo.getGenericFuzzyOperatorName());
	    		return false; 		
    	return true;
	}


}
