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
	public void execute(Pipeline pipeline, FuzzyAggregatorCommand faCommand)
			throws ExecuteProcessException, ScriptException {
    	
    	int ndx = alreadyExists(pipeline, faCommand.getFuzzyAggregatorName());
    	if(ndx == -1)
    		pipeline.addFuzzyAggregator(faCommand);
    	else {
    		pipeline.updateFuzzyAggregator(faCommand, ndx);    		
    		JMH.addFuzzyMessage("[" + faCommand.getInstruction().getInstructionName() + "]: definition of " + faCommand.getFuzzyAggregatorName() + " has been replaced.");
    	}
		JMH.addJCOMessage("[" + faCommand.getInstruction().getInstructionName() + "] executed:\t" + faCommand.getFuzzyAggregatorName() + " fuzzy operator registered");
		
	}
	
    private int alreadyExists(Pipeline pipeline, String faName) {
        for(int i = 0; i < pipeline.getFuzzyAggregators().size(); i++) {
            if(pipeline.getFuzzyAggregators().get(i).getFuzzyAggregatorName().equals(faName)) {
                return i;
            }
        }
        return -1;
    }

}
