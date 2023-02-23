package jco.ql.engine.byZunEvaluator;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.FunctionFactor;

public class FunctionEvaluator implements JCOConstants {
	
	public static JCOValue evaluate(FunctionFactor function, Pipeline pipeline) {
		if (function.getFunctionType() == FunctionFactor.FUNCTION)
			return UserDefinedFunctionEvaluator.evaluate (function, pipeline);
		
		if (function.getFunctionType() == FunctionFactor.COUNT_FUNCTION)
			return getCountValue (function, pipeline);
		
		if (function.getFunctionType() == FunctionFactor.TO_STRING_FUNCTION)
			return toStringValue (function, pipeline);
		
		if (function.getFunctionType() == FunctionFactor.TO_INT_FUNCTION)
			return toIntValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.TO_FLOAT_FUNCTION)
			return toFloatValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.TO_BOOL_FUNCTION)
			return toBoolValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.SERIALIZE_FUNCTION)
			return toSerializedValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.ABS_FUNCTION)
			return getAbsValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.MIN_FUNCTION)
			return getMinValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.MAX_FUNCTION)
			return getMaxValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.GEO_DISTANCE_FUNCTION)
			return getGeodesicDistanceValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.JARO_WINKLER_FUNCTION)
			return getJaroWinklerSimilarityValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.GEOMETRY_FIELD_FUNCTION)
			return getGeometryFieldValue (pipeline);

		if (function.getFunctionType() == FunctionFactor.GEOMETRY_LENGTH_FUNCTION)
			return getGeometryLengthValue (function, pipeline);

		if (function.getFunctionType() == FunctionFactor.GEOMETRY_AREA_FUNCTION)
			return getGeometryAreaValue (function, pipeline);

		return new SimpleValue (); // null value
	}

	

	// ----------------------------------------------------------------------------------------

	private static SimpleValue getCountValue(FunctionFactor factor, Pipeline pipeline) {
		long count = 1;		// defaultValue
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		
		if (value.getType() == EValueType.NULL) 
			count = 0;
		else if (value.getType() == EValueType.ARRAY) {
			count = 0;
			List<JCOValue> listValues = ((ArrayValue) value).getValues();							
			if (listValues != null)
				count = listValues.size();
		}

		return new SimpleValue (count);		
	}

	
	private static JCOValue toBoolValue(FunctionFactor factor, Pipeline pipeline) {
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);

		if (value.getType() == EValueType.NULL) 
			return new SimpleValue (false);
		
		if (value.getType() == EValueType.BOOLEAN) 
			return value;
	
		if (value.getType() == EValueType.INTEGER || value.getType() == EValueType.DECIMAL) {
			double d = Double.parseDouble(value.getStringValue().replace("\"", "").replace(",", ".").trim());
			return new SimpleValue (d != 0);
		}

		if (value.getType() == EValueType.STRING) 
			return new SimpleValue (!"".equals(value.getStringValue()));
		
		if (value.getType() == EValueType.ARRAY) {
			long count = 0;
			List<JCOValue> listValues = ((ArrayValue) value).getValues();							
			if (listValues != null)
				count = listValues.size();

			return new SimpleValue (count > 0);
		} 
		// any other case with value != null
		return new SimpleValue (true);
	}

	
	private static JCOValue toIntValue(FunctionFactor factor, Pipeline pipeline) {
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		long toInt = 0; 	// default Value for NULL & ARRAY & DOCUMENT 
		if (value.getType() == EValueType.BOOLEAN) {
			if (value.getStringValue().equalsIgnoreCase(TRUE_LCST))
				toInt = 1;
		}
		else if (value.getType() == EValueType.INTEGER || 
				value.getType() == EValueType.DECIMAL || 
				value.getType() == EValueType.STRING) {
			double d;
			try {
				d = Double.parseDouble(value.getStringValue().replace("\"", "")
																	.replace("'", "")
																	.replace(",", ".").trim());
			} catch (NumberFormatException nfe) {
				d = 0.0;
			}
			toInt = (long) d;	// decimal part is truncated
		}
		return new SimpleValue (toInt);
	}


	private static JCOValue toFloatValue(FunctionFactor factor, Pipeline pipeline) {
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		double toFloat = 0.0; 	// default Value for NULL & ARRAY & DOCUMENT 
		if (value.getType() == EValueType.BOOLEAN) {
			if (value.getStringValue().equalsIgnoreCase(TRUE_LCST))
				toFloat = 1.0;
		}
		else if (value.getType() == EValueType.INTEGER || 
				value.getType() == EValueType.DECIMAL || 
				value.getType() == EValueType.STRING) {
			try {
				toFloat = 1.0 * Double.parseDouble(value.getStringValue().replace("\"", "")
																		.replace("'", "")
																		.replace(",", ".").trim());
			} catch (NumberFormatException nfe) {
				toFloat = 0.0;
			}
		}
		return new SimpleValue (toFloat);
	}


	private static JCOValue toStringValue(FunctionFactor factor, Pipeline pipeline) {
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		if (value.getType() == EValueType.NULL)
			return value;
		if (value.getType() == EValueType.DOCUMENT 	|| 
			value.getType() == EValueType.GEOMETRY 	||
				value.getType() == EValueType.ARRAY)
			return new SimpleValue ("");
		
		return new SimpleValue (value.getStringValue());
	}


	private static JCOValue toSerializedValue(FunctionFactor factor, Pipeline pipeline) {
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		if (value.getType() == EValueType.NULL)
			return value;

		return new SimpleValue (value.getStringValue());
	}

	
	private static JCOValue getAbsValue(FunctionFactor factor, Pipeline pipeline) {
		if (factor.functionParams.size() != 1)
			return new SimpleValue (); 	// null value
		JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);

		if (JCOValue.isIntValue(value)) {
			int v = JCOValue.getIntValue(value);
			if (v < 0)
				return new SimpleValue (-v);
			return value;
		}
		if (JCOValue.isDoubleValue(value)) {
			double v = JCOValue.getIntValue(value);
			if (v < 0)
				return new SimpleValue (-v);
			return value;
		}
		return new SimpleValue (); // null value
	}


	private static JCOValue getMinValue(FunctionFactor factor, Pipeline pipeline) {
		if (factor.functionParams.size() == 0)
			return new SimpleValue (); 	// null value

		double min, v;
		JCOValue minValue = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		if (!JCOValue.isNumericValue(minValue))
			return new SimpleValue (); // null value
		else
			min = JCOValue.getDoubleValue(minValue);

		for (int i=1; i<factor.functionParams.size(); i++) {
			JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
			if (!JCOValue.isNumericValue(value))
				return new SimpleValue (); // null value
			else
				v = JCOValue.getDoubleValue(value);
			
			if (v < min) {
				minValue = value;
				min = v;
			}
		}
		return minValue; 
	}


	private static JCOValue getMaxValue(FunctionFactor factor, Pipeline pipeline) {
		if (factor.functionParams.size() == 0)
			return new SimpleValue (); 	// null value

		double max, v;
		JCOValue maxValue = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
		if (!JCOValue.isNumericValue(maxValue))
			return new SimpleValue (); // null value
		else
			max = JCOValue.getDoubleValue(maxValue);

		for (int i=1; i<factor.functionParams.size(); i++) {
			JCOValue value = ExpressionPredicateEvaluator.calculate(factor.functionParams.get(0), pipeline);
			if (!JCOValue.isNumericValue(value))
				return new SimpleValue (); // null value
			else
				v = JCOValue.getDoubleValue(value);
			
			if (v > max) {
				maxValue = value;
				max = v;
			}
		}
		return maxValue; 
	}

	
	private static JCOValue getGeodesicDistanceValue(FunctionFactor function, Pipeline pipeline) {
		JCOValue jcoLat1, jcoLat2, jcoLon1, jcoLon2;
		double lat1, lat2, lon1, lon2;
		if (!function.checkParamNumber()) {
			JMH.add("Wrong number of parameters for " + function.functionName + " function:"
					+ "\n" + function.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value
		}
		
		jcoLat1 = ExpressionPredicateEvaluator.calculate(function.functionParams.get(0), pipeline);
		jcoLon1 = ExpressionPredicateEvaluator.calculate(function.functionParams.get(1), pipeline);
		jcoLat2 = ExpressionPredicateEvaluator.calculate(function.functionParams.get(2), pipeline);
		jcoLon2 = ExpressionPredicateEvaluator.calculate(function.functionParams.get(3), pipeline);
		
		if (!JCOValue.isNumericValue(jcoLat1) || !JCOValue.isNumericValue(jcoLat2) || 
			!JCOValue.isNumericValue(jcoLon1) || !JCOValue.isNumericValue(jcoLon2)) {
			JMH.add("Coordinates for " + function.functionName + " function must be numeric:"
					+ "\n" + function.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value
		}

		lat1 = JCOValue.getDoubleValue(jcoLat1); 
		lon1 = JCOValue.getDoubleValue(jcoLon1); 
		lat2 = JCOValue.getDoubleValue(jcoLat2); 
		lon2 = JCOValue.getDoubleValue(jcoLon2);
		if (lat1 < -90	|| lat1 > +90 || lat2 < -90 || lat2 > +90) {
			JMH.add("Latitudes for " + function.functionName + " function must in [-90, 90] degrees range:"
					+ "\n" + function.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value			
		}
		if (lon1 < -180	|| lon1 > +180 || lon2 < -180 || lon2 > +180) {
			JMH.add("Longitdes for " + function.functionName + " function must in [-180, 180] degrees range:"
				+ "\n" + function.toString()
				+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value						
		}

		int R = 6371; 																				// Earth radius in km
		double dLat = (lat2-lat1) * (Math.PI/180);
		double dLon = (lon2-lon1) * (Math.PI/180);
		double a = Math.pow(Math.sin(dLat/2), 2) +
					Math.cos(lat1 * (Math.PI/180)) *
					Math.cos(lat2 * (Math.PI/180)) *
					Math.pow(Math.sin(dLon/2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c; 																				// Distance in km
		return new SimpleValue (d);
	}


	private static JCOValue getJaroWinklerSimilarityValue(FunctionFactor function, Pipeline pipeline) {
		JCOValue sv1, sv2;
		if (!function.checkParamNumber()) {
			JMH.add("Wrong number of parameters for " + function.functionName + " function:"
					+ "\n" + function.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value
		}
		
		sv1= ExpressionPredicateEvaluator.calculate(function.functionParams.get(0), pipeline);
		sv2= ExpressionPredicateEvaluator.calculate(function.functionParams.get(1), pipeline);

		if (!JCOValue.isStringValue(sv1) || !JCOValue.isStringValue(sv2)) {
			JMH.add("Parameters for " + function.functionName + " function must be string:"
					+ "\n" + function.toString()
					+ "\n" + pipeline.getCurrentDoc().toString() );
			return new SimpleValue ();	// empty value
		}
		
		String s1 = sv1.getStringValue().toLowerCase();
		String s2 = sv2.getStringValue().toLowerCase();
		
		return new SimpleValue (jaroWinkler (s1, s2));

	}

	
    // Jaro Winkler Similarity
	// https://www.geeksforgeeks.org/jaro-and-jaro-winkler-similarity/
    private static double jaroWinkler(String s1, String s2)
    {
        double jaro_dist = jaroDistance(s1, s2); 
        // If the jaro Similarity is above a threshold
        if (jaro_dist > 0.7) {     
            // Find the length of common prefix
            int prefix = 0;
     
            for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
                // If the characters match
                if (s1.charAt(i) == s2.charAt(i))
                    prefix++;
                else
                    break;
            }
     
            // Maximum of 4 characters are allowed in prefix
            prefix = Math.min(4, prefix);     
            // Calculate jaro winkler Similarity
            jaro_dist += 0.1 * prefix * (1 - jaro_dist);
        }
        return jaro_dist;
    }	

    
    private static double jaroDistance(String s1, String s2)
    {
        // If the strings are equal
        if (s1 == s2)
            return 1.0;
     
        // Length of two strings
        int len1 = s1.length(),
            len2 = s2.length();
     
        if (len1 == 0 || len2 == 0)
            return 0.0;
     
        // Maximum distance upto which matching
        // is allowed
        int max_dist = (int)Math.floor(Math.max(len1, len2) / 2) - 1;
     
        // Count of matches
        int match = 0;
     
        // Hash for matches
        int hash_s1[] = new int [s1.length()];
        int hash_s2[] = new int[s2.length()];
     
        // Traverse through the first string
        for (int i = 0; i < len1; i++) {
            // Check if there is any matches
            for (int j = Math.max(0, i - max_dist);
                j < Math.min(len2, i + max_dist + 1); j++)
                 
            // If there is a match
            if (s1.charAt(i) == s2.charAt(j) && hash_s2[j] == 0) {
                hash_s1[i] = 1;
                hash_s2[j] = 1;
                match++;
                break;
            }
        }
     
        // If there is no match
        if (match == 0)
            return 0.0;
     
        // Number of transpositions
        double t = 0;
     
        int point = 0;
     
        // Count number of occurrences
        // where two characters match but
        // there is a third matched character
        // in between the indices
        for (int i = 0; i < len1; i++)
            if (hash_s1[i] == 1) {
                // Find the next matched character
                // in second string
                while (hash_s2[point] == 0)
                    point++;
     
                if (s1.charAt(i) != s2.charAt(point++))
                    t++;
            }
     
        t /= 2;
     
        // Return the Jaro Similarity
        return ((	(double)match) / ((double)len1) 
                +	((double)match) / ((double)len2)
                + ((double)match - t) / ((double)match))
            / 3.0;
    }

// ---------------------------------------------------------
    

	private static JCOValue getGeometryFieldValue(Pipeline pipeline) {
		DocumentDefinition doc = pipeline.getCurrentDoc();
		if (doc == null)
			return new SimpleValue ();		// null value

		return doc.getValue(GEOMETRY_FIELD_NAME_DOT);
	}

	private static JCOValue getGeometryLengthValue (FunctionFactor function, Pipeline pipeline) {
        Geometry geo = getGeometry(function, pipeline);
		if (geo == null) {
			JMH.addJCOMessage("No valid geometry to evaluate for function " + function.functionName);
			// empty value
			return new SimpleValue();
		}
        // By construction there's only one parameters
		String unit = getUnit(function, pipeline);
		if (unit == null) {
			JMH.addJCOMessage("Wrong Unit parameters:\t" + unit + " (use 'M', 'KM' or 'ML' in " + function.functionName + ")");
			// empty value
			return new SimpleValue();				
		} 
			
        double len = 0;
		if (geo instanceof LineString) 
			len = getLineLength((LineString) geo);
		else if (geo instanceof MultiLineString) 
			len = getMultiLineLength((MultiLineString) geo);			
		else if (geo instanceof MultiPolygon) 
			len += getMultiPolygonLength ((MultiPolygon) geo);        		
		else if (geo instanceof GeometryCollection) 
			len += getGeometryCollectionLength ((GeometryCollection) geo);        		

		len = len * EARTH_RADIUS_KM;     		
        if (unit.equals("M")) {
        	len = len * KM_2_M;
        } else if (unit.equals("ML")) {
        	len = len / KM_2_MILE;
        }
        SimpleValue value = new SimpleValue(len);
        return value;				
	}

	private static double getGeometryCollectionLength (GeometryCollection geoColl) {
        double len = 0;
		for (int i=0; i<geoColl.getNumGeometries(); i++) {
			Geometry geo = geoColl.getGeometryN(i);
	        if (geo instanceof LineString) 
	        	len += getLineLength ((LineString) geo);
			else if (geo instanceof MultiLineString) 
				len += getMultiLineLength ((MultiLineString) geo);        		
			else if (geo instanceof MultiPolygon) 
				len += getMultiPolygonLength ((MultiPolygon) geo);        		
			else if (geo instanceof GeometryCollection) 
				len += getGeometryCollectionLength ((GeometryCollection) geo);        		
		}        
        return len;		
	}
	private static double getMultiPolygonLength (MultiPolygon multiPolygon) {
        double len = 0;
		for (int i=0; i<multiPolygon.getNumGeometries(); i++) {
			Geometry geo = multiPolygon.getGeometryN(i);
	        if (geo instanceof LineString) 
	        	len += getLineLength ((LineString) geo);
			else if (geo instanceof MultiLineString) 
				len += getMultiLineLength ((MultiLineString) geo);        		
			else if (geo instanceof MultiPolygon) 
				len += getMultiPolygonLength ((MultiPolygon) geo);        		
			else if (geo instanceof GeometryCollection) 
				len += getGeometryCollectionLength ((GeometryCollection) geo);        		
		}        
        return len;		
	}
	private static double getMultiLineLength (MultiLineString multiLine) {
        double len = 0;
		for (int i=0; i<multiLine.getNumGeometries(); i++) {
			Geometry geo = multiLine.getGeometryN(i);
	        if (geo instanceof LineString) 
	        	len += getLineLength ((LineString) geo);
			else if (geo instanceof MultiLineString) 
				len += getMultiLineLength ((MultiLineString) geo);        		
			else if (geo instanceof MultiPolygon) 
				len += getMultiPolygonLength ((MultiPolygon) geo);        		
			else if (geo instanceof GeometryCollection) 
				len += getGeometryCollectionLength ((GeometryCollection) geo);        		
		}        
        return len;		
	}
	private static double getLineLength (LineString line) {
        double len = 0;
        Coordinate[] coord = line.getCoordinates();
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
        return len;		
	}
	
	private static Geometry getGeometry (FunctionFactor function, Pipeline pipeline) {
		DocumentDefinition curDoc = pipeline.getCurrentDoc();
		GeometryValue geoValue = (GeometryValue) curDoc.getValue(GEOMETRY_FIELD_NAME);
		if (geoValue == null) 
			return null;

        Geometry geo = geoValue.getGeometry();
        return geo;
	}
	private static String getUnit (FunctionFactor function, Pipeline pipeline) {
		String unit = null;
		JCOValue u = ExpressionPredicateEvaluator.calculate(function.functionParams.get(0), pipeline);
		if (!JCOValue.isStringValue(u)) 
			return null;
		unit = u.getStringValue();
		if (!checkUnit(unit)) 
			return null;
		return unit;
	}
	private static boolean checkUnit (String unit) {
		boolean isM = "M".equalsIgnoreCase(unit);
		boolean isKM = "KM".equalsIgnoreCase(unit);
		boolean isML = "ML".equalsIgnoreCase(unit);
		return isM || isKM || isML;
	}

	// ---------------------------------------------------------
	
	
	private static JCOValue getGeometryAreaValue (FunctionFactor function, Pipeline pipeline) {
        Geometry geo = getGeometry(function, pipeline);
		if (geo == null) {
			JMH.addJCOMessage("No valid geometry to evaluate for function " + function.functionName);
			// empty value
			return new SimpleValue();
		}

        // By construction there's only one parameters
		String unit = getUnit(function, pipeline);
		if (unit == null) {
			JMH.addJCOMessage("Wrong Unit parameters:\t" + unit + " (use 'M', 'KM' or 'ML' in " + function.functionName + ")");
			// empty value
			return new SimpleValue();				
		} 

        double area = 0;
        if (geo instanceof Polygon) 
        	area = getPolygonArea ((Polygon) geo);
		else if (geo instanceof MultiPolygon) 
        	area = getMultiPolygonArea ((MultiPolygon) geo);        		
		else if (geo instanceof GeometryCollection) 
			area += getGeometryCollectionArea ((GeometryCollection) geo);        		
        
        if (unit.equals("ML"))
            area = area / M_2_MILE_SQUARE;
        else if (unit.equals("KM")) 
            area = area / KM_2_M_SQUARE;
        SimpleValue value = new SimpleValue(area);
        return value;				
	}

	private static double getGeometryCollectionArea (GeometryCollection multiPolygon) {
		double area = 0;
		for (int i=0; i<multiPolygon.getNumGeometries(); i++) {
			Geometry geo = multiPolygon.getGeometryN(i);
	        if (geo instanceof Polygon) 
	        	area += getPolygonArea ((Polygon) geo);
			else if (geo instanceof MultiPolygon) 
	        	area += getMultiPolygonArea ((MultiPolygon) geo);        		
			else if (geo instanceof GeometryCollection) 
				area += getGeometryCollectionArea ((GeometryCollection) geo);        		
		}
        return area;		
	}
	private static double getMultiPolygonArea (MultiPolygon multiPolygon) {
		double area = 0;
		for (int i=0; i<multiPolygon.getNumGeometries(); i++) {
			Geometry geo = multiPolygon.getGeometryN(i);
	        if (geo instanceof Polygon) 
	        	area += getPolygonArea ((Polygon) geo);
			else if (geo instanceof MultiPolygon) 
	        	area += getMultiPolygonArea ((MultiPolygon) geo);        		
			else if (geo instanceof GeometryCollection) 
				area += getGeometryCollectionArea ((GeometryCollection) geo);        		
		}
        return area;		
	}
	private static double getPolygonArea (Polygon polygon) {
		double area = 0;
        Coordinate[] coord = polygon.getCoordinates();
        List<Point> locations = new ArrayList<>();
        for (Coordinate coordinate : coord) 
            locations.add(GeometryFactory.createPointFromInternalCoord(coordinate, polygon.getCentroid()));

        // area in square meters
        area = calculateAreaOfPolygonOnSphereInSquareMeters(locations);
        return area;		
	}
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
			double x0 = listX.get(i - 1);
			double y0 = listY.get(i - 1);
			double x1 = listX.get(i);
			double y1 = listY.get(i);
			listArea.add(calculateAreaInSquareMeters(x0, x1, y0, y1));

		}

		// sum of triangle area
		double areasSum = 0;
		for (Double area : listArea) {
			areasSum += area;
		}

		if (areasSum < 0)
			areasSum = EARTH_SURFACE_M - areasSum;
		return areasSum;
	}
	private static Double calculateAreaInSquareMeters(double x0, double x1, double y0, double y1) {
		return (y0 * x1 - x0 * y1) / 2;
	}

	
	private static double calculateYSegment(double latitudeRef, double latitude, double circumference) {
		return (latitude - latitudeRef) * circumference / 360.0;
	}

	
	private static double calculateXSegment(double longitudeRef, double longitude, double latitude, double circumference) {
		return (longitude - longitudeRef) * circumference * Math.cos(Math.toRadians(latitude)) / 360.0;
	}
	
    
}
