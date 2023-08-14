package jco.ql.engine.executor;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.engine.JMH;

/**
*
* @author Balicco Matteo
*
*/


@Executor(FuzzySetModelCommand.class)
public class FuzzySetModelExecutor implements IExecutor<FuzzySetModelCommand> {

    @Override
    public void execute(Pipeline pipeline, FuzzySetModelCommand ftCommand) throws ExecuteProcessException {
    	if (pipeline.getFuzzySetModels().containsKey(ftCommand.getFuzzySetModelName()))
    		JMH.addFuzzyMessage("[" + ftCommand.getInstruction().getInstructionName() + "]: definition of Fuzzyset Model " + ftCommand.getFuzzySetModelName() + " has been replaced.");
    	pipeline.addFuzzySetModel(ftCommand);
		JMH.addJCOMessage("[" + ftCommand.getInstruction().getInstructionName() + "] executed:\t Fuzzyset Model " + ftCommand.getFuzzySetModelName()+ " registered");
    }

}
