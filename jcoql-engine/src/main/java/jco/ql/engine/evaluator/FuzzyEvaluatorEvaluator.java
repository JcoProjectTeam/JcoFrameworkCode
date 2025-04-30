//FI created on 27.10.2022
package jco.ql.engine.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.UsingPredicate;
import jco.ql.parser.model.util.AggregateClause;
import jco.ql.parser.model.util.FESortArrayClause;
import jco.ql.parser.model.util.FEDeriveClause;
import jco.ql.parser.model.util.FEForAllClause;
import jco.ql.parser.model.util.FEInternalClause;
import jco.ql.parser.model.util.LocallyClause;
import jco.ql.parser.model.util.Parameter;
import jco.ql.parser.model.util.SortField;


public class FuzzyEvaluatorEvaluator implements JCOConstants {
	static final int EVAL_OK 	= 0;	
	static final int NOK_ARRAY 	= -1;	
	static final int NOK_DIM 	= -2;	
	static final int NOK_NUM 	= -1;	
	static final int NOK_RANGE 	= -2;	
	static final int NOK_RES 	= -3;	
	static final int NOK_DIV 	= -4;	

	
	public static SimpleValue evaluate (FuzzyEvaluatorCommand fec, UsingPredicate usingPredicate, Pipeline pipeline) {
    	if (fec.getParameters().size() != usingPredicate.fuzzyFunctionParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    		return new SimpleValue (); // null
    	}
    	
    	DocumentDefinition feDoc = getActualParameters (fec, usingPredicate, pipeline);
    	if (!checkParameters (feDoc, fec)) {
			JMH.addFuzzyMessage("Wrong type of parameters sent to " + fec.getFuzzyEvaluatorType() + ": " + fec.getFuzzyFunctionName());
    		return new SimpleValue ();     		
    	}

    	Pipeline fePipeline = new Pipeline(pipeline);
    	fePipeline.setCurrentDoc(feDoc);
    	if (fec.getPreCondition() != null)
    		if (!ConditionEvaluator.matchCondition(fec.getPreCondition(), fePipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
	    		return new SimpleValue (); // null    		
	    	}

    	for (FEInternalClause fic: fec.feInternalClauseList) 
    		if (fic.isSortClause()) { 
    			FESortArrayClause sac = (FESortArrayClause)fic;
        		int res = performSorting (sac, feDoc);				// feDoc is likely to be modified
        		if (res == NOK_ARRAY) 
        			JMH.addFuzzyMessage("Impossible to sort non-array field in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
        		else if (res == NOK_DIM) 
        			JMH.addFuzzyMessage("Impossible to sort togheter array fields of different dimension in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    			
    		}
    		else if (fic.isDeriveClause()) { 
    			FEDeriveClause dc = (FEDeriveClause) fic;
    			JCOValue jv = ExpressionPredicateEvaluator.calculate(dc.expression, fePipeline);
    			if (dc.isDeriveScalar() && !JCOValue.isNumericValue(jv))
        			JMH.addFuzzyMessage("Non numerical result in DERIVE clause in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    			FieldDefinition fd = new FieldDefinition(dc.alias, jv);
    			feDoc.addField(fd);
    		}
    		else {
    			FEForAllClause fac = (FEForAllClause) fic;
    			int res = evaluateForAll(fac, fePipeline);			
    			if (res == NOK_NUM)
        			JMH.addFuzzyMessage("Non numerical range in FOR ALL clause in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    			else if (res == NOK_RANGE)
        			JMH.addFuzzyMessage("Range error in FOR ALL clause in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    			else if (res == NOK_RES)
        			JMH.addFuzzyMessage("Non numerical result in FOR ALL clause in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    			else if (res == NOK_DIV)
        			JMH.addFuzzyMessage("Division by 0 in FOR ALL clause in " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    		}
    			
    	JCOValue eval = ExpressionPredicateEvaluator.calculate(fec.getEvaluate(), fePipeline);
    	SimpleValue membership = getMembership (eval, fec);
    	return membership;
	}	
	

	// PF 2023.08.09
	private static DocumentDefinition getActualParameters(FuzzyEvaluatorCommand fec, UsingPredicate usingEvaluatorPredicate, Pipeline pipeline) {
		DocumentDefinition d = new DocumentDefinition ();
		
		for (int i=0; i<usingEvaluatorPredicate.fuzzyFunctionParameters.size(); i++) {
			Expression expr = usingEvaluatorPredicate.fuzzyFunctionParameters.get(i);
			JCOValue jv = ExpressionPredicateEvaluator.calculate(expr, pipeline);
			FieldDefinition fd = new FieldDefinition(fec.getParameters().get(i).name, jv);
			d.addField(fd);
		}	
		return d;
	}


	// PF 2023.08.09
	private static boolean checkParameters(DocumentDefinition parameterDoc, FuzzyEvaluatorCommand fec) {
		for (Parameter p: fec.getParameters()) {
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
	private static int performSorting(FESortArrayClause sac, DocumentDefinition feDoc) {
		// check that arrays to order are real array - by construction this should be useless
		for (String arrayName: sac.sourceArrayList) {
			JCOValue jv = feDoc.getValue(arrayName);
			if (!JCOValue.isArrayValue(jv)) 
				return NOK_ARRAY;
		}
		
		int aSize = ((ArrayValue) feDoc.getValue(sac.sourceArrayList.get(0))).getValues().size();
		// in case of more arrays to order, check if they have the same size
		for (String arrayName: sac.sourceArrayList) {
			ArrayValue av = (ArrayValue) feDoc.getValue(arrayName);
			int as = av.getValues().size();
			if (aSize != as)
				return NOK_DIM;
		}

		// creating new sorted (empty-now) array fields
		for (String newArrayName: sac.targetArrayList) {
			ArrayValue av = new ArrayValue();
			FieldDefinition fd = new FieldDefinition(newArrayName, av);
			feDoc.addField(fd);
		}

		// insert source values one-by-one into target arrays in the right position
		for (int i=0; i<aSize; i++) {
			// retrieve the position to insert the value
    		int j = getInsertIndex(i, feDoc, sac);
    		// insert the values in all target arrays involved
    		for (int k=0; k<sac.sourceArrayList.size(); k++) {
    			ArrayValue sourceArray = (ArrayValue) feDoc.getValue(sac.sourceArrayList.get(k));
    			ArrayValue targetArray = (ArrayValue) feDoc.getValue(sac.targetArrayList.get(k));
    			JCOValue jv = sourceArray.getValues().get(i);
    			targetArray.getValues().add(j, jv);
    		}   		
    	}
		
		return EVAL_OK; // all ok;
	}

	private static int getInsertIndex (int ndx, DocumentDefinition feDoc, FESortArrayClause sac) {
		
		for (int j=0; j<ndx; j++) {
			boolean next = true;
			int k=0;
			while (next && k<sac.sortingFieldList.size()) {
				SortField sf = sac.sortingFieldList.get(k);

				String source = sf.sourceArray;
				ArrayValue sav = (ArrayValue)feDoc.getValue(source); 
				JCOValue valueToInsert = sav.getValues().get(ndx);
				String target = sf.targetArray;
				ArrayValue tav = (ArrayValue)feDoc.getValue(target); 
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
	private static int evaluateForAll(FEForAllClause fac, Pipeline fePipeline) {
		DocumentDefinition feDoc = fePipeline.getCurrentDoc();
		ArrayValue sourceArray = (ArrayValue) feDoc.getValue(fac.idArray);		
		int size = sourceArray.getValues().size();
		int firstIndex;
		int lastIndex;
				
		if(fac.firstIndex == null || fac.lastIndex == null) {
			firstIndex = 0;
			lastIndex = size;
		}
		else {
			JCOValue fi = ExpressionPredicateEvaluator.calculate(fac.firstIndex, fePipeline);
			JCOValue li = ExpressionPredicateEvaluator.calculate(fac.lastIndex, fePipeline);
			if (JCOValue.isIntValue(fi) && JCOValue.isIntValue(li)) {
				firstIndex = ((SimpleValue)fi).getNumericValue().intValue()-1;
				lastIndex  = ((SimpleValue)li).getNumericValue().intValue();
			}
			else
				return NOK_NUM;
		}
		
		if (firstIndex >= lastIndex || firstIndex < 0 || lastIndex > size) 
			return NOK_RANGE;

		for (int i=firstIndex; i<lastIndex; i++) {
			FieldDefinition pos = new FieldDefinition ("POS", new SimpleValue(i+1));
			feDoc.addField(pos);
			JCOValue x = sourceArray.getValues().get(i);
			FieldDefinition var = new FieldDefinition(fac.var, x);
			feDoc.addField(var);
			for (LocallyClause lc: fac.locally) {
				JCOValue lv = ExpressionPredicateEvaluator.calculate(lc.expression, fePipeline);
				FieldDefinition ld = new FieldDefinition(lc.alias, lv);
				feDoc.addField(ld);
				if (!JCOValue.isNumericValue(lv))
					return NOK_RES;
			}
			for (AggregateClause ac: fac.aggregate) {
				SimpleValue sv = new SimpleValue();
				JCOValue av = ExpressionPredicateEvaluator.calculate(ac.exp, fePipeline);
				if (!JCOValue.isNumericValue(av))
					return NOK_RES;
				else
					sv = (SimpleValue)av;

				if (i==firstIndex) {
					FieldDefinition ad = new FieldDefinition(ac.alias, sv);
					feDoc.addField(ad);
				} 
				else {
					SimpleValue partial = (SimpleValue)feDoc.getValue(ac.alias);	// by construction alias field must be a number
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
					feDoc.addField(ad);					
					if (!JCOValue.isNumericValue(partial))
						return NOK_RES;
				}
			}
		}
		return EVAL_OK;
	}



 	
	private static SimpleValue getMembership(JCOValue eval, FuzzyEvaluatorCommand fec) {
		double x0, x1, y0, y1, membership;
		if (!JCOValue.isNumericValue(eval)) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for " + fec.getFuzzyEvaluatorType() + ":\t" + fec.getFuzzyFunctionName());
    		return new SimpleValue (); // null    					
		}
		double ev = 0;
		if (eval.getType() == EValueType.INTEGER) 
			ev = (Long) eval.getValue();
		else 
			ev = (Double) eval.getValue();
		
		if (ev < fec.getPolyline().polyline.get(0).getXvalue())
			return new SimpleValue (fec.getPolyline().polyline.get(0).getYvalue());
		for (int i=1; i<fec.getPolyline().getSize(); i++ )
			if (ev < fec.getPolyline().polyline.get(i).getXvalue()) {
				x0 = fec.getPolyline().polyline.get(i-1).getXvalue();
				y0 = fec.getPolyline().polyline.get(i-1).getYvalue();
				x1 = fec.getPolyline().polyline.get(i).getXvalue();
				y1 = fec.getPolyline().polyline.get(i).getYvalue();
				membership = ((y1-y0) / (x1-x0)) * (ev-x0) + y0; 
				return new SimpleValue (membership);			
			}
		
		return new SimpleValue (fec.getPolyline().polyline.get(fec.getPolyline().getSize()-1).getYvalue());
	}

 	
 	// GB metodo per valutatore generico
	public static List<FieldDefinition> evaluateGeneric(FuzzyEvaluatorCommand gfe, UsingPredicate usingPredicate, Pipeline pipeline, String fuzzySetModel) {
		List<JCOValue> actualParameters = new ArrayList<>();
		DocumentDefinition fuzzyDoc;
		List<FieldDefinition> degrees = new ArrayList<>();
		List<JCOValue> tempList = new ArrayList<>();
		List<FieldDefinition> derivedDegrees = new ArrayList<>();

		
		// controllo che il valutaore sia sullo stesso fuzzy set
		if (!gfe.getFuzzySetModelName().equals(fuzzySetModel)) {
    		JMH.addFuzzyMessage("Wrong fuzzyset model: [" + fuzzySetModel + "] for fuzzy evaluator: [" + usingPredicate.fuzzyFunction + "]");
    		return null;		
    	}
		
		FuzzySetModelCommand fsmc = pipeline.getFuzzySetModel(fuzzySetModel);
		
		// controllo che coincida il numero di parametri
		if (gfe.getParameters().size() != usingPredicate.fuzzyFunctionParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Evaluator:\t" + gfe.getFuzzyFunctionName());
    		return null;
		}
		
		// controllo che coincida il tipo dei parametri
		actualParameters = getActualParameters (usingPredicate, pipeline);
    	if (!checkParameters (actualParameters, gfe.getParameters())) {
    		JMH.addFuzzyMessage("Wrong type of parameters for Fuzzy Evaluator:\t" + gfe.getFuzzyFunctionName());
    		return null;    		
    	}
    	
    	// passo alla verifica delle pre condition
    	fuzzyDoc = createFuzzyDoc (actualParameters, gfe.getParameters());
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	if (gfe.getPreCondition() != null)
    		if (!ConditionEvaluator.matchCondition(gfe.getPreCondition(), fuzzyPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Evaluator:\t" + gfe.getFuzzyFunctionName());
	    		return null; // null    		
    		}
    	
    	// Clausole interne
    	for (FEInternalClause fic: gfe.feInternalClauseList) 
    		if (fic.isSortClause()) { 
    			FESortArrayClause sac = (FESortArrayClause)fic;
        		int res = performSorting (sac, fuzzyDoc);				
        		if (res == NOK_ARRAY) 
        			JMH.addFuzzyMessage("Impossible to sort non-array field in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
        		else if (res == NOK_DIM) 
        			JMH.addFuzzyMessage("Impossible to sort togheter array fields of different dimension in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    			
    		}
    		else if (fic.isDeriveClause()) { 
    			FEDeriveClause dc = (FEDeriveClause) fic;
    			JCOValue jv = ExpressionPredicateEvaluator.calculate(dc.expression, fuzzyPipeline);
    			if (dc.isDeriveScalar() && !JCOValue.isNumericValue(jv))
        			JMH.addFuzzyMessage("Non numerical result in DERIVE clause in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    			FieldDefinition fd = new FieldDefinition(dc.alias, jv);
    			fuzzyDoc.addField(fd);
    		}
    		else {
    			FEForAllClause fac = (FEForAllClause) fic;
    			int res = evaluateForAll(fac, fuzzyPipeline);			
    			if (res == NOK_NUM)
        			JMH.addFuzzyMessage("Non numerical range in FOR ALL clause in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    			else if (res == NOK_RANGE)
        			JMH.addFuzzyMessage("Range error in FOR ALL clause in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    			else if (res == NOK_RES)
        			JMH.addFuzzyMessage("Non numerical result in FOR ALL clause in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    			else if (res == NOK_DIV)
        			JMH.addFuzzyMessage("Division by 0 in FOR ALL clause in " + gfe.getFuzzyEvaluatorType() + ":\t" + gfe.getFuzzyFunctionName());
    		}
    	
    	// calcolo delle evaluates e memorizzo nella temp
    	for (int i = 0; i < gfe.getEvaluates().size(); i++) 
			tempList.add(ExpressionPredicateEvaluator.calculate(gfe.getEvaluates().get(i), fuzzyPipeline));
		
    	// calcolo dei gradi di membership e poi di quelli derivati
    	for (int i = 0; i < fsmc.getDegrees().size(); i++) 
    		degrees.add(new FieldDefinition(fsmc.getDegrees().get(i).name, getDegree(tempList.get(i) , gfe, i)));
    	derivedDegrees = derivedDegreesEvaluate(degrees, fsmc.getDerivedDegrees(), fsmc.getDerivedExpr(), pipeline);
    	
    	// controllo che i gradi rispettino i vincoli definiti dal modello
    	if (!checkConstraint(degrees, derivedDegrees, pipeline, fsmc.getConstraint())) {
			JMH.add("Constraint is not respected for " + gfe.getFuzzySetModelName());
			return null;
		}
    	
    	degrees.addAll(derivedDegrees);
		
		return degrees;
	}
	
	// GB
	private static List<JCOValue> getActualParameters(UsingPredicate usingPredicate, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    	for (Expression expr : usingPredicate.fuzzyFunctionParameters)
    		actualParameters.add(ExpressionPredicateEvaluator.calculate(expr, pipeline));
		return actualParameters;
	}
 	
	// GB implementare con i metodi della JCOValue, se non matchano return false e messaggio in JMH.addFuzzyMessage(...)
	private static boolean checkParameters(List<JCOValue> actualParameters, List<Parameter> parameters) {
		boolean pres = false;
		JCOValue actual;
		Parameter param;
		for (int i = 0; i < actualParameters.size(); i++) {
			actual = actualParameters.get(i);
			for(int j = 0; j < parameters.size(); j++) {
				param = parameters.get(j);
				if(JCOValue.isNumericValue(actual) && param.isNumeric())
					pres = true;
				if(JCOValue.isArrayValue(actual) && param.isArray())
					pres = true;
				if(JCOValue.isBooleanValue(actual) && param.isBoolean())
					pres = true;
				if(JCOValue.isDocumentValue(actual) && param.isDocument())
					pres = true;
				if(JCOValue.isStringValue(actual) && param.isString())
					pres = true;
			}
			if (!pres)
				return false;
			pres = false;
		}
		return true;
	}
	
	// GB
	private static DocumentDefinition createFuzzyDoc(List<JCOValue> actualParameters, List<Parameter> parameters) {
		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
		for (int i=0; i<actualParameters.size(); i++) {
			FieldDefinition fd = new FieldDefinition(parameters.get(i).name, actualParameters.get(i));
			fields.add(fd);
		}
		
		DocumentDefinition doc = new DocumentDefinition(fields);
		return doc;
	}
 	
	// GB si potrebbe riutilizzare getMembership per ogni polyline
	private static SimpleValue getDegree(JCOValue eval, FuzzyEvaluatorCommand gfe, int pos) {
		double x0, x1, y0, y1, membership;
		if (eval.getType() != EValueType.INTEGER && eval.getType() != EValueType.DECIMAL) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for Fuzzy Evaluator:\t" + gfe.getFuzzyFunctionName());
    		return new SimpleValue ();    					
		}
		
		double ev = 0;
		
		if (JCOValue.isNumericValue(eval)) {
			String s = eval.getStringValue();
			ev = Double.parseDouble(s);
		}
		
		List<FuzzyPoint> p = gfe.getPolylines().get(pos).polyline;
		if (ev < p.get(0).getXvalue())
			return new SimpleValue (p.get(0).getYvalue());
		
		for (int i=1; i<p.size(); i++ )
			if (ev < p.get(i).getXvalue()) {
				x0 = p.get(i-1).getXvalue();
				y0 = p.get(i-1).getYvalue();
				x1 = p.get(i).getXvalue();
				y1 = p.get(i).getYvalue();	
				membership = ((y1-y0) / (x1-x0)) * (ev-x0) + y0; 
				return new SimpleValue (membership);			
			}
		
		return new SimpleValue (p.get(p.size()-1).getYvalue());
	}
	
	// GB
	private static List<FieldDefinition> derivedDegreesEvaluate(List<FieldDefinition> degrees, List<String>derivedName, List<Expression>derivedExpr, Pipeline pipeline) {
		List<FieldDefinition> value = new ArrayList<>();
		JCOValue j;
		double ev = 0.0;
		DocumentDefinition fuzzyDoc = new DocumentDefinition (degrees);
		Pipeline fuzzyPipeline = new Pipeline(pipeline);
		fuzzyPipeline.setCurrentDoc(fuzzyDoc);
		
		for (int i = 0; i < derivedExpr.size(); i++) {
			j = ExpressionPredicateEvaluator.calculate(derivedExpr.get(i), fuzzyPipeline);
			if (JCOValue.isNumericValue(j)) {
				String s = j.getStringValue();
				ev = Double.parseDouble(s);
				if (ev<0) 
					ev=0;
				else if (ev>1) 
					ev=1;
			}
			value.add( new FieldDefinition(derivedName.get(i), new SimpleValue(ev) ));
		}
		
		return value;
	}
	
	// GB
	private static boolean checkConstraint(List<FieldDefinition> degree, List<FieldDefinition> derivedDegree, Pipeline pipeline, Condition condition) {
		List<FieldDefinition> d = new ArrayList<>();
		d.addAll(degree);
		d.addAll(derivedDegree);
		DocumentDefinition fuzzyDoc = new DocumentDefinition(d);
		Pipeline fuzzyPipeline = new Pipeline(pipeline);
		fuzzyPipeline.setCurrentDoc(fuzzyDoc);
		if (condition != null)
			if (!ConditionEvaluator.matchCondition((Condition) condition, fuzzyPipeline)) 
				return false; 		
		return true;
	}
  	
}
