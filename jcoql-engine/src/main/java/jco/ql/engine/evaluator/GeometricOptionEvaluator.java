package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.wololo.jts2geojson.GeoJSONReader;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.GeometricOption;

public class GeometricOptionEvaluator implements JCOConstants {
	
	public static DocumentDefinition evaluateGeometricOption(Pipeline pipeline, GeometricOption geometricOption) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		List<FieldDefinition> fields = doc.getFields();

		// PF. ~geometry must be deleted if not KEEPING (UNDEFINED = KEEPING by default
		if (geometricOption.getType() != GeometricOption.KEEPING && geometricOption.getType() != GeometricOption.UNDEFINED) {
			// remove old ~geometry
			for (int i = 0; i < fields.size(); i++) 
				if(GEOMETRY_FIELD_NAME.equals(fields.get(i).getName())) {
					fields.remove(i);
					break;		// there is only one ~geometry
				}

			// POINT GEOMETRY
			if(geometricOption.getType() == GeometricOption.POINT) {
				Coordinate[] coordinates = new Coordinate[1];
				JCOValue lat = doc.getValue(geometricOption.latitude.toString());
				JCOValue lon = doc.getValue(geometricOption.longitude.toString());

				if (JCOValue.isNumericValue(lon) && JCOValue.isNumericValue(lat)) {
					coordinates[0] = new Coordinate(getCoordinate((SimpleValue)lon), getCoordinate((SimpleValue)lat));
					CoordinateSequence s = new CoordinateArraySequence(coordinates, 1);
					Geometry geometry = new Point(s, new GeometryFactory(new PrecisionModel(), 0));
					fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeometryValue(geometry)));
				}
			}

			// FIELD GEOMETRY
			else if(geometricOption.getType() == GeometricOption.FIELD_REF) {
				JCOValue g = doc.getValue(geometricOption.fieldRef.toString());
				if(JCOValue.isDocumentValue(g)) {
					GeometryValue gValue  = new GeometryValue(new GeoJSONReader().read(g.toString()));
					if (gValue != null)
						fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, gValue));
				}
			} 

			// AGGREGATE GEOMETRY
			else if(geometricOption.getType() == GeometricOption.AGGREGATE) {
				GeometryCollection collection;
				Geometry[] geometries;
				List<Geometry> listGeometries = new ArrayList<>();
				JCOValue v = doc.getValue(geometricOption.fieldRef.toString());

				if(JCOValue.isArrayValue(v)) {
					List<JCOValue> values = ((ArrayValue)v).getValues();
					for (JCOValue value : values) {
						if(JCOValue.isDocumentValue(value)) {
							JCOValue gValue = ((DocumentValue) value).getValue(GEOMETRY_FIELD_NAME);
							if (JCOValue.isGeometryValue(gValue))  {
								GeometryValue gjValue = (GeometryValue)gValue;
								listGeometries.add(gjValue.getGeometry());
							}
						}
					}

					if(!listGeometries.isEmpty()){
						geometries = listGeometries.toArray(new Geometry[listGeometries.size()]);
						collection = new GeometryCollection(geometries, new GeometryFactory(new PrecisionModel(), 0));
						fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeometryValue(collection)));
					}
				}
			}
				
			// POLYLINE GEOMETRY
			else if(geometricOption.getType() == GeometricOption.TO_POLYLINE) {
				Geometry lineString;
				Coordinate[] coordinates;
				List<Coordinate> listCoordinate = new ArrayList<>();
				JCOValue v = doc.getValue(geometricOption.fieldRef.toString());

				if(JCOValue.isArrayValue(v)) {
					List<JCOValue> values = ((ArrayValue)v).getValues();
					for (JCOValue value : values) {
						if(JCOValue.isDocumentValue(value)) {
							JCOValue gValue = ((DocumentValue) value).getValue(GEOMETRY_FIELD_NAME);
							if (JCOValue.isGeometryValue(gValue))  {
								GeometryValue gjValue = (GeometryValue)gValue;
								listCoordinate.add(new Coordinate(gjValue.getGeometry().getCentroid().getX(), gjValue.getGeometry().getCentroid().getY()));
							}
						}
					}

					if(!listCoordinate.isEmpty()){
						coordinates = listCoordinate.toArray(new Coordinate[listCoordinate.size()]);
						CoordinateSequence sequence =  new CoordinateArraySequence(coordinates);
						lineString = new LineString(sequence, new GeometryFactory(new PrecisionModel(), 0));
						fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeometryValue(lineString)));
					}
				}
			} 
		}

		return new DocumentDefinition(fields);
	}

	
	public static DocumentDefinition removeGeometry(DocumentDefinition doc) {
		boolean change = false;

		if (doc == null)
			return doc;
		
		List<FieldDefinition> fields = doc.getFields();

		// remove old ~geometry
		for (int i = 0; i < fields.size(); i++) 
			if(GEOMETRY_FIELD_NAME.equals(fields.get(i).getName())) {
				fields.remove(i);
				change = true;
				break;		// there is only one ~geometry
			}
		if (change)
			return new DocumentDefinition(fields);		

		return doc;
	}


	private static double getCoordinate(SimpleValue v) {
		return (Double)(v.getValue());
	}

}
