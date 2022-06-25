package jco.ql.engine.evaluator;

import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.engine.byZunEvaluator.ConditionEvaluator;
import jco.ql.engine.byZunEvaluator.GenerateEvaluator;
import jco.ql.model.Case;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.parser.model.util.GenerateSection;
import jco.ql.parser.model.util.WhereCase;


public class CaseEvaluator implements JCOConstants {

	public static DocumentDefinition evaluate(Pipeline pipeline, Case caseFilter) throws ScriptException {
		DocumentDefinition outDoc = null;

		for(WhereCase wc : caseFilter.getWhereConditions()) {
			if (ConditionEvaluator.matchCondition(wc.getCondition(), pipeline)) {
				outDoc = pipeline.getCurrentDoc();
				if (wc.hasGenerateSection()) {
					GenerateSection gs = wc.generateSection;					
					outDoc = GenerateEvaluator.evaluate(pipeline, gs);
				}	
				// PF. Once the 1st where condition has met with the current document there's no need to go on.
				break;	// exit for cycle
			} 
			else if(caseFilter.isKeepOthers()) 
				outDoc = pipeline.getCurrentDoc();

		} 
		return outDoc;
	}

	

}
