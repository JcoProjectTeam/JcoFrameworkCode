package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.fuzzy.FuzzySetDefinition;

public class CheckForFuzzySetEvaluator implements JCOConstants{

	public static DocumentDefinition evaluate(FuzzySetDefinition checkForFuzzySet, Pipeline checkForPipeline) {
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

}
