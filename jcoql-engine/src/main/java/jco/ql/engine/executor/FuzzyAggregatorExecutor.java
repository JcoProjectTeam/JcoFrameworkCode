//FI Created on 27.10.2022
package jco.ql.engine.executor;

import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzyAggregatorCommand;
import jco.ql.model.engine.JMH;


@Executor(FuzzyAggregatorCommand.class)
public class FuzzyAggregatorExecutor implements IExecutor<FuzzyAggregatorCommand>{

	@Override
	public void execute(Pipeline pipeline, FuzzyAggregatorCommand faCommand) throws ExecuteProcessException, ScriptException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	if (pipeline.getFuzzyFunctions().containsKey(faCommand.getFuzzyFunctionName()))
    		JMH.addFuzzyMessage("[" + faCommand.getInstruction().getInstructionName() + "]: definition of " + faCommand.getFuzzyFunctionName() + " has been replaced.");
    	pipeline.addFuzzyFunction(faCommand);
		JMH.addJCOMessage("[" + faCommand.getInstruction().getInstructionName() + "] executed:\t" + faCommand.getFuzzyFunctionName() + " fuzzy aggregator registered");
    }
	
}
