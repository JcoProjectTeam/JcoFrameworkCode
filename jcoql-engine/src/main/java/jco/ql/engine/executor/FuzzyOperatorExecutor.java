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
    	int ndx = alreadyExists(pipeline, foCommand.getFuzzyOperatorName());
    	if(ndx == -1)
    		pipeline.addFuzzyOperator(foCommand);
    	else {
    		pipeline.updateFuzzyOperator (foCommand, ndx);    		
    		JMH.addFuzzyMessage("[" + foCommand.getInstruction().getInstructionName() + "]: definition of " + foCommand.getFuzzyOperatorName() + " has been replaced.");
    	}
		JMH.addJCOMessage("[" + foCommand.getInstruction().getInstructionName() + "] executed:\t" + foCommand.getFuzzyOperatorName() + " fuzzy operator registered");
    }

	// return the index of FO in the list. -1 if the FO is not (yet) existing
    private int alreadyExists(Pipeline pipeline, String foName) {
        for(int i = 0; i < pipeline.getFuzzyOperators().size(); i++) {
            if(pipeline.getFuzzyOperators().get(i).getFuzzyOperatorName().equals(foName)) {
                return i;
            }
        }
        return -1;
    }
    

    /***********************************************************/
    

}
