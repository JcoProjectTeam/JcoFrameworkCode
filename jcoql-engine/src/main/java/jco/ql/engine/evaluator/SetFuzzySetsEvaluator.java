package jco.ql.engine.evaluator;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.SetFuzzySetsCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.fuzzy.FuzzySetDefinitionElement;

// PF. rewritten on 16.07.2021
public class SetFuzzySetsEvaluator implements JCOConstants {
// ZUN CHECK* controllare
    public static List<FieldDefinition> evaluate(DocumentDefinition ld, DocumentDefinition rd, SetFuzzySetsCommand sfsc) {
    	DocumentDefinition fuzzyLeftDoc = null;
    	DocumentDefinition fuzzyRightDoc = null;
    	if (ld.getValue(FUZZYSETS_FIELD_NAME) != null)    	
    		fuzzyLeftDoc = new DocumentDefinition (((DocumentValue) ld.getValue(FUZZYSETS_FIELD_NAME)).getFields());
        if (rd.getValue(FUZZYSETS_FIELD_NAME) != null)
    		fuzzyRightDoc = new DocumentDefinition (((DocumentValue) rd.getValue(FUZZYSETS_FIELD_NAME)).getFields());

        int  policy = sfsc.policyType;
        if (policy == SetFuzzySetsCommand.POLICY_DEFAULT)
        	policy = SetFuzzySetsCommand.POLICY_AND;

    	DocumentDefinition outDoc =  new DocumentDefinition();

    	if (sfsc.setType == SetFuzzySetsCommand.KEEP_ALL) {
    		if (fuzzyLeftDoc != null) 
    			for (int i=0; i<fuzzyLeftDoc.getFields().size(); i++)
    				insertField (outDoc, fuzzyLeftDoc.getFields().get(i), fuzzyLeftDoc.getFields().get(i).getName(), sfsc.policyType);    		
    		if (fuzzyRightDoc != null)
    			for (int i=0; i<fuzzyRightDoc.getFields().size(); i++)
    				insertField (outDoc, fuzzyRightDoc.getFields().get(i), fuzzyRightDoc.getFields().get(i).getName(), sfsc.policyType);    		    			
    	}

    	else if (sfsc.setType == SetFuzzySetsCommand.KEEP_LEFT) {
    		if (fuzzyLeftDoc != null)
    			for (int i=0; i<fuzzyLeftDoc.getFields().size(); i++)
    				insertField (outDoc, fuzzyLeftDoc.getFields().get(i), fuzzyLeftDoc.getFields().get(i).getName(), sfsc.policyType);    		
    	}

    	else if (sfsc.setType == SetFuzzySetsCommand.KEEP_RIGHT) {
    		if (fuzzyRightDoc != null)
    			for (int i=0; i<fuzzyRightDoc.getFields().size(); i++)
    				insertField (outDoc, fuzzyRightDoc.getFields().get(i), fuzzyRightDoc.getFields().get(i).getName(), sfsc.policyType);    		    			
    	}

    	else if (sfsc.setType == SetFuzzySetsCommand.DEFINITION_LIST) {
    		for (int j=0; j<sfsc.fuzzySetsList.size(); j++) {
    			FuzzySetDefinitionElement fsde = sfsc.fuzzySetsList.get(j);
    			// Non-Function case
    			if (!fsde.isFunction()) {
    				DocumentDefinition fuzzyDoc = fuzzyLeftDoc;
    				if (fsde.isRight())
    					fuzzyDoc = fuzzyRightDoc;
    				if (fuzzyDoc != null)
	    				if (fsde.allSide()) {
	    	    			for (int i=0; i<fuzzyDoc.getFields().size(); i++)
	    	    				insertField (outDoc, fuzzyDoc.getFields().get(i), fuzzyDoc.getFields().get(i).getName(), sfsc.policyType);    		    			
	    				}
	    				else if (fuzzyDoc.hasField(fsde.sourceFuzzySet)){
	    					String newName = fsde.sourceFuzzySet;
	    					if (fsde.hasNewName())
	    						newName = fsde.newFuzzySet;
		    				insertField (outDoc, fuzzyDoc.getField(fsde.sourceFuzzySet), newName, sfsc.policyType);    		    			
	    				}
    			}
    			// Function case
    			else {
                    GeometryValue lgj = (GeometryValue) ld.getValue(GEOMETRY_FIELD_NAME);
                    GeometryValue rgj = (GeometryValue) rd.getValue(GEOMETRY_FIELD_NAME);

                	if(lgj != null && rgj != null) {
	                    Geometry lg = lgj.getGeometry();
	                    Geometry rg = rgj.getGeometry();	
	                    double value = 0;
	
	                	if(lg != null && rg != null) {
		                    // HOW-MEET - HOW_MEET
		                    if(fsde.isHowMeetFunction()) {
			                    Geometry boundaryLeft = lg.getBoundary();
			                    Geometry boundaryRight = rg.getBoundary();
		                        if(!boundaryLeft.isEmpty() && !boundaryRight.isEmpty()) {
		                        	if (fsde.isLeft() && boundaryLeft.getLength() != 0) {
		                                value = (boundaryLeft.intersection(boundaryRight)).getLength() / boundaryLeft.getLength();
		                            }
		                            if(fsde.isRight()  && boundaryRight.getLength() != 0) {
		                                value = (boundaryLeft.intersection(boundaryRight)).getLength() / boundaryRight.getLength();
		                            }
		                        }	
		                    }
		                    // INSIDE - HOW_INCLUDE
		                    else if(fsde.isHowIncludeFunction()) {
		                        Geometry intersect = lg.intersection(rg);
		                        if(fsde.isLeft() && lg.getArea() != 0) {
		                            value = intersect.getArea() / lg.getArea();
		                        }
		                        if(fsde.isRight() && rg.getArea() != 0) {
		                            value = intersect.getArea() / rg.getArea();
		                        }
		                    }
			                // OVERLAP - HOW_INTERSECT
		                    else if(fsde.isHowIntersectFunction()) {
		                        Geometry intersect = lg.intersection(rg);
		                        Geometry union = lg.union(rg);
		
		                        if(union.getArea() != 0) {
		                            value = intersect.getArea() / union.getArea();		
		                        }
		                    }
	                    }
	        			JCOValue v = new SimpleValue(value);
	    				insertField (outDoc, new FieldDefinition(fsde.newFuzzySet, v), fsde.newFuzzySet, sfsc.policyType);    		    			
                	}
        		}
    		}
    			
    	}
    	return outDoc.getFields();
    }


