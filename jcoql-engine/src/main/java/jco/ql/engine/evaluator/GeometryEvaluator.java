package jco.ql.engine.evaluator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.GeometryValue;
import jco.ql.parser.model.JoinCollections;

public class GeometryEvaluator implements JCOConstants {

	public static GeometryValue evaluate(int geometryOperation, DocumentDefinition ld, DocumentDefinition rd) {
		GeometryValue leftGeometry = (GeometryValue) ld.getValue(GEOMETRY_FIELD_NAME);
		GeometryValue rightGeometry = (GeometryValue) rd.getValue(GEOMETRY_FIELD_NAME);
		
		GeometryValue outGeo = null;
		if(geometryOperation == JoinCollections.GEOMETRY_LEFT) 								
			outGeo = new GeometryValue(leftGeometry.getGeometry());

		else if(geometryOperation == JoinCollections.GEOMETRY_RIGHT) 				
			outGeo = new GeometryValue(rightGeometry.getGeometry());
			
		else if(geometryOperation == JoinCollections.GEOMETRY_INTERSECTION) 
			outGeo = new GeometryValue(leftGeometry.getGeometry().intersection(rightGeometry.getGeometry()));
		
		else if(geometryOperation == JoinCollections.GEOMETRY_ALL) {
			GeometryCollection collection;
			Geometry[] geometries = new Geometry[2];				
			geometries[0] = leftGeometry.getGeometry();
			geometries[1] = rightGeometry.getGeometry();

			collection = new GeometryCollection(geometries, new GeometryFactory(new PrecisionModel(), 0));
			outGeo = new GeometryValue(collection);
		}
		return outGeo;
	}

}
