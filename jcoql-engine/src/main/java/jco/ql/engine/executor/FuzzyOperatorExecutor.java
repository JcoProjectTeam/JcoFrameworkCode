package jco.ql.engine.executor;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.engine.JMH;


@Executor(FuzzyOperatorCommand.class)
public class FuzzyOperatorExecutor implements IExecutor<FuzzyOperatorCommand> {

    @Override
    public void execute(Pipeline pipeline, FuzzyOperatorCommand foCommand) throws ExecuteProcessException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	if (pipeline.getFuzzyOperators().contains(foCommand.getFuzzyOperatorName()))
    		JMH.addFuzzyMessage("[" + foCommand.getInstruction().getInstructionName() + "]: definition of " + foCommand.getFuzzyOperatorName() + " has been replaced.");
    	pipeline.addFuzzyOperator(foCommand);
		JMH.addJCOMessage("[" + foCommand.getInstruction().getInstructionName() + "] executed:\t" + foCommand.getFuzzyOperatorName() + " fuzzy operator registered");
    }
    

}
