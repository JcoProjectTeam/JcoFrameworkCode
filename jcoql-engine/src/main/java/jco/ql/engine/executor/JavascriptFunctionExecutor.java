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
    	// PF. 2022.03.24 - New Policy... in case of already existing USER Defined Function, a message is emitted and the newer version replace the old one
        if(pipeline.getJsFunctions().containsKey(command.getFunctionName())) 
        	JMH.addJSMessage("[" + command.getInstruction().getInstructionName() + "]: definition of " + command.getFunctionName() + " function has been replaced.");        	

        pipeline.addUserFunction(command);

		JMH.addJSMessage("[" + command.getInstruction().getInstructionName() + "]:\t" + command.getFunctionName() + " function registered");
    }
}
