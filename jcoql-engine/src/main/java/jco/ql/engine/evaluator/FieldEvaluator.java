package jco.ql.engine.evaluator;

import org.wololo.jts2geojson.GeoJSONReader;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.reference.FieldReference;
import jco.ql.model.value.FieldValue;
import jco.ql.model.value.GeoJsonValue;
import jco.ql.model.value.JCOValue;


public class FieldEvaluator {
	// ZUN CHECK* questa si potra eliminare
	public static JCOValue evaluate(Pipeline pipeline, JCOValue value) {
		JCOValue outValue = value;

		if(value != null) {
			if (value instanceof FieldValue) {
				FieldValue fieldValue = (FieldValue) value;
				final FieldReference fieldReference = fieldValue.getFieldReference();

				if(fieldReference != null) {
					Object objDoc = pipeline.get(fieldReference.getCollectionAlias());
					if(objDoc != null && objDoc instanceof DocumentDefinition) {
						outValue = ((DocumentDefinition) objDoc).getValue(fieldReference.getFieldName());
					}
				}
			}
		}

		return outValue;
	}

	

// PF - method added in order to evaluate if a field is a GeoJson geometry 
	public static JCOValue evaluateGeometry(Pipeline pipeline, JCOValue value) {
		JCOValue outValue = value;
		if(value != null && value instanceof FieldValue) {
			FieldValue fieldValue = (FieldValue) value;
			final FieldReference fieldReference = fieldValue.getFieldReference();

			if(fieldReference != null) {
				Object objDoc = pipeline.get(fieldReference.getCollectionAlias());
				if(objDoc != null && objDoc instanceof DocumentDefinition) {
					try {
						outValue = ((DocumentDefinition) objDoc).getValue(fieldReference.getFieldName());
						outValue  = new GeoJsonValue(new GeoJSONReader().read(outValue.toString()));
					} catch (Exception e) {
						outValue = null;
					}
				}
			}
		}
		return outValue;
	}



	
}
