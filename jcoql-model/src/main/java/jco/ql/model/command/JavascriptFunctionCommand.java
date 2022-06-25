package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.JavascriptFunction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.util.Parameter;

public class JavascriptFunctionCommand implements ICommand {
	private Instruction instruction = null;

    private String functionName;
    private List<Parameter> parameters;
    private Condition preCondition;
    private String body;
    
    private String code;


	public JavascriptFunctionCommand (JavascriptFunction jsF) {
		instruction = jsF;		

		functionName = jsF.functionName;
		parameters = jsF.parameters;
		preCondition = jsF.preCondition;		
		body = jsF.body;  
		code = composeCode();
	}


	public String getFunctionName() {
        return functionName;
    }

	public String getCode () {
		return code;
	}

	
	public List<Parameter> getParameters() {
        return parameters;
    }

    public Condition getPreCondition() {
        return preCondition;
    }

    public boolean hasPreCondition() {
        return (preCondition != null);
    }

    public String getBody() {
        return body;
    }

    
	private String composeCode() {
    	StringBuffer code = new StringBuffer ("function " + functionName + "(");

    	int countFunctionParam = 0;
        for(Parameter pd : parameters) {
            if(countFunctionParam == 0) 
            	code.append(pd.name);
            else
            	code.append(", " + pd.name);
            countFunctionParam++;
        }
        code.append(")\n");
        code.append(body + "\n\n");

        return code.toString();
	}


    @Override
    public String toString() {
        String stringVal = "CREATE JAVASCRIPT FUNCTION ";
        return stringVal + this.functionName;
    }

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
