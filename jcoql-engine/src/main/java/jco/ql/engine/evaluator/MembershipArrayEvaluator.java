package jco.ql.engine.evaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.parser.model.predicate.MembershipArray;

public class MembershipArrayEvaluator implements JCOConstants {

	public static JCOValue evaluate(MembershipArray function, Pipeline pipeline) {
    	DocumentDefinition doc = pipeline.getCurrentDoc();
    	ArrayValue arrayValue = new ArrayValue ();

		if(function.getMembershipArrayType() == MembershipArray.MA_ALL) {
	    	JCOValue fsv = doc.getValue(JCOConstants.FUZZYSETS_FIELD_NAME);
	    	if (fsv != null && JCOValue.isDocumentValue(fsv)) {
		    	DocumentValue dv = (DocumentValue) fsv;
		    	for (FieldDefinition fd: dv.getFields())
		    		arrayValue.add(fd.getValue());				    		
	    	}
		}
 		else if(function.getMembershipArrayType() == MembershipArray.MA_SELECTED) {
 	    	for (String fsn: function.fuzzySetsSelected) {
 	        	JCOValue jv = doc.getValue(JCOConstants.FUZZYSETS_FIELD_NAME + JCOConstants.DOT + fsn);
 	    		arrayValue.add(jv);
 	    	} 			
 		}
 		else if(function.getMembershipArrayType() == MembershipArray.MA_FROM_ARRAY) {
 	    	JCOValue arrayField = doc.getValue(function.arrayName.toString());
 	    	if (JCOValue.isArrayValue(arrayField)) {
	 	    	ArrayValue array = (ArrayValue) arrayField;
	 	    	for (JCOValue jv : array.getValues()) 
	 	    		if (JCOValue.isDocumentValue(jv)) {
	 	    			DocumentValue dv = (DocumentValue) jv;
	 	            	JCOValue v = dv.getValue(JCOConstants.FUZZYSETS_FIELD_NAME + JCOConstants.DOT + function.fuzzySet);
	 	        		arrayValue.add(v);    			
	 	    		}
 	    	}
 		}

		return arrayValue;
	}
}
