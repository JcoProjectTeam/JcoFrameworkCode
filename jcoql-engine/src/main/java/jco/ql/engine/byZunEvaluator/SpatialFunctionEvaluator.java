package jco.ql.engine.byZunEvaluator;


import org.locationtech.jts.geom.Geometry;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.EOrientation;
import jco.ql.parser.model.util.SpatialFunction;


public class SpatialFunctionEvaluator implements JCOConstants {

	public static boolean matchSpatialCondition(SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
		if (spatialFunction.type == SpatialFunction.DISTANCE)
			return matchDistance (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.ORIENTATION)
			return compareOrientation (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.INCLUDED_LEFT)
			return rightGeo.contains(leftGeo);
		else if (spatialFunction.type == SpatialFunction.INCLUDED_RIGHT)
			return leftGeo.contains(rightGeo);
		else if (spatialFunction.type == SpatialFunction.MEET)
			return leftGeo.getBoundary().intersects(rightGeo.getBoundary());
		else if (spatialFunction.type == SpatialFunction.INTERSECT)
			return leftGeo.intersects(rightGeo);
		return false;
	}

	
	public static JCOValue evaluate (SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
		JCOValue value = new SimpleValue ();  // null default value

		if (spatialFunction.type == SpatialFunction.DISTANCE)
			value = getDistance (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.ORIENTATION)
			value = getOrientation(spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.INCLUDED_LEFT)
			value = new SimpleValue (rightGeo.contains(leftGeo));
		else if (spatialFunction.type == SpatialFunction.INCLUDED_RIGHT)
			value = new SimpleValue (leftGeo.contains(rightGeo));
		else if (spatialFunction.type == SpatialFunction.MEET)
            value = new SimpleValue(leftGeo.getBoundary().intersects(rightGeo.getBoundary()));
		else if (spatialFunction.type == SpatialFunction.INTERSECT)
            value = new SimpleValue(leftGeo.intersects(rightGeo));

		return value;
	}

// ****************************************************************************************************
	
	
	private static boolean matchDistance (SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
		JCOValue v1 = getDistance (spatialFunction, leftGeo, rightGeo);
		JCOValue v2 = new SimpleValue (spatialFunction.distance);

		return ComparisonPredicateEvaluator.compare(v1, v2, spatialFunction.comparatorType);
	}


	private static boolean compareOrientation(SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
        double angle = getAngle (spatialFunction, leftGeo, rightGeo);
        EOrientation orientation = spatialFunction.orientation;
        return orientation.closeAngle(angle, spatialFunction.delta);
	}

	
	// ****************************************************************************************************

	private static JCOValue getDistance (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
		double lat1 = lg.getCentroid().getY();
		double lon1 = lg.getCentroid().getX();
		double lat2 = rg.getCentroid().getY();
		double lon2 = rg.getCentroid().getX();    
	
		double dLat = Math.toRadians(lat2-lat1);  
		double dLon = Math.toRadians(lon2-lon1);
		  		  
		double a = 	Math.sin(dLat/2) * Math.sin(dLat/2) +
					Math.cos(Math.toRadians(lat1)) * 
					Math.cos(Math.toRadians(lat2)) * 
					Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double distance = c * EARTH_RADIUS_KM; 
		
        if (spatialFunction.unit.equals("M")) {
            distance = distance * KM_2_M;
        } else if (spatialFunction.unit.equals("ML")) {
            distance = distance / KM_2_MILE;
        }

        SimpleValue value = new SimpleValue(distance);
        return value;		
	}

	
	private static JCOValue getOrientation (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
        double angle = getAngle (spatialFunction, lg, rg);
        String orientation = EOrientation.getOrientationFromAngle(angle).toString();
        SimpleValue value = new SimpleValue(orientation);
        return value;
	}


	// *************************************************************************************************
	
		
	

	private static double getAngle (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
        double angle = 0;
        if (spatialFunction.from.equals("RIGHT")) 
            angle = angleFromCoordinate(Math.toRadians(lg.getCentroid().getY()),
            							Math.toRadians(lg.getCentroid().getX()), 
            							Math.toRadians(rg.getCentroid().getY()),
            							Math.toRadians(rg.getCentroid().getX()));
        else if (spatialFunction.from.equals("LEFT"))
            angle = angleFromCoordinate(Math.toRadians(rg.getCentroid().getY()),
					            		Math.toRadians(rg.getCentroid().getX()), 
					            		Math.toRadians(lg.getCentroid().getY()),
					                    Math.toRadians(lg.getCentroid().getX()));
		return angle;
	}

	
	public static double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {

		double dLon = (long2 - long1);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		double brng = Math.atan2(y, x);

		brng = Math.toDegrees(brng);
		brng = (brng + 360) % 360;

		return brng;
	}

}
