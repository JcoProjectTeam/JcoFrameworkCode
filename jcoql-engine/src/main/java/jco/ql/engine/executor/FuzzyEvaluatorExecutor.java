//FI Created on 27.10.2022
package jco.ql.engine.executor;

import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.engine.JMH;


@Executor(FuzzyEvaluatorCommand.class)
public class FuzzyEvaluatorExecutor implements IExecutor<FuzzyEvaluatorCommand>{

	@Override
	public void execute(Pipeline pipeline, FuzzyEvaluatorCommand feCommand) throws ExecuteProcessException, ScriptException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	if (pipeline.getFuzzyFunctions().containsKey(feCommand.getFuzzyFunctionName()))
    		JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: definition of " + feCommand.getFuzzyFunctionName() + " has been replaced.");
    	pipeline.addFuzzyFunction(feCommand);
		JMH.addJCOMessage("[" + feCommand.getInstruction().getInstructionName() + "] executed:\t" + feCommand.getFuzzyFunctionName() + " registered");
    }
	
}
