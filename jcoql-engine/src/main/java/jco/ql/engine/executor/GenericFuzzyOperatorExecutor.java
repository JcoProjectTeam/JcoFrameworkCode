package jco.ql.engine.executor;

import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzySetModelCommand;
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
    	String fuzzysetModel = fgoCommand.getFuzzysetModelName();
    	String operator = fgoCommand.getGenericFuzzyOperatorName();
    	FuzzySetModelCommand fsmc = pipeline.getFuzzySetModels().get(operator);

    	if(fsmc == null) 
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: missing Fuzzyset Model " + fuzzysetModel + 
    							" for Generic Fuzzy Operator " + operator);
    	else if(fsmc.getDegrees().size() != fgoCommand.getDegrees().size()) 
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: Generic Fuzzyset Model " + operator + 
    							" must handle all degrees of Fuzzyset Model " + fuzzysetModel);
    	else if(!checkDegrees (fsmc, fgoCommand.getDegrees())) 
    		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: Generic Fuzzyset Model " + operator + 
								" must handle all degrees of Fuzzyset Model " + fuzzysetModel);
    	else {
    		if (pipeline.getFuzzyFunctions().containsKey(operator))
    	   		JMH.addFuzzyMessage("[" + fgoCommand.getInstruction().getInstructionName() + "]: definition of " + fgoCommand.getGenericFuzzyOperatorName() + " has been replaced.");
    		pipeline.addFuzzyFunction(fgoCommand);
    		JMH.addJCOMessage("[" + fgoCommand.getInstruction().getInstructionName() + "] executed:\t Generic Fuzzy Operator " + fgoCommand.getGenericFuzzyOperatorName() + " registered");
    	}
	}
    
    
    //****************************************************************************************************************************
    
    
    private boolean checkDegrees(FuzzySetModelCommand fsmc, List<Parameter> degrees) {
    	boolean presence = false;
    	List<Parameter> ftd = fsmc.getDegrees();
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
    
}
