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

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.FieldName;
import jco.ql.model.GeometryDefinition;
import jco.ql.model.command.EGeometryAction;
import jco.ql.model.command.GeometricOptionCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.FieldValue;
import jco.ql.model.value.GeoJsonValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;

public class GeometricOptionCommandEvaluator implements JCOConstants {
	
	public static DocumentDefinition evaluateGeometricOption(Pipeline pipeline, GeometricOptionCommand geometricOption) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		List<FieldDefinition> fields = doc.getFields();
		EGeometryAction geometryAction = geometricOption.getGeometryAction();

		// PF. ~geometry must be deleted because of DROPPING or GENERATE
		if (geometryAction != EGeometryAction.KEEP) {
			for (int i = 0; i < fields.size(); i++) 
				if(GEOMETRY_FIELD_NAME.equals(fields.get(i).getName())) {
					fields.remove(i);
					break;		// there is only one ~geometry
				}
							
			if (EGeometryAction.GENERATE == geometryAction) {
				GeometryDefinition geoDef = geometricOption.getGeometry();
				Geometry geometry;
		
				if(geoDef!=null) {

					// POINT GEOMETRY
					if(geoDef.getType() == GeometryDefinition.POINT) {
						Coordinate[] coordinates = new Coordinate[1];
						// ZUN CHECK* qui va controllato e modificato... la pipeline funziona diversamente
						JCOValue lat = FieldEvaluator.evaluate(pipeline, new FieldValue(geoDef.getLatitude()));
						JCOValue lon = FieldEvaluator.evaluate(pipeline, new FieldValue(geoDef.getLongitude()));
		
						if(lat != null && lon != null) 
							if(lat instanceof SimpleValue && lon instanceof SimpleValue) 
								if((((SimpleValue)lat).getType() == EValueType.DECIMAL || ((SimpleValue)lat).getType() == EValueType.INTEGER) &&
									(((SimpleValue)lon).getType() == EValueType.DECIMAL || ((SimpleValue)lon).getType() == EValueType.INTEGER)) {
		
									coordinates[0] = new Coordinate(getCoordinate((SimpleValue)lon), getCoordinate((SimpleValue)lat));
									CoordinateSequence s = new CoordinateArraySequence(coordinates, 1);
									geometry = new Point(s, new GeometryFactory(new PrecisionModel(), 0));
									fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeoJsonValue(geometry)));
								}
					}

					// FIELD GEOMETRY
					else if(geoDef.getType() == GeometryDefinition.FIELD_REF) {
						// ZUN CHECK* controllare che la pipeline funziona in maiera diversa
						JCOValue g = FieldEvaluator.evaluateGeometry(pipeline, new FieldValue(geoDef.getField()));
						if(g != null) 
							if(g instanceof GeoJsonValue) 
								fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, g));
					} 

					// AGGREGATE GEOMETRY
					else if(geoDef.getType() == GeometryDefinition.AGGREGATE) {
						GeometryCollection collection;
						Geometry[] geometries;
						List<Geometry> listGeometries = new ArrayList<>();
						JCOValue v = FieldEvaluator.evaluate(pipeline, new FieldValue(geoDef.getField()));
		
						if(v !=null) {
							if(v instanceof ArrayValue) {
								List<JCOValue> values = ((ArrayValue)v).getValues();
								for (JCOValue value : values) {
									if(value instanceof DocumentValue) {
										GeoJsonValue temp = (GeoJsonValue)((DocumentValue)value).getValue(FieldName.fromString(GEOMETRY_FIELD_NAME));
										listGeometries.add(temp.getGeometry());
									}
								}
								if(!listGeometries.isEmpty()){
									geometries = listGeometries.toArray(new Geometry[listGeometries.size()]);
									collection = new GeometryCollection(geometries, new GeometryFactory(new PrecisionModel(), 0));
									fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeoJsonValue(collection)));
								}
							} 
						}
					}
						
					// POLYLINE GEOMETRY
					else if(geoDef.getType() == GeometryDefinition.TO_POLYLINE) {
						Geometry lineString;
						Coordinate[] coordinates;
						List<Coordinate> listCoordinate = new ArrayList<>();
						JCOValue v = FieldEvaluator.evaluate(pipeline, new FieldValue(geoDef.getField()));
						if(v != null) {
							if(v instanceof ArrayValue) {
								List<JCOValue> values = ((ArrayValue)v).getValues();
								for (JCOValue value : values) {
									if(value instanceof DocumentValue) {
										GeoJsonValue temp = (GeoJsonValue)((DocumentValue)value).getValue(FieldName.fromString(GEOMETRY_FIELD_NAME));
										listCoordinate.add(new Coordinate(temp.getGeometry().getCentroid().getX(), temp.getGeometry().getCentroid().getY()));
									}
								}
		
								if(!listCoordinate.isEmpty()){
									coordinates = listCoordinate.toArray(new Coordinate[listCoordinate.size()]);
									CoordinateSequence sequence =  new CoordinateArraySequence(coordinates);
									//collection = new GeometryCollection(geometries, new GeometryFactory(new PrecisionModel(), 0));
									lineString = new LineString(sequence, new GeometryFactory(new PrecisionModel(), 0));
									fields.add(new FieldDefinition(GEOMETRY_FIELD_NAME, new GeoJsonValue(lineString)));
								}
							} 
						}
					} 
				}
			}
		}

		return new DocumentDefinition(fields);
	}


	private static double getCoordinate(SimpleValue v) {
		return (Double)(v.getValue());
	}
}
