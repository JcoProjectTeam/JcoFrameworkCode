package jco.ql.engine.byZunEvaluator;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.EOrientation;
import jco.ql.parser.model.util.SpatialFunction;


public class SpatialFunctionEvaluator implements JCOConstants {
	public final static double EARTH_RADIUS_KM 				= 6371;
	public final static double EARTH_RADIUS_M 				= 6371*1000;
	public final static double KM_2_MILE 					= 1.609344;
	public final static double KM_2_M 						= 1000;
	public final static double KM_2_M_SQUARE 				= 1000*1000;
	public final static double M_2_MILE_SQUARE 				= 1609.344*1609.344; 	

	public static boolean matchSpatialCondition(SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
		if (spatialFunction.type == SpatialFunction.DISTANCE)
			return matchComparison (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.AREA)
			return matchComparison (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.LENGTH)
			return matchComparison (spatialFunction, leftGeo, rightGeo);
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
		else if (spatialFunction.type == SpatialFunction.AREA)
			value = getArea (spatialFunction, leftGeo, rightGeo);
		else if (spatialFunction.type == SpatialFunction.LENGTH)
			value = getLength (spatialFunction, leftGeo, rightGeo);
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
	
	
	private static boolean matchComparison (SpatialFunction spatialFunction, Geometry leftGeo, Geometry rightGeo) {
		JCOValue v1, v2;
		v1 = new SimpleValue(-1.0);
		v2 = new SimpleValue(-1.0);
		if (spatialFunction.type == SpatialFunction.DISTANCE) {
			v1 = getDistance (spatialFunction, leftGeo, rightGeo);
			v2 = new SimpleValue (spatialFunction.distance);
		}
		else if (spatialFunction.type == SpatialFunction.AREA) {
			v1= getArea (spatialFunction, leftGeo, rightGeo);
			v2 = new SimpleValue (spatialFunction.area);			
		}
		else {// if (spatialFunction.type == SpatialFunction.LENGTH) {
			v1= getLength (spatialFunction, leftGeo, rightGeo);
			v2 = new SimpleValue (spatialFunction.length);						
		}

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

	
	private static JCOValue getArea (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
        Geometry temp = lg.intersection(rg);
        double area = 0;
        if (temp.getGeometryType().equals("Polygon")) {
            Coordinate[] coord = temp.getCoordinates();
            List<Point> locations = new ArrayList<>();
            for (Coordinate coordinate : coord) 
                locations.add(GeometryFactory.createPointFromInternalCoord(coordinate, lg.getCentroid()));

            // area in square meters
            area = calculateAreaOfPolygonOnSphereInSquareMeters(locations);
            if (spatialFunction.unit.equals("ML"))
                area = area / M_2_MILE_SQUARE;
            else if (spatialFunction.unit.equals("KM")) 
                area = area / KM_2_M_SQUARE;
        }
        SimpleValue value = new SimpleValue(area);
        return value;				
	}

	
	private static JCOValue getLength (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
        Geometry temp = lg.intersection(rg);
        double len = 0;
        if (temp.getGeometryType().equals("LineString")) {
            Coordinate[] coord = temp.getCoordinates();
            for (int i=1; i<coord.length; i++) {
        		double lat1 = coord[i-1].getY();
        		double lon1 = coord[i-1].getX();
        		double lat2 = coord[i].getY();
        		double lon2 = coord[i].getX();    
        	
        		double dLat = Math.toRadians(lat2-lat1);  
        		double dLon = Math.toRadians(lon2-lon1);
        		  		  
        		double a = 	Math.sin(dLat/2) * Math.sin(dLat/2) +
        					Math.cos(Math.toRadians(lat1)) * 
        					Math.cos(Math.toRadians(lat2)) * 
        					Math.sin(dLon/2) * Math.sin(dLon/2);
        		len += 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
            }

    		len = len * EARTH_RADIUS_KM;     		
            if (spatialFunction.unit.equals("M")) {
            	len = len * KM_2_M;
            } else if (spatialFunction.unit.equals("ML")) {
            	len = len / KM_2_MILE;
            }
        }
        SimpleValue value = new SimpleValue(len);
        return value;				
	}

	
	
	private static JCOValue getOrientation (SpatialFunction spatialFunction, Geometry lg, Geometry rg) {
        double angle = getAngle (spatialFunction, lg, rg);
        String orientation = EOrientation.getOrientationFromAngle(angle).toString();
        SimpleValue value = new SimpleValue(orientation);
        return value;
	}


	// *************************************************************************************************
	
		
	private static double calculateAreaOfPolygonOnSphereInSquareMeters(List<Point> locations) {
		if (locations.size() < 3) {
			return 0;
		}

		final double diameter = EARTH_RADIUS_M * 2;
		final double circumference = diameter * Math.PI;
		final List<Double> listY = new ArrayList<Double>();
		final List<Double> listX = new ArrayList<Double>();
		final List<Double> listArea = new ArrayList<Double>();

		// segment calculation for each point
		final double latitudeRef = locations.get(0).getY();
		final double longitudeRef = locations.get(0).getX();
		for (int i = 1; i < locations.size(); i++) {
			final double latitude = locations.get(i).getY();
			final double longitude = locations.get(i).getX();

			listY.add(calculateYSegment(latitudeRef, latitude, circumference));
			listX.add(calculateXSegment(longitudeRef, longitude, latitude, circumference));
		}

		// triangle area calculation
		for (int i = 1; i < listX.size(); i++) {
			final double x1 = listX.get(i - 1);
			final double y1 = listY.get(i - 1);
			final double x2 = listX.get(i);
			final double y2 = listY.get(i);
			listArea.add(calculateAreaInSquareMeters(x1, x2, y1, y2));

		}

		// sum of triangle area
		double areasSum = 0;
		for (final Double area : listArea) {
			areasSum = areasSum + area;
		}

		// area in absolute value: (if points are evaluated clockwise it would be negative)
		return Math.abs(areasSum);// Math.sqrt(areasSum * areasSum);
	}
	private static Double calculateAreaInSquareMeters(double x1, double x2, double y1, double y2) {
		return (y1 * x2 - x1 * y2) / 2;
	}

	
	private static double calculateYSegment(double latitudeRef, double latitude, double circumference) {
		return (latitude - latitudeRef) * circumference / 360.0;
	}

	
	private static double calculateXSegment(double longitudeRef, double longitude, double latitude, double circumference) {
		return (longitude - longitudeRef) * circumference * Math.cos(Math.toRadians(latitude)) / 360.0;
	}
	

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
