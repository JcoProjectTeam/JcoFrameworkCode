package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.WUKPredicate;

/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class WUKPredicateEvaluator implements JCOConstants {

	public static SimpleValue evaluate(WUKPredicate predicate, Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		DocumentValue fuzzysetsField = (DocumentValue) doc.getValue(FUZZYSETS_FIELD_NAME);

		// evaluation in short-circuit
		if (fuzzysetsField == null) {
			 if (predicate.getWUKType() == WUKPredicate.UNKNOWN_PREDICATE)
				 return new SimpleValue(true);

			 return new SimpleValue(false);
		}
		else {
			DocumentDefinition fuzzyDoc = new DocumentDefinition (fuzzysetsField.getFields());
			if (predicate.getWUKType() == WUKPredicate.WITHIN_PREDICATE) {
				for (String fs : predicate.getFuzzySetsList()) 
					if (fuzzyDoc.getValue(fs) == null || Double.parseDouble(fuzzyDoc.getValue(fs).getStringValue()) == 0)
						 return new SimpleValue(false);
				 return new SimpleValue(true);
			}

			else if (predicate.getWUKType() == WUKPredicate.UNKNOWN_PREDICATE) {
				for (String fs : predicate.getFuzzySetsList()) 
					if (fuzzyDoc.getValue(fs) != null)
						 return new SimpleValue(false);				
				 return new SimpleValue(true);
			}

			else if (predicate.getWUKType() == WUKPredicate.KNOWN_PREDICATE) {
				for (String fs : predicate.getFuzzySetsList()) 
					if (fuzzyDoc.getValue(fs) == null)
						 return new SimpleValue(false);				
				 return new SimpleValue(true);
			}
		}
		
		 return new SimpleValue(false);
	}

}
