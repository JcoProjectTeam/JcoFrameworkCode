package jco.ql.engine.byZunEvaluator;



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
		ArrayValue array = pipeline.getCurrentDoc().getArrayValue(ref.id_array);
		if(array == null) {
			JMH.add("Array not declared: \t" + ref.id_array);
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
			JMH.add("Index out of acceptable range for array: \t" + ref.id_array);
			return new SimpleValue();
		}
		
		JCOValue value = array.getValues().get(index.intValue());
		
		
		if(!ref.hasFields() && (value.getType() == EValueType.DECIMAL || value.getType() == EValueType.INTEGER))
			return value;
		else if(value.getType() == EValueType.DOCUMENT && ref.hasFields()) {
			JCOValue valueFromDocument = ((DocumentValue) value).getValue(ref.array_field.toString());
			if(valueFromDocument.getType() == EValueType.DECIMAL || valueFromDocument.getType() == EValueType.INTEGER)
				return valueFromDocument;
			else {
				JMH.add("Found no numeric value in field : \t" + ref.array_field.toString());
				return new SimpleValue();
			}			
		}
		else {
			JMH.add("Wrong type of value in array : " + ref.id_array);
			return new SimpleValue();
		}
			
	}
	
	

}
