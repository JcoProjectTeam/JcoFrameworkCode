package jco.ql.engine.evaluator;



import jco.ql.engine.Pipeline;
import java.math.BigDecimal;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ArrayReference;


public class ArrayReferenceEvaluator {
	
	public static JCOValue evaluate(ArrayReference ref, Pipeline pipeline) {
		ArrayValue array = pipeline.getCurrentDoc().getArrayValue(ref.idArray);
		if(array == null) {
			JMH.add("Array not declared: \t" + ref.idArray);
			return new SimpleValue();
		}
		

		//controllo che l'indice sia accettabile
		BigDecimal index =  ((SimpleValue)ExpressionPredicateEvaluator.calculate(ref.index, pipeline)).getNumericValue().subtract(new BigDecimal(1));

		if(index == null){
			JMH.add("Wrong expression in index: \t" + ref.index.toString());
			return new SimpleValue();
		}
		if(index.intValue() <0 || index.intValue() >= array.getValues().size())
		{
			JMH.add("Index out of range in array: \t" + ref.idArray);
			return new SimpleValue();
		}
		
		JCOValue value = array.getValues().get(index.intValue());
		
		
		if(!ref.hasFields() && (value.getType() == EValueType.DECIMAL || value.getType() == EValueType.INTEGER))
			return value;
		else if(value.getType() == EValueType.DOCUMENT && ref.hasFields()) {
			JCOValue valueFromDocument = ((DocumentValue) value).getValue(ref.arrayField.toString());
			if(valueFromDocument.getType() == EValueType.DECIMAL || valueFromDocument.getType() == EValueType.INTEGER)
				return valueFromDocument;
			else {
				JMH.add("Found no numeric value in field : \t" + ref.arrayField.toString());
				return new SimpleValue();
			}			
		}
		else {
			JMH.add("Wrong type of value in array : " + ref.idArray);
			return new SimpleValue();
		}
			
	}
	
	

}
