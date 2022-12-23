//FI created on 27.10.2022
package jco.ql.engine.byZunEvaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jco.ql.byZun.ZunWarningTracker;
import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyAggregatorCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.FuzzyAggregator;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.UsingAggregatorPredicate;
import jco.ql.parser.model.util.AggregateClause;
import jco.ql.parser.model.util.ForAllClause;
import jco.ql.parser.model.util.LocallyClause;
import jco.ql.parser.model.util.Parameter;


public class FuzzyAggregatorEvaluator {
	
	public static SimpleValue evaluate (UsingAggregatorPredicate usingAggregatorPredicate, Pipeline pipeline) {
		
		int ndx = alreadyExists(pipeline, usingAggregatorPredicate.fuzzyAggregatorName);
    	if (ndx == -1) {
    		JMH.addFuzzyMessage("Fuzzy Aggregator not found:\t" + usingAggregatorPredicate.fuzzyAggregatorName);
    		return new SimpleValue (); // null
    	}

    	FuzzyAggregatorCommand fa = pipeline.getFuzzyAggregators().get(ndx);
    	if (fa.getParameters().size()-1 != usingAggregatorPredicate.fuzzyAggregatorParameters.size()) {
    		JMH.addFuzzyMessage("Wrong number of parameters for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		return new SimpleValue (); // null
    	}
    	
    	// Prendo tutti i parametri attuali, a parte l'array che viene trattato separatamente
    	List<JCOValue> actualParameters = getActualParameters (usingAggregatorPredicate, pipeline);
    	if (!checkParameters (actualParameters, fa.getParameters())) {
    		JMH.addFuzzyMessage("Wrong type of parameters for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		return new SimpleValue (); // null    		
    	}
    	
    	String membershipArrayName = fa.getParameters().get(0).name;
    	DocumentDefinition fuzzyDoc = createFuzzyDoc (actualParameters, fa.getParameters());   	    	
    	
    	//Creo l'array contenente il valore delle membership da aggregare a partire dal documenti corrente
    	ArrayValue fuzzyArray = createArrayOfMembership(usingAggregatorPredicate, fa, pipeline);
    	
    	if(fuzzyArray == null) return new SimpleValue();
    	
    	//Aggiungo l'array al documento fuzzy
    	fuzzyDoc.addField(new FieldDefinition(membershipArrayName, fuzzyArray));   	
    	Pipeline fuzzyPipeline = new Pipeline(pipeline);
    	fuzzyPipeline.setCurrentDoc(fuzzyDoc);
    	
    	if (fa.getPreCondition() != null)
    		if (!ConditionEvaluator.matchCondition(fa.getPreCondition(), fuzzyPipeline)) {
	    		JMH.addFuzzyMessage("Precondition not matched for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
	    		return new SimpleValue (); // null    		
	    	}
    	
    	//Gestione For All
    	
    	for(ForAllClause forAllClause: fa.getForAll()){    		
    		if(fuzzyDoc.getField(forAllClause.idArray) == null || !(fuzzyDoc.getField(forAllClause.idArray).getValue() instanceof ArrayValue) ) {
    			JMH.addFuzzyMessage("Error on For All clause:\t array " + forAllClause.idArray + " isn't defined");
    			return new SimpleValue();
    		}
    		// set the pipeline for the FOR ALL clause
    		Pipeline forAllPipeline = new Pipeline(fuzzyPipeline);
    		DocumentDefinition forAllDocument = createFuzzyDoc(actualParameters, fa.getParameters());
    		
    		ArrayValue sortedArray = fuzzyArray;
    		//Evaluation of versus
    		if(fa.getVersus() == FuzzyAggregator.ASCENDING) {
    			List<SimpleValue> simpleValueList = new ArrayList<>();
    			List<JCOValue> jcoValueList = new ArrayList<JCOValue>(); //devo avere una lista di JCOValue perchè ArrayValue non accetta una lista di SimpleValue
    			for(int i = 0; i<fuzzyArray.getValues().size(); i++) 
    				simpleValueList.add((SimpleValue) fuzzyArray.getValues().get(i)); 			
    			Collections.sort(simpleValueList);  			
    			for(SimpleValue sv : simpleValueList)
    				jcoValueList.add(sv);
    			sortedArray = new ArrayValue(jcoValueList); 
    			
    		}
    		else if(fa.getVersus() == FuzzyAggregator.DESCENDING) {
    			List<SimpleValue> simpleValueList = new ArrayList<>();
    			List<JCOValue> jcoValueList = new ArrayList<JCOValue>(); //devo avere una lista di JCOValue perchè ArrayValue non accetta una lista di SimpleValue
    			for(int i = 0; i<fuzzyArray.getValues().size(); i++) 
    				simpleValueList.add((SimpleValue) fuzzyArray.getValues().get(i)); 			
    			Collections.sort(simpleValueList, Collections.reverseOrder());  			
    			for(SimpleValue sv : simpleValueList)
    				jcoValueList.add(sv);
    			sortedArray = new ArrayValue(jcoValueList);     			
    		}
    		forAllDocument.addField(new FieldDefinition(membershipArrayName, sortedArray));   	
    		forAllPipeline.setCurrentDoc(forAllDocument);
    		
    		ArrayValue forAllArray = (ArrayValue) forAllDocument.getValue(forAllClause.idArray);
    		
    		int size = forAllArray.getValues().size();
    		int firstIndex;
    		int lastIndex;
    		
    		
    		if(forAllClause.firstIndex != null && forAllClause.lastIndex != null) {
    			//Gli indici nel linguaggio partono da uno, mentre nel codice vengono salvati da 0 per agevolare i calcoli
    			firstIndex = fromExpressionToInt(forAllClause.lastIndex, fuzzyPipeline)-1;
    			lastIndex = fromExpressionToInt(forAllClause.firstIndex, fuzzyPipeline)-1;
    		}
    		else {
    			firstIndex = 0;
    			lastIndex = size-1;
    		}
    		
    		if(firstIndex >= lastIndex || size-1 < lastIndex || size-1 < firstIndex) {
    			JMH.addFuzzyMessage("Error on FOR All clause:\t wrong indexes for array " + forAllClause.idArray);
    			return new SimpleValue();
    		}
    		
    		
    		
    		
    		//Evaluation of locally
    		if(forAllClause.locally != null) {
    			for(LocallyClause locallyClause: forAllClause.locally) {
	    			ArrayValue locallyArray = new ArrayValue();
	    			forAllPipeline.setFuzzyAggregatorIndex(firstIndex);
	    			for (int i=firstIndex; i<=lastIndex; i++) {
	    				JCOValue value = ExpressionPredicateEvaluator.calculate(locallyClause.expression, forAllPipeline);				
	    				locallyArray.add(value);
	    				forAllPipeline.incFuzzyAggregatorIndex(1); //aggiungere nella tesi che il passo può essere diverso da 1
	    			}
	    			FieldDefinition field = new FieldDefinition(locallyClause.alias, locallyArray);
	    			forAllDocument.addField(field);
    			}    	    			
    		}
    		//Evaluation of aggregate
    		for(AggregateClause aggClause: forAllClause.aggregate) {
        		ArrayList<SimpleValue> aggValues = new ArrayList<SimpleValue>();
        		forAllPipeline.setFuzzyAggregatorIndex(firstIndex);
        		for(int i=firstIndex; i<=lastIndex; i++) {
        			JCOValue value = ExpressionPredicateEvaluator.calculate(aggClause.exp, forAllPipeline);
        			if(value.getType() != EValueType.INTEGER && value.getType() != EValueType.DECIMAL) {
        				JMH.addFuzzyMessage("Error on AGGREGATE clause in Fuzzy Aggregator \"" + fa.getFuzzyAggregatorName() + "\":\t impossible to aggregate non numeric values ");
            			return new SimpleValue();
        			}       				
        			aggValues.add((SimpleValue)value);
        			ZunWarningTracker.add("Valore espressione AGGREGATE: " + value.toString());
        			
        			forAllPipeline.incFuzzyAggregatorIndex(1); 
        		}
        		SimpleValue aggregationResult = calculateAggregation(aggClause, aggValues);
        		if(aggregationResult.getType().equals(EValueType.NULL)){
        			JMH.addFuzzyMessage("Aggregation impossible, maybe an invalid type of aggregation has been specified");
        			return new SimpleValue();
        		}
        		
        		FieldDefinition field = new FieldDefinition(aggClause.alias, aggregationResult);
        		forAllDocument.addField(field); 
        		fuzzyDoc.addField(field); //Aggiungo il risultato dell'aggregazione al documento fuzzy           		
        	}
    	}
    	JCOValue evaluateResult = ExpressionPredicateEvaluator.calculate(fa.getEvaluate(), fuzzyPipeline);
    	
    	
    	if(evaluateResult.getType() != EValueType.DECIMAL && evaluateResult.getType() != EValueType.INTEGER) {
    		JMH.addFuzzyMessage("EVALUATE expression returns wrong type value for Fuzzy Aggregator:\t" + fa.getFuzzyAggregatorName());
    		return new SimpleValue ();     	
    	}
    	else if(fa.getPolyline().isEmpty()) {
    		SimpleValue result = (SimpleValue) evaluateResult;
    		if(result.getNumericValue().compareTo(BigDecimal.ZERO) < 0 || result.getNumericValue().compareTo(BigDecimal.ONE) > 0) {
    			JMH.addFuzzyMessage("Fuzzy aggregator most return a value between 0 and 1, result produced: " + result.getNumericValue());
    			return new SimpleValue();
    		}

    		return (SimpleValue) evaluateResult;
    		
    	}
    	else {
    		return getMembership(evaluateResult, fa);
    	}
	}	
	
	
	// return the index of FA in the list. -1 if the FA is not (yet) existing
    private static int alreadyExists(Pipeline pipeline, String faName) {
        for(int i = 0; i < pipeline.getFuzzyAggregators().size(); i++) {
            if(pipeline.getFuzzyAggregators().get(i).getFuzzyAggregatorName().equals(faName)) {
                return i;
            }
        }
        return -1;
    }
    
    private static List<JCOValue> getActualParameters(UsingAggregatorPredicate usingAggregatorPredicate, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    		
    	for (Expression expr : usingAggregatorPredicate.fuzzyAggregatorParameters)
    		actualParameters.add(ExpressionPredicateEvaluator.calculate(expr, pipeline));
		return actualParameters;
	}
    
    private static int fromExpressionToInt(Expression e, Pipeline pipeline) {
    	return ((SimpleValue)ExpressionPredicateEvaluator.calculate(e, pipeline)).getNumericValue().intValue();
    }
    
    private static boolean checkParameters(List<JCOValue> actualParameters, List<Parameter> parameters) {
		// TODO - verifica parametri (per esempio che il primo sia un array)
		for (int i=0; i<actualParameters.size(); i++) {
			;
		}
		return true;
	}
    
 // by contruction actualParameters and parameters have the same number of element
 	private static DocumentDefinition createFuzzyDoc(List<JCOValue> actualParameters, List<Parameter> parameters) {
 		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
 		for (int i=0; i<actualParameters.size(); i++) {
 			FieldDefinition fd = new FieldDefinition(parameters.get(i).name, actualParameters.get(i));
 			fields.add(fd);
 		}
 		DocumentDefinition doc = new DocumentDefinition(fields);
 		return doc;
 	}
 	
 	//Metodo per creare l'array contenente i valori di membership da aggregare, partendo dal documento corrente e dal tipo di Using Aggregator specificato
 	private static ArrayValue createArrayOfMembership(UsingAggregatorPredicate usingAggregatorPredicate, FuzzyAggregatorCommand fa, Pipeline pipeline) {
 		ArrayValue returnArrayValue = null;
 		if(usingAggregatorPredicate.aggregatorType == UsingAggregatorPredicate.SELECTED_FUZZY_SET_FROM_ARRAY) {
 			System.out.println("QUAAAAAA");
    		if(usingAggregatorPredicate.arrayName == null) {
    			JMH.addFuzzyMessage("Error on array reference on aggregator:\t" + fa.getFuzzyAggregatorName());
    			return null;
    		}
    		ArrayValue array = pipeline.getCurrentDoc().getArrayValue(usingAggregatorPredicate.arrayName);
    		if(array == null){
    			JMH.addFuzzyMessage("Error on array reference on aggregator, array not defined in corrent document:\t" + usingAggregatorPredicate.arrayName);
    			return null;
    		}
    		
    		returnArrayValue = new ArrayValue();
    		for(JCOValue documentValue: array.getValues()) {
    			if(documentValue.getType() != EValueType.DOCUMENT)
    				JMH.addFuzzyMessage("Error on array of document \"" + usingAggregatorPredicate.arrayName + "\":\t impossible to iterate through array");
    			
    			DocumentDefinition document = (DocumentDefinition) documentValue.getValue();
    			DocumentDefinition fuzzyDocument = ((DocumentValue) document.getField(JCOConstants.FUZZYSETS_FIELD_NAME).getValue()).getDocument();
				for (int i = 0; i < usingAggregatorPredicate.fuzzySetsSelected.size(); i++) {
					boolean fuzzySetFound = false;
					for (FieldDefinition fuzzySet : fuzzyDocument.getFields()) {
						if (usingAggregatorPredicate.fuzzySetsSelected.get(i).equals(fuzzySet.getName())) {
							returnArrayValue.add(new SimpleValue(Double.parseDouble((String) fuzzySet.getValue().getValue())));
							fuzzySetFound = true;
							break;
						}
					}
					if (!fuzzySetFound) {
						JMH.addFuzzyMessage("Error on array of document \"" + usingAggregatorPredicate.arrayName
								+ "\":\t at least one of the element inside the array hasn't a"
								+ " membership value for the specified fuzzy set: \t"
								+ usingAggregatorPredicate.fuzzySetsSelected.get(i));
						return null;
					}

				}
    		}
    	}
 		else if(usingAggregatorPredicate.aggregatorType == UsingAggregatorPredicate.ALL_MEMBERSHIP_IN_DOCUMENT) {
    		DocumentValue fuzzySets = (DocumentValue) pipeline.getCurrentDoc().getValue(JCOConstants.FUZZYSETS_FIELD_NAME);
    		if(fuzzySets == null || fuzzySets.getFields().size() < 2){
    			JMH.addFuzzyMessage("Error on fuzzy set definition in current document: field \"" + JCOConstants.FUZZYSETS_FIELD_NAME + "\" not existing or has less than 2 fields\t" + fa.getFuzzyAggregatorName());
    			return null;
    		}
    		returnArrayValue = new ArrayValue();
    		for(FieldDefinition fuzzySet: fuzzySets.getFields()) {
    			if(fuzzySet.getValue().getValue() instanceof Double) returnArrayValue.add(new SimpleValue((Double) fuzzySet.getValue().getValue()));
    			else returnArrayValue.add(new SimpleValue(Double.parseDouble((String) fuzzySet.getValue().getValue())));
    		}
    	} 	
 		else if(usingAggregatorPredicate.aggregatorType == UsingAggregatorPredicate.SELECTED_FUZZY_SET_IN_DOCUMENT) {
    		DocumentValue fuzzySets = (DocumentValue) pipeline.getCurrentDoc().getValue(JCOConstants.FUZZYSETS_FIELD_NAME);
    		if(fuzzySets == null || fuzzySets.getFields().size() < 2){
    			JMH.addFuzzyMessage("Error on fuzzy set definition in current document: field \"" + JCOConstants.FUZZYSETS_FIELD_NAME + "\" not existing or has less than 2 fields\t" + fa.getFuzzyAggregatorName());
    			return null;
    		}
    		returnArrayValue = new ArrayValue();
    		for (int i = 0; i < usingAggregatorPredicate.fuzzySetsSelected.size(); i++) {
				boolean fuzzySetFound = false;
				for (FieldDefinition fuzzySet : fuzzySets.getFields()) {
					if (usingAggregatorPredicate.fuzzySetsSelected.get(i).equals(fuzzySet.getName())) {
						returnArrayValue.add(new SimpleValue(Double.parseDouble((String) fuzzySet.getValue().getValue())));
						fuzzySetFound = true;
						break;
					}
				}
				if (!fuzzySetFound) {
					JMH.addFuzzyMessage("Error on fuzzy set definition in current document"
							+ ":\t at least one of the element inside the array hasn't a"
							+ " membership value for the specified fuzzy set: \t"
							+ usingAggregatorPredicate.fuzzySetsSelected.get(i));
					return null;
				}

			}
    	}
 		
 		
		return returnArrayValue;		
 	}
 	
 	private static SimpleValue calculateAggregation(AggregateClause clause, ArrayList<SimpleValue> values) {
 		BigDecimal acc;
 		if(clause.withType == AggregateClause.PRODUCT || clause.withType == AggregateClause.DIVISION)
 			acc = new BigDecimal(1);
 		else if (clause.withType == AggregateClause.MINIMUM)
 			acc = BigDecimal.valueOf(Long.MAX_VALUE);
 		else if (clause.withType == AggregateClause.MAXIMUM)
 			acc = BigDecimal.valueOf(Long.MIN_VALUE);
 		else 
 			acc = new BigDecimal(0);
 		
 		for(SimpleValue value: values) {
 			if(clause.withType == AggregateClause.SUM)
 				acc = acc.add(value.getNumericValue());
 			else if(clause.withType == AggregateClause.SUBTRACTION)
 				acc = acc.subtract(value.getNumericValue());
 			else if(clause.withType == AggregateClause.PRODUCT)
 				acc = acc.multiply(value.getNumericValue());
 			else if(clause.withType == AggregateClause.DIVISION)
 				acc = acc.divide(value.getNumericValue());
 			else if(clause.withType == AggregateClause.MINIMUM)
 				acc = acc.min(value.getNumericValue());
 			else if(clause.withType == AggregateClause.MAXIMUM)
 				acc = acc.max(value.getNumericValue());
 			else
 				return new SimpleValue(); 			
 		} 		
 		return new SimpleValue(acc);
 	}
 	
	private static SimpleValue getMembership(JCOValue eval, FuzzyAggregatorCommand fa) {
		double x0, x1, y0, y1, membership;
		if (eval.getType() != EValueType.INTEGER && eval.getType() != EValueType.DECIMAL) {
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
