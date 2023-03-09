package jco.ql.engine.executor;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzySetTypeCommand;
import jco.ql.model.engine.JMH;

/**
*
* @author Balicco Matteo
*
*/


@Executor(FuzzySetTypeCommand.class)
public class FuzzySetTypeExecutor implements IExecutor<FuzzySetTypeCommand> {

    @Override
    public void execute(Pipeline pipeline, FuzzySetTypeCommand ftCommand) throws ExecuteProcessException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	int ndx = alreadyExists(pipeline, ftCommand.getFuzzySetTypeName() );
    	if(ndx == -1) {
    		pipeline.addFuzzySetTypeExecutor(ftCommand);
    		//for (int i=0; i<pipeline.getFuzzySetType().size(); i++) 
    		//	JMH.addFuzzyMessage("PIPELINE IS: " + pipeline.getFuzzySetType().get(i).getFuzzySetTypeName());
    	} else {
    		pipeline.updateFuzzySetTypeExecutor (ftCommand, ndx);    		
    		JMH.addFuzzyMessage("[" + ftCommand.getInstruction().getInstructionName() + "]: definition of " + ftCommand.getFuzzySetTypeName() + " has been replaced.");
    	}	
    }

	// return the index of FT in the list. -1 if the FO is not (yet) existing
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
