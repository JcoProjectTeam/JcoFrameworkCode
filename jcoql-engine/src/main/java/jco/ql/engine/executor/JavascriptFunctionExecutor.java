package jco.ql.engine.executor;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.JavascriptFunctionCommand;
import jco.ql.model.engine.JMH;


@Executor(JavascriptFunctionCommand.class)
public class JavascriptFunctionExecutor implements IExecutor<JavascriptFunctionCommand> {
    @Override
    public void execute(Pipeline pipeline, JavascriptFunctionCommand command) throws ExecuteProcessException {
    	// PF. 2022.03.24 - New Policy... in case of already existing JSFunction, a message is emitted and the newer version replace the old one
    	int ndx = alreadyExists(pipeline, command.getFunctionName());
        if(ndx == -1) 
        	pipeline.addJsFunction(command);
        else {
        	pipeline.updateJsFunction(command, ndx);
        	JMH.addJSMessage("[" + command.getInstruction().getInstructionName() + "]: definition of " + command.getFunctionName() + " has been replaced.");        	
        }
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + command.getFunctionName() + " function registered");
    }

	// return the index of JSF in the list. -1 if the JSF is not (yet) existing
    private static int alreadyExists(Pipeline pipeline, String functionName) {
        for(int i = 0; i < pipeline.getJsFunctions().size(); i++) {
            if(pipeline.getJsFunctions().get(i).getFunctionName().equals(functionName)) {
                return i;
            }
        }
        return -1;
    }
}