	private static void insertField (DocumentDefinition doc, FieldDefinition f, String newName, int policy) {
		if (f != null && f.getValue() != null) {

			// fuzzyset non-present and insert insert FIRST value
			if (!doc.hasField(newName))
				doc.addField(new FieldDefinition(newName, f.getValue()));
			// fuzzyset present, insert according policy
			else {
				// insert LAST value
				if (policy == SetFuzzySetsCommand.POLICY_LAST)
					doc.addField(new FieldDefinition(newName, f.getValue()));
				// insert MIN value		
				// ZUN: 2025.03.06 - Check the cases for complex fuzzy set models and also different models
				else if (policy == SetFuzzySetsCommand.POLICY_AND) {					
					SimpleValue newValue = (SimpleValue)f.getValue();
					SimpleValue oldValue = (SimpleValue) doc.getValue(newName);		
					if (newValue.compareTo(oldValue) < EQUAL) 
						doc.addField(new FieldDefinition(newName, f.getValue()));
				}
				// insert MAX value		
				// ZUN: 2025.03.06 - Check the cases for complex fuzzy set models and also different models
				else if (policy == SetFuzzySetsCommand.POLICY_OR) {
					SimpleValue newValue = (SimpleValue)f.getValue();
					SimpleValue oldValue = (SimpleValue) doc.getValue(newName);		
					if (newValue.compareTo(oldValue) > EQUAL) 
						doc.addField(new FieldDefinition(newName, f.getValue()));
				}
			}
		}
	}

}
