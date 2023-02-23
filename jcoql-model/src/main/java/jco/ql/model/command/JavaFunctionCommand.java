package jco.ql.model.command;

import java.nio.file.Path;
import java.util.List;

import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.JavaFunction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.util.Parameter;
import java.lang.reflect.Method;

public class JavaFunctionCommand implements ICommand, FunctionCommand {
	private Instruction instruction = null;

    private String functionName;
    private List<Parameter> parameters;
    private Condition preCondition;
    private String className;
    private String importClause;
    private String body;
    
    private String code;
    private Path javaClass;
    private Method methodToInvoke;


	public JavaFunctionCommand (JavaFunction jF) {
		instruction = jF;		

		functionName = jF.functionName;
		className = jF.className;
		parameters = jF.parameters;
		preCondition = jF.preCondition;		
		
		body = jF.body;  
		code = composeCode();
		javaClass = null;
		methodToInvoke = null;
	}

	@Override
	public String getFunctionName() {
        return functionName;
    }

	public String getCode () {
		return code;
	}

	public Path getJavaClass () {
		return javaClass;
	}

	public void setJavaClass (Path javaClass) {
		this.javaClass = javaClass ;
	}

	public Method getMethodToInvoke () {
		return methodToInvoke;
	}

	public void setMethodToInvoke (Method methodToInvoke) {
		this.methodToInvoke = methodToInvoke;
	}

	public String getClassName () {
		return className;
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

    public boolean hasImportClause() {
        return (importClause != null);
    }

    public String getBody() {
        return body;
    }

    
	private String composeCode() {
    	StringBuffer code = new StringBuffer ();
    	if (hasImportClause())
    		code.append(importClause + "\n");
    	code.append("public class " + className + " \n");
        code.append(body + "\n\n");

        return code.toString();
	}


    @Override
    public String toString() {
        String stringVal = "CREATE JAVA FUNCTION ";
        return stringVal + this.functionName;
    }

	@Override
	public Instruction getInstruction() {
		return instruction;
	}


	@Override
	public int getType() {
		return JAVA_FUNCTION;
	}

}
