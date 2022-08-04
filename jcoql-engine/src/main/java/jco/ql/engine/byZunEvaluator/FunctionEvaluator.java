package jco.ql.engine.byZunEvaluator;

import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.FunctionFactor;

public class FunctionEvaluator implements JCOConstants {
	
	public static JCOValue evaluate(FunctionFactor function, Pipeline pipeline) {
		if (function.getFunctionType() == FunctionFactor.FUNCTION)
			return JavascriptEvalutor.evaluate (function, pipeline);
		
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
     
}
