//FI created on 27.10.2022
package jco.ql.engine.evaluator;

import java.math.BigDecimal;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyAggregatorCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.UsingPredicate;
import jco.ql.parser.model.util.AggregateClause;
import jco.ql.parser.model.util.DeriveClause;
import jco.ql.parser.model.util.ForAllClause;
import jco.ql.parser.model.util.ForAllDeriveElement;
import jco.ql.parser.model.util.LocallyClause;
import jco.ql.parser.model.util.Parameter;
import jco.ql.parser.model.util.SortField;
import jco.ql.parser.model.util.SortFuzzyAggregatorElement;


public class FuzzyAggregatorEvaluator implements JCOConstants {
	
	public static SimpleValue evaluate (FuzzyAggregatorCommand fa, UsingPredicate usingPredicate, Pipeline pipeline) {
    	if (fa.getParameters().size() != usingPredicate.fuzzyFunctionParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		return new SimpleValue (); // null
    	}
    	
    	DocumentDefinition faDoc = getActualParameters (fa, usingPredicate, pipeline);
    	if (!checkParameters (faDoc, fa)) {
			JMH.addFuzzyMessage("Wrong type of parameters sent to Fuzzy Aggregator: " + fa.getFuzzyAggregatorName());
    		return new SimpleValue ();     		
    	}

    	Pipeline faPipeline = new Pipeline(pipeline);
    	faPipeline.setCurrentDoc(faDoc);
    	
    	if (fa.getPreCondition() != null)
    		if (!ConditionEvaluator.matchCondition(fa.getPreCondition(), faPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
	    		return new SimpleValue (); // null    		
	    	}

    	for (SortFuzzyAggregatorElement sfae: fa.sortList) {
    		int res = performSorting (sfae, faDoc);				// faDoc is likely to be modified
    		if (res == -1) 
    			JMH.addFuzzyMessage("Impossible to sort non-array field in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		else if (res == -2) 
    			JMH.addFuzzyMessage("Impossible to sort togheter array fields of different dimension in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    	}
    	    	
    	for (ForAllDeriveElement fade: fa.forAllDeriveList) 
    		if (fade.isDeriveClause()) {
    			DeriveClause dc = (DeriveClause) fade;
    			JCOValue jv = ExpressionPredicateEvaluator.calculate(dc.expression, faPipeline);
    			if (!JCOValue.isNumericValue(jv))
        			JMH.addFuzzyMessage("Non numerical result in DERIVE clause in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    			FieldDefinition fd = new FieldDefinition(dc.alias, jv);
    			faDoc.addField(fd);
    		}
    		else {
    			ForAllClause fac = (ForAllClause) fade;
    			int res = evaluateForAll(fac, faPipeline);			
    			if (res == -1)
        			JMH.addFuzzyMessage("Non numerical range in FOR ALL clause in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    			else if (res == -2)
        			JMH.addFuzzyMessage("Range error in FOR ALL clause in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    			else if (res == -3)
        			JMH.addFuzzyMessage("Non numerical result in FOR ALL clause in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    			else if (res == -4)
        			JMH.addFuzzyMessage("Division by 0 in FOR ALL clause in Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		}
    			
    	JCOValue eval = ExpressionPredicateEvaluator.calculate(fa.getEvaluate(), faPipeline);
    	SimpleValue membership = getMembership (eval, fa);
    	return membership;
	}	
	

	// PF 2023.08.09
	private static DocumentDefinition getActualParameters(FuzzyAggregatorCommand fa, UsingPredicate usingAggregatorPredicate, Pipeline pipeline) {
		DocumentDefinition d = new DocumentDefinition ();
		
		// other Fuzzy Aggregator parameters
		for (int i=0; i<usingAggregatorPredicate.fuzzyFunctionParameters.size(); i++) {
			Expression expr = usingAggregatorPredicate.fuzzyFunctionParameters.get(i);
			JCOValue jv = ExpressionPredicateEvaluator.calculate(expr, pipeline);
			FieldDefinition fd = new FieldDefinition(fa.getParameters().get(i+1).name, jv);
			d.addField(fd);
		}	
		return d;
	}


	// PF 2023.08.09
	private static boolean checkParameters(DocumentDefinition parameterDoc, FuzzyAggregatorCommand fa) {
		for (Parameter p: fa.getParameters()) {
			JCOValue jv = parameterDoc.getValue(p.name);
			if (!checkParameter(p, jv)) 
				return false;
		}
		return true;
	}
	// PF 2023.08.09
	private static boolean checkParameter(Parameter p, JCOValue jv) {
		if (p.isArray() && JCOValue.isArrayValue(jv))
			return true;
		if (p.isBoolean() && JCOValue.isBooleanValue(jv))
			return true;
		if (p.isDocument() && JCOValue.isDocumentValue(jv))
			return true;
		if (p.isNumeric() && JCOValue.isNumericValue(jv))
			return true;
		if (p.isString() && JCOValue.isStringValue(jv))
			return true;
		return false;
	}


	// PF 2023.08.09
	private static int performSorting(SortFuzzyAggregatorElement sfae, DocumentDefinition faDoc) {
		// check that arrays to order are real array - by construction this should be useless
		for (String arrayName: sfae.sourceArrayList) {
			JCOValue jv = faDoc.getValue(arrayName);
			if (!JCOValue.isArrayValue(jv)) 
				return -1;
		}
		
		int aSize = ((ArrayValue) faDoc.getValue(sfae.sourceArrayList.get(0))).getValues().size();
		// in case of more arrays to order, check if they have the same size
		for (String arrayName: sfae.sourceArrayList) {
			ArrayValue av = (ArrayValue) faDoc.getValue(arrayName);
			int as = av.getValues().size();
			if (aSize != as)
				return -2;
		}

		// creating new sorted (empty-now) array fields
		for (String newArrayName: sfae.targetArrayList) {
			ArrayValue av = new ArrayValue();
			FieldDefinition fd = new FieldDefinition(newArrayName, av);
			faDoc.addField(fd);
		}

		// insert source values one-by-one into target arrays in the right position
		for (int i=0; i<aSize; i++) {
			// retrieve the position to insert the value
    		int j = getInsertIndex(i, faDoc, sfae);
    		// insert the values in all target arrays involved
    		for (int k=0; k<sfae.sourceArrayList.size(); k++) {
    			ArrayValue sourceArray = (ArrayValue) faDoc.getValue(sfae.sourceArrayList.get(k));
    			ArrayValue targetArray = (ArrayValue) faDoc.getValue(sfae.targetArrayList.get(k));
    			JCOValue jv = sourceArray.getValues().get(i);
    			targetArray.getValues().add(j, jv);
    		}   		
    	}
		
		return 0; // all ok;
	}

	private static int getInsertIndex (int ndx, DocumentDefinition faDoc, SortFuzzyAggregatorElement sfae) {
		
		for (int j=0; j<ndx-1; j++) {
			boolean next = true;
			int k=0;
			while (next && k<sfae.sortingFieldList.size()) {
				SortField sf = sfae.sortingFieldList.get(k);

				String source = sf.sourceArray;
				ArrayValue sav = (ArrayValue)faDoc.getValue(source); 
				JCOValue valueToInsert = sav.getValues().get(ndx);
				String target = sf.targetArray;
				ArrayValue tav = (ArrayValue)faDoc.getValue(target); 
				JCOValue valueToCompare = tav.getValues().get(j);
				
				int cmp = compare (sf, valueToInsert, valueToCompare);
				if (cmp == INSERT_BEFORE)
					return j;
				else if (cmp == INSERT_AFTER)
					next = false;
				k++;
			}
		}
		return ndx;		
	}
	
	private static int compare(SortField sf, JCOValue valueToInsert, JCOValue valueToCompare) {
		if (!(	(sf.fieldType == SortField.NUMERIC 	&& JCOValue.isNumericValue(valueToInsert))	||
				(sf.fieldType == SortField.STRING 	&& JCOValue.isStringValue(valueToInsert))	||	
				(sf.fieldType == SortField.BOOLEAN 	&& JCOValue.isBooleanValue(valueToInsert))	)	)
			return INSERT_AFTER;
		if (!(	(sf.fieldType == SortField.NUMERIC 	&& JCOValue.isNumericValue(valueToCompare))	||
				(sf.fieldType == SortField.STRING 	&& JCOValue.isStringValue(valueToCompare))	||	
				(sf.fieldType == SortField.BOOLEAN 	&& JCOValue.isBooleanValue(valueToCompare))	)	)
			return INSERT_BEFORE;
		
		SimpleValue v1 = (SimpleValue) valueToInsert;
		SimpleValue v2 = (SimpleValue) valueToCompare;
		if (v2.compareTo(v1) == GREATER_THAN)
			if (sf.versus == SortField.ASCENDING)
				return INSERT_BEFORE;
			else
				return INSERT_AFTER;

		else if (v2.compareTo(v1) == LESS_THAN)
			if (sf.versus == SortField.ASCENDING)
				return INSERT_AFTER;
			else
				return INSERT_BEFORE;
		return EQUAL;
	}


	// PF added on 2023.08.09
	private static int evaluateForAll(ForAllClause fac, Pipeline faPipeline) {
		DocumentDefinition faDoc = faPipeline.getCurrentDoc();
		ArrayValue sourceArray = (ArrayValue) faDoc.getValue(fac.idArray);		
		int size = sourceArray.getValues().size();
		int firstIndex;
		int lastIndex;
				
		if(fac.firstIndex == null || fac.lastIndex == null) {
			firstIndex = 0;
			lastIndex = size;
		}
		else {
			JCOValue fi = ExpressionPredicateEvaluator.calculate(fac.firstIndex, faPipeline);
			JCOValue li = ExpressionPredicateEvaluator.calculate(fac.lastIndex, faPipeline);
			if (JCOValue.isIntValue(fi) && JCOValue.isIntValue(li)) {
				firstIndex = ((SimpleValue)fi).getNumericValue().intValue()-1;
				lastIndex  = ((SimpleValue)li).getNumericValue().intValue();
			}
			else
				return -1;
		}
		
		if (firstIndex >= lastIndex || firstIndex < 0 || lastIndex > size) 
			return -2;

		for (int i=firstIndex; i<lastIndex; i++) {
			FieldDefinition pos = new FieldDefinition ("POS", new SimpleValue(i+1));
			faDoc.addField(pos);
			JCOValue x = sourceArray.getValues().get(i);
			FieldDefinition var = new FieldDefinition(fac.var, x);
			faDoc.addField(var);
			for (LocallyClause lc: fac.locally) {
				JCOValue lv = ExpressionPredicateEvaluator.calculate(lc.expression, faPipeline);
				FieldDefinition ld = new FieldDefinition(lc.alias, lv);
				faDoc.addField(ld);
				if (!JCOValue.isNumericValue(lv))
					return -3;
			}
			for (AggregateClause ac: fac.aggregate) {
				SimpleValue sv = new SimpleValue();
				JCOValue av = ExpressionPredicateEvaluator.calculate(ac.exp, faPipeline);
				if (!JCOValue.isNumericValue(av))
					return -3;
				else
					sv = (SimpleValue)av;

				if (i==firstIndex) {
					FieldDefinition ad = new FieldDefinition(ac.alias, sv);
					faDoc.addField(ad);
				} 
				else {
					SimpleValue partial = (SimpleValue)faDoc.getValue(ac.alias);	// by construction alias field must be a number
					BigDecimal acc = partial.getNumericValue();
					BigDecimal val = sv.getNumericValue();
					if (ac.withType == AggregateClause.MAXIMUM)
						acc = acc.max(val);
					else if (ac.withType == AggregateClause.MINIMUM)
						acc = acc.min(val);
					else if (ac.withType == AggregateClause.PRODUCT)
						acc = acc.multiply(val);
					else if (ac.withType == AggregateClause.SUM)
						acc = acc.add(val);
					else 
						acc = acc.add(val);
					
					partial = new SimpleValue(acc);
					FieldDefinition ad = new FieldDefinition(ac.alias, partial);
					faDoc.addField(ad);					
					if (!JCOValue.isNumericValue(partial))
						return -3;
				}
			}
		}
		return 0;
	}



 	
	private static SimpleValue getMembership(JCOValue eval, FuzzyAggregatorCommand fa) {
		double x0, x1, y0, y1, membership;
		if (!JCOValue.isNumericValue(eval)) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		return new SimpleValue (); // null    					
		}
		double ev = 0;
		if (eval.getType() == EValueType.INTEGER) 
			ev = (Integer) eval.getValue();
		else 
			ev = (Double) eval.getValue();
		
		if (ev < fa.getPolyline().get(0).getX())
			return new SimpleValue (fa.getPolyline().get(0).getY().doubleValue());
		for (int i=1; i<fa.getPolyline().size(); i++ )
			if (ev < fa.getPolyline().get(i).getX()) {
				x0 = fa.getPolyline().get(i-1).getX();
				y0 = fa.getPolyline().get(i-1).getY();
				x1 = fa.getPolyline().get(i).getX();
				y1 = fa.getPolyline().get(i).getY();
				membership = ((y1-y0) / (x1-x0)) * (ev-x0) + y0; 
				return new SimpleValue (membership);			
			}
		
		return new SimpleValue (fa.getPolyline().get(fa.getPolyline().size()-1).getY().doubleValue());
	}

 	
 	
 	
}
