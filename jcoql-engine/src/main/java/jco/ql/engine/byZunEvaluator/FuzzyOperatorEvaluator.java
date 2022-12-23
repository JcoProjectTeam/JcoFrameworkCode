package jco.ql.engine.byZunEvaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.UsingPredicate;
import jco.ql.parser.model.util.Parameter;

public class FuzzyOperatorEvaluator {

    public static SimpleValue evaluate (UsingPredicate usingPredicate, Pipeline pipeline) {
    	int ndx = alreadyExists(pipeline, usingPredicate.fuzzyOperator);
    	if (ndx == -1) {
    		JMH.addFuzzyMessage("Fuzzy Operator not found:\t" + usingPredicate.fuzzyOperator);
    		return new SimpleValue (); // null
    	}

    	FuzzyOperatorCommand fo = pipeline.getFuzzyOperators().get(ndx);
    	if (fo.getParameters().size() != usingPredicate.fuzzyOperatorParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Operator:\t" + fo.getFuzzyOperatorName());
    		return new SimpleValue (); // null
    	}
    	
    	List<JCOValue> actualParameters = getActualParameters (usingPredicate, pipeline);
    	if (!checkParameters (actualParameters, fo.getParameters())) {
    		JMH.addFuzzyMessage("Wrong type of parameters for Fuzzy Operator:\t" + fo.getFuzzyOperatorName());
    		return new SimpleValue (); // null    		
    	}
    	
    	DocumentDefinition fuzzyDoc = createFuzzyDoc (actualParameters, fo.getParameters());   	
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	if (fo.getPrecondition() != null)
    		if (!ConditionEvaluator.matchCondition(fo.getPrecondition(), fuzzyPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Operator:\t" + fo.getFuzzyOperatorName());
	    		return new SimpleValue (); // null    		
	    	}

    	JCOValue eval = ExpressionPredicateEvaluator.calculate(fo.getEvaluate(), fuzzyPipeline);
    	SimpleValue membership = getMembership (eval, fo);
    	
    	return membership;
    }

    /* ****************************************************************************************************** */

	// return the index of FO in the list. -1 if the FO is not (yet) existing
    private static int alreadyExists(Pipeline pipeline, String foName) {
        for(int i = 0; i < pipeline.getFuzzyOperators().size(); i++) {
            if(pipeline.getFuzzyOperators().get(i).getFuzzyOperatorName().equals(foName)) {
                return i;
            }
        }
        return -1;
    }
    

    private static List<JCOValue> getActualParameters(UsingPredicate usingPredicate, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    	for (Expression expr : usingPredicate.fuzzyOperatorParameters)
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
		if (eval.getType() != EValueType.INTEGER && eval.getType() != EValueType.DECIMAL) {
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


}
