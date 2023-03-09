package jco.ql.engine.byZunEvaluator;

import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.fuzzy.FuzzySetDefinition;

public class CheckForFuzzySetEvaluator implements JCOConstants{

	// modified by Balicco
	public static DocumentDefinition evaluate(FuzzySetDefinition checkForFuzzySet, Pipeline checkForPipeline) {
		DocumentDefinition curDoc;
		
		if(checkForFuzzySet.type == null)
			curDoc = evaluateClassical(checkForFuzzySet, checkForPipeline); 	//old classical fuzzy set
		else
			curDoc = evaluateGeneric(checkForFuzzySet, checkForPipeline);		// generic fuzzy set - Balicco
		return curDoc;
	}

	// modified by Balicco
	public static DocumentDefinition evaluateClassical(FuzzySetDefinition checkForFuzzySet, Pipeline checkForPipeline) {
    	DocumentDefinition curDoc = checkForPipeline.getCurrentDoc();
        SimpleValue membership = ConditionEvaluator.fuzzyEvaluate(checkForFuzzySet.using, checkForPipeline);
        
        if (membership == null || membership.getType() == EValueType.NULL)
        	return curDoc;
        
        FieldDefinition fuzzySet = new FieldDefinition(checkForFuzzySet.fuzzySet, membership);
        if (curDoc.getValue(FUZZYSETS_FIELD_NAME) == null) {
        	DocumentValue dv = new DocumentValue();
        	FieldDefinition fd = new FieldDefinition(FUZZYSETS_FIELD_NAME, dv); 
        	curDoc.addField (fd);
        }
        FieldDefinition fd = curDoc.getField(FUZZYSETS_FIELD_NAME);
        DocumentValue dv = (DocumentValue)fd.getValue();        
        DocumentDefinition fuzzySets = dv.getDocument();
        fuzzySets.addField(fuzzySet);

        return curDoc;
	}

	
	// added by Balicco
	public static DocumentDefinition evaluateGeneric(FuzzySetDefinition checkForFuzzySet, Pipeline checkForPipeline) {
    	DocumentDefinition curDoc;
    	List<FieldDefinition> degrees;
    	curDoc = checkForPipeline.getCurrentDoc();
    	
    	if (!checkForPipeline.hasFuzzySetType(checkForFuzzySet.type)) {
    		JMH.addFuzzyMessage("Wrong fuzzy set type name: [" + checkForFuzzySet.type + "] is not found");
    		return curDoc;		
    	}
    	degrees = ConditionEvaluator.genericFuzzyEvaluate (checkForFuzzySet.using, checkForPipeline, checkForFuzzySet.type);
    	
        if (degrees == null )
        	return curDoc;
        
        if (curDoc.getValue(FUZZYSETS_FIELD_NAME) == null) {
        	DocumentValue dv = new DocumentValue();
        	FieldDefinition fd = new FieldDefinition(FUZZYSETS_FIELD_NAME, dv); 
        	curDoc.addField (fd);
        }
        degrees.add( new FieldDefinition("type", new SimpleValue(checkForFuzzySet.type)));
        FieldDefinition fd = curDoc.getField(FUZZYSETS_FIELD_NAME);
        DocumentValue dv = (DocumentValue)fd.getValue();
        DocumentDefinition fuzzySets = dv.getDocument();
        fuzzySets.addDocument(checkForFuzzySet.fuzzySet, degrees);

        return curDoc;
	}
	
}
