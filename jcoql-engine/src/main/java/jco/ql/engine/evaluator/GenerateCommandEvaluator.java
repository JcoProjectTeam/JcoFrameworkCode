package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.BuildAction;
import jco.ql.parser.model.util.Field;
import jco.ql.parser.model.util.ObjectStructure;
import jco.ql.parser.model.util.OutputFieldSpec;

public class GenerateCommandEvaluator implements JCOConstants {

	public static DocumentDefinition evaluateBuildAction(Pipeline pipeline, BuildAction buildAction) {
		DocumentDefinition curDoc = pipeline.getCurrentDoc();
		List<FieldDefinition> startingFields = curDoc.getFields();
		List<FieldDefinition> fields = new ArrayList<FieldDefinition> ();

		// adding ~geometry and ~fuzzysets by default on the beginning
		for (int i = 0; i < startingFields.size(); i++) 
			if(GEOMETRY_FIELD_NAME.equals(startingFields.get(i).getName()) || FUZZYSETS_FIELD_NAME.equals(startingFields.get(i).getName()))
				fields.add(startingFields.get(i));

		if (buildAction.getType() == BuildAction.BUILD_ACTION)
			fields.addAll(evaluateObjectStructure (pipeline, buildAction.objectStructure));	
		if (buildAction.getType() == BuildAction.ADD_ACTION)
			;
		if (buildAction.getType() == BuildAction.REMOVE_ACTION)
			;

		return new DocumentDefinition(fields);
	}
		
	
	
	private static List<FieldDefinition> evaluateObjectStructure(Pipeline pipeline, ObjectStructure objectStructure) {
		JCOValue value = new SimpleValue();
		List<FieldDefinition> outputList = new ArrayList<FieldDefinition> ();
		for (OutputFieldSpec ofs : objectStructure.outputList) {
			if (ofs.type == OutputFieldSpec.OBJECT_STRUCTURE) {
				List<FieldDefinition> tempList = evaluateObjectStructure (pipeline, ofs.valueObjectStructure);
				DocumentDefinition tempDoc = new DocumentDefinition (tempList);
				value = new DocumentValue(tempDoc);
			}
			else
				value = ExpressionFactorEvaluator.evaluate(ofs.factor, pipeline);				

			if (!JCOValue.isNull(value))
				insertFieldValue (outputList, ofs.fieldRef, value);
		}
		
		return outputList;
	}



	private static void insertFieldValue(List<FieldDefinition> outputList, Field fieldRef, JCOValue value) {
		final int NOT_FOUND = -1;
		int found = NOT_FOUND;

		String root = fieldRef.fields.get(0).replace(FIELD_SEPARATOR, "");
		if (fieldRef.size() == 1) {
			found = NOT_FOUND;
			for (int i=0; i<outputList.size(); i++) {
				if (outputList.get(i).getName().equals(root)) {
					outputList.get(i).setValue(value);
					found = i;
					break;
				}
			}
			if (found == NOT_FOUND) {
				FieldDefinition fd = new FieldDefinition(fieldRef.toString(), value);
				outputList.add(fd);
			}
		}
	
		else {
			Field suffixField = fieldRef.cloneSuffix();
			found = NOT_FOUND;
			for (int i=0; i<outputList.size(); i++) {
				FieldDefinition fd = outputList.get(i);
				if (fd.getName().equals(root)) {
					found = i;
					JCOValue currentValue = fd.getValue();
					if (currentValue.getType() == EValueType.DOCUMENT) {
						DocumentDefinition curDoc = (DocumentDefinition) currentValue.getValue();
						List<FieldDefinition> subOutputList = curDoc.getFields();
						insertFieldValue (subOutputList, suffixField, value);
						DocumentDefinition newDoc = new DocumentDefinition(subOutputList);
						JCOValue newValue = new DocumentValue(newDoc);
						fd.setValue(newValue);
					}
					else {
						List<FieldDefinition> subOutputList = new ArrayList<FieldDefinition>();
						insertFieldValue (subOutputList, suffixField, value);
						DocumentDefinition newDoc = new DocumentDefinition(subOutputList);
						JCOValue newValue = new DocumentValue(newDoc);
						fd.setValue(newValue);
					}

					break;
				}
			}
			if (found == NOT_FOUND) {
				List<FieldDefinition> subOutputList = new ArrayList<FieldDefinition>();
				insertFieldValue (subOutputList, suffixField, value);
				DocumentDefinition newDoc = new DocumentDefinition(subOutputList);
				JCOValue newValue = new DocumentValue(newDoc);
				FieldDefinition fd = new FieldDefinition (root, newValue);
				outputList.add(fd);
			}
			
		}
	}

}
