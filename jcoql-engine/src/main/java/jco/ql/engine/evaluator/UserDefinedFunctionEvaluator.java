package jco.ql.engine.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FunctionEvaluatorInterface;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.command.JavaFunctionCommand;
import jco.ql.model.command.JavascriptFunctionCommand;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.FunctionFactor;
import jco.ql.parser.model.util.Parameter;

public class UserDefinedFunctionEvaluator {

	public static JCOValue evaluate(FunctionFactor functionCall, Pipeline pipeline) {
    	JCOValue value = new SimpleValue();		// null value

    	if (!pipeline.getUserFunctions().containsKey(functionCall.functionName)) {
    		JMH.addJSMessage("Function not found:\t" + functionCall.functionName);
    		return value; // null
    	}

    	FunctionEvaluatorInterface function = pipeline.getUserFunctions().get(functionCall.functionName);

    	if (function instanceof JavaFunctionCommand) {
	    	JavaFunctionCommand jf = (JavaFunctionCommand) function;
	    	value = evaluateJavaFunction (jf, functionCall, pipeline);
    	}
    	else if (function instanceof JavascriptFunctionCommand) {
	    	JavascriptFunctionCommand jsf = (JavascriptFunctionCommand) function;
	    	value = evaluateJavascriptFunction (jsf, functionCall, pipeline);
    	}
    	else if (function instanceof FuzzyEvaluatorCommand) {
    		FuzzyEvaluatorCommand fe = (FuzzyEvaluatorCommand) function;
	    	value = FuzzyEvaluatorEvaluator.evaluateCrisp(fe, functionCall, pipeline);
    	}
    	return value;
	}

	
	static JCOValue evaluateJavaFunction(JavaFunctionCommand jfc, FunctionFactor functionCall, Pipeline pipeline) {
    	JCOValue value = new SimpleValue();		// null value	
    	if (jfc.getParameters().size() != functionCall.functionParams.size()) {
    		JMH.addJSMessage("Wrong number of parameters for Java function:\t" + jfc.getFunctionEvaluatorName());
    		return value; // null
    	}

    	List<JCOValue> actualParameters = getActualParameters (functionCall.functionParams, pipeline);
    	if (!checkParameters (actualParameters, jfc.getParameters())) {
    		JMH.addJSMessage("Wrong type of parameters for Java function:\t" + jfc.getFunctionEvaluatorName());
    		return value; // null
    	}

    	if (jfc.hasPreCondition()) {    		
	    	DocumentDefinition jsDoc = createJSDoc (actualParameters, jfc.getParameters());   	
	    	Pipeline jsPipeline = new Pipeline(pipeline);
	    	jsPipeline.setCurrentDoc(jsDoc);
	    	if (!ConditionEvaluator.matchCondition(jfc.getPreCondition(), jsPipeline)) {
	    		JMH.addJSMessage("Precondition not matched for Java function:\t" + jfc.getFunctionEvaluatorName());
	    		return value; // null
	    	}
    	}

		Object mArg [] = new Object [actualParameters.size()];
		for (int i=0; i<actualParameters.size(); i++)
			mArg [i] = actualParameters.get(i).getValue();
		try {
			Object javaRes = jfc.getMethodToInvoke().invoke(null, mArg);
			if (javaRes instanceof Integer)
				value = new SimpleValue ((int) javaRes);
			else if (javaRes instanceof Float)
				value = new SimpleValue ((double) javaRes);
			else if (javaRes instanceof Double)
				value = new SimpleValue ((double) javaRes);
			else if (javaRes instanceof Boolean)
				value = new SimpleValue ((boolean) javaRes);
			else if (javaRes instanceof String)
				value = new SimpleValue ((String) javaRes);
			else 
				value = new SimpleValue (javaRes.toString());

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			JMH.addJSMessage("Cannot invoke Java function " + jfc.getFunctionEvaluatorName() + "\n" + e1.toString());
			e1.printStackTrace();
		}

    	return value;
	}

	
	static JCOValue evaluateJavascriptFunction(JavascriptFunctionCommand jsf, FunctionFactor functionCall, Pipeline pipeline) {
    	if (jsf.getParameters().size() != functionCall.functionParams.size()) {
    		JMH.addJSMessage("Wrong number of parameters for Javascript function:\t" + jsf.getFunctionEvaluatorName());
    		return new SimpleValue (); // null
    	}

    	List<JCOValue> actualParameters = getActualParameters (functionCall.functionParams, pipeline);
    	if (!checkParameters (actualParameters, jsf.getParameters())) {
    		JMH.addJSMessage("Wrong type of parameters for Javascript function:\t" + jsf.getFunctionEvaluatorName());
    		return new SimpleValue (); // null    		
    	}

    	if (jsf.hasPreCondition()) {    		
	    	DocumentDefinition jsDoc = createJSDoc (actualParameters, jsf.getParameters());   	
	    	Pipeline jsPipeline = new Pipeline(pipeline);
	    	jsPipeline.setCurrentDoc(jsDoc);
	    	if (!ConditionEvaluator.matchCondition(jsf.getPreCondition(), jsPipeline)) {
	    		JMH.addJSMessage("Precondition not matched for Javascript function:\t" + jsf.getFunctionEvaluatorName());
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
        	String st = "JS Exception in " + jsf.getFunctionEvaluatorName() + " at (" + e.getLineNumber() + ", " + e.getColumnNumber() + ")";
        	st += "\n" + e.getMessage();
            JMH.addJSMessage(st);
        }   
    	return new SimpleValue (); // null	
	}

	/* ********************************************************************************************* */
    
    private static List<JCOValue> getActualParameters(List<Expression> functionParams, Pipeline pipeline) {
    	List<JCOValue> actualParameters = new ArrayList<JCOValue> ();
    	for (Expression expr : functionParams)
    		actualParameters.add(ExpressionPredicateEvaluator.calculate(expr, pipeline));
		return actualParameters;
	}


	// by contruction actualParameters and parameters have the same number of element
	private static boolean checkParameters(List<JCOValue> actualParameters, List<Parameter> parameters) {
		// TODO - ZUN non appena definiti se ci sono tipi standard
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

    	code.append(jsFun.getFunctionEvaluatorName() + "(");
        for(int p = 0; p < values.size(); p++) {
        	JCOValue v = values.get(p);
        	String valueString = getValueString (v);
        	if(p == 0)
            	code.append(valueString);
            else 
            	code.append(", " + valueString);
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
