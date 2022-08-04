package jco.ql.engine.byZunEvaluator;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.JavascriptFunctionCommand;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.FunctionFactor;
import jco.ql.parser.model.util.Parameter;

public class JavascriptEvalutor {

	public static JCOValue evaluate(FunctionFactor jsFunctionContainer, Pipeline pipeline) {
    	int ndx = alreadyExists(pipeline, jsFunctionContainer.functionName);
    	if (ndx == -1) {
    		JMH.addJSMessage("Javascript Function not found:\t" + jsFunctionContainer.functionName);
    		return new SimpleValue (); // null
    	}

    	JavascriptFunctionCommand jsf = pipeline.getJsFunctions().get(ndx);
    	if (jsf.getParameters().size() != jsFunctionContainer.functionParams.size()) {
    		JMH.addJSMessage("Wrong number of parameters for Fuzzy Operator:\t" + jsf.getFunctionName());
    		return new SimpleValue (); // null
    	}

    	List<JCOValue> actualParameters = getActualParameters (jsFunctionContainer.functionParams, pipeline);
    	if (!checkParameters (actualParameters, jsf.getParameters())) {
    		JMH.addJSMessage("Wrong type of parameters for Fuzzy Operator:\t" + jsf.getBody());
    		return new SimpleValue (); // null    		
    	}

    	if (jsf.hasPreCondition()) {    		
	    	DocumentDefinition jsDoc = createJSDoc (actualParameters, jsf.getParameters());   	
	    	Pipeline jsPipeline = new Pipeline(pipeline);
	    	jsPipeline.setCurrentDoc(jsDoc);
	    	if (!ConditionEvaluator.matchCondition(jsf.getPreCondition(), jsPipeline)) {
	    		JMH.addJSMessage("Precondition not matched for Javascript Function:\t" + jsf.getFunctionName());
	    		return new SimpleValue (); // null    		
	    	}
    	}

        ScriptEngine engine = pipeline.getEngine();
        try {
        	String jsCode = getJSCode(jsf, actualParameters);
            String jsResult = engine.eval(jsCode).toString();
            // TODO gestire anche valori diversi 
            return new SimpleValue(Double.parseDouble(jsResult));
        } catch (ScriptException e) {
        	String st = "JS Exception in " + jsf.getFunctionName() + " at (" + e.getLineNumber() + ", " + e.getColumnNumber() + ")";
        	st += "\n" + e.getMessage();
            JMH.addJSMessage(st);
        }    	
    	    	
    	return new SimpleValue (); // null
	}

	/* ********************************************************************************************* */

	// return the index of JSF in the list. -1 if the JSF is not (yet) existing
    private static int alreadyExists(Pipeline pipeline, String functionName) {
        for(int i = 0; i < pipeline.getJsFunctions().size(); i++) {
            if(pipeline.getJsFunctions().get(i).getFunctionName().equals(functionName)) {
                return i;
            }
        }
        return -1;
    }

    
    private static List<JCOValue> getActualParameters(List<Expression> functionParams, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    	for (Expression expr : functionParams)
    		actualParameters.add(ExpressionPredicateEvaluator.calculate(expr, pipeline));
		return actualParameters;
	}


	// by contruction actualParameters and parameters have the same number of element
	private static boolean checkParameters(List<JCOValue> actualParameters, List<Parameter> parameters) {
		// TODO - non appena definiti se ci sono tipi standard
		for (int i=0; i<actualParameters.size(); i++) {
			;
		}
		return true;
	}


	// by contruction actualParameters and parameters have the same number of element
	private static DocumentDefinition createJSDoc(List<JCOValue> actualParameters, List<Parameter> parameters) {
		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
		for (int i=0; i<actualParameters.size(); i++) {
			FieldDefinition fd = new FieldDefinition(parameters.get(i).name, actualParameters.get(i));
			fields.add(fd);
		}
		DocumentDefinition doc = new DocumentDefinition(fields);
		return doc;
	}


    private static String getJSCode(JavascriptFunctionCommand jsFun, List<JCOValue> values) {
    	StringBuffer code = new StringBuffer (jsFun.getCode());

    	code.append(jsFun.getFunctionName() + "(");
        for(int p = 0; p < values.size(); p++) {
        	JCOValue v = values.get(p);
        	String valueString = getValueString (v);
        	if(p == 0)
            	code.append(valueString);
            else 
            	code.append(", " +valueString);
        }

        code.append(");\n");

        return code.toString();
    }

    // TODO ... handle type... now is a just a draft
	private static String getValueString(JCOValue v) {
		if (v == null)
			return "null";

		if (v.getType() == EValueType.STRING)
			return "\"" + v.getStringValue().replace("\"", "\\\"") + "\"";
		
		return v.getStringValue();
	}
}
