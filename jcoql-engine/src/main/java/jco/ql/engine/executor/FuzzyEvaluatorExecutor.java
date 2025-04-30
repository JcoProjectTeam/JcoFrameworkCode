//FI Created on 27.10.2022
package jco.ql.engine.executor;

import java.util.List;

import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.engine.JMH;
import jco.ql.parser.model.util.Parameter;


@Executor(FuzzyEvaluatorCommand.class)
public class FuzzyEvaluatorExecutor implements IExecutor<FuzzyEvaluatorCommand>{

	@Override
	public void execute(Pipeline pipeline, FuzzyEvaluatorCommand feCommand) throws ExecuteProcessException, ScriptException {
    	// PF. 2022.03.24 - New Policy... in case of already existing Fuzzy Operator, a message is emitted and the newer version replace the old one
    	
		if (!feCommand.isGenericEvaluator()) { // GB
			if (pipeline.getFuzzyFunctions().containsKey(feCommand.getFuzzyFunctionName()))
	    		JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: definition of " 
	    				+ feCommand.getFuzzyFunctionName() + " has been replaced.");
	
	    	pipeline.addFuzzyFunction(feCommand);
			JMH.addJCOMessage("[" + feCommand.getInstruction().getInstructionName() + "] executed:\t" + feCommand.getFuzzyFunctionName() 
				+ " registered");
		}
		else {
			// GB
			String fuzzySetModel = feCommand.getFuzzySetModelName();
			String evaluatorName = feCommand.getFuzzyFunctionName();
			FuzzySetModelCommand fsmc = pipeline.getFuzzySetModels().get(fuzzySetModel);
			
			if (fsmc == null)
				JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: missing Fuzzyset Model " + fuzzySetModel  
						+ " for Generic Fuzzy Evaluator " + evaluatorName);
			else if(fsmc.getDegrees().size() != feCommand.getDegrees().size()) 
	    		JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: Generic Fuzzyset Model " + evaluatorName 
	    				+ " must handle all degrees of Fuzzyset Model " + fuzzySetModel);
	    	else if(!checkDegrees (fsmc, feCommand.getDegrees())) 
	    		JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: Generic Fuzzyset Model " + evaluatorName  
						+ " must handle all degrees of Fuzzyset Model " + fuzzySetModel);
	    	else {
	    		if (pipeline.getFuzzyFunctions().containsKey(evaluatorName))
	    	   		JMH.addFuzzyMessage("[" + feCommand.getInstruction().getInstructionName() + "]: definition of " 
	    	   				+ feCommand.getFuzzyFunctionName() + " has been replaced.");
	    		pipeline.addFuzzyFunction(feCommand);
	    		JMH.addJCOMessage("[" + feCommand.getInstruction().getInstructionName() + "] executed:\t Generic Fuzzy Evaluator " 
	    				+ feCommand.getFuzzyFunctionName() + " registered");
	    	}
		}
	}
		
	// GB
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
