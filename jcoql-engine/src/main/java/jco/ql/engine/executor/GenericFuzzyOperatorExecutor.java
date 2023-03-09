package jco.ql.engine.executor;

import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.GenericFuzzyOperatorCommand;
import jco.ql.model.engine.JMH;
import jco.ql.parser.model.util.Parameter;

/**
*
* @author Balicco Matteo
*
*/


@Executor(GenericFuzzyOperatorCommand.class)
public class GenericFuzzyOperatorExecutor implements IExecutor<GenericFuzzyOperatorCommand> {

    @Override
    public void execute(Pipeline pipeline, GenericFuzzyOperatorCommand fgoCommand) throws ExecuteProcessException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	int ndx 		= alreadyExists(pipeline, fgoCommand.getGenericFuzzyOperatorName());
    	int typePos 	= existType(pipeline, fgoCommand.getFuzzyTypeName());
    	boolean useAllD = useAllDegrees(pipeline, fgoCommand.getDegrees(), typePos);
    	boolean checkD 	= checkDegrees(pipeline, fgoCommand.getDegrees(), typePos);
    	
    	
    	if(typePos == -1) {
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: fuzzy set type of " + fgoCommand.getGenericFuzzyOperatorName() + " is not been defined.");
    	} else if(!useAllD) {
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: definition of " + fgoCommand.getGenericFuzzyOperatorName() + " must have all degrees of his fuzzy set type.");
    	} else if(!checkD) {
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: definition of " + fgoCommand.getGenericFuzzyOperatorName() + " must have the same degrees of his fuzzy set type.");
    	} else if(ndx == -1) {
    		pipeline.addGenericFuzzyOperator(fgoCommand);
    		JMH.addJCOMessage("[" + fgoCommand.getInstruction().getInstructionName() + "] executed:\t" + fgoCommand.getGenericFuzzyOperatorName() + " fuzzy operator registered");
    	} else {
    		pipeline.updateGenericFuzzyOperator (fgoCommand, ndx);    		
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: definition of " + fgoCommand.getGenericFuzzyOperatorName() + " has been replaced.");
    		JMH.addJCOMessage("[" + fgoCommand.getInstruction().getInstructionName() + "] executed:\t" + fgoCommand.getGenericFuzzyOperatorName() + " fuzzy operator registered");
    	}
	}
    
   
    
    
    //****************************************************************************************************************************
    
    // return the index of Fgo in the list. -1 if the FO is not (yet) existing
    private int alreadyExists(Pipeline pipeline, String foName) {
        for(int i = 0; i < pipeline.getFuzzyOperators().size(); i++) {
            if(pipeline.getFuzzyOperators().get(i).getFuzzyOperatorName().equals(foName)) {
                return i;
            }
        }
        return -1;
    }
    
    
    // return the index of FT refer to FGO in the list. -1 if the FO is not (yet) existing
    private int existType(Pipeline pipeline, String type) {
    	for(int i = 0; i < pipeline.getFuzzySetType().size(); i++) {
            if(pipeline.getFuzzySetType().get(i).getFuzzySetTypeName().equals(type)) {
                return i;
            }
        }
        return -1;
	}
    
    private boolean useAllDegrees(Pipeline pipeline, List<Parameter> degrees, int pos) {
    	if(pos == -1) 
    		return false;
    	if(pipeline.getFuzzySetType().get(pos).getDegrees().size() == degrees.size()) 
                return true;
        return false;
	}
    
    private boolean checkDegrees(Pipeline pipeline, List<Parameter> degrees, int pos) {
    	if(pos == -1) 
    		return false;
    	boolean presence = false;
    	List<Parameter> ftd = pipeline.getFuzzySetType().get(pos).getDegrees();
    	for(int i = 0; i < degrees.size(); i++) {
    		for(int j = 0; j < ftd.size(); j++) {
    			if(degrees.get(i).name.equals(ftd.get(j).name)) {
    				presence = true;
    			}
    		}
    		if (!presence)
				return false;
    		presence = false;
        }
        return true;
	}
    
    
    

    /***********************************************************/
    

}
