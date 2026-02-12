package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.GenericFuzzyOperator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPolyline;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.Parameter;

/**
*
* @author Balicco Matteo
*
*/

public class GenericFuzzyOperatorCommand implements ICommand, FunctionEvaluatorInterface {
	private Instruction 		instruction = null;
    private String 				genericFuzzyOperatorName;
    private List<Parameter> 	parameters;
    private Condition 			precondition;
    private List<Expression> 	evaluate;
    private List<FuzzyPolyline> polylines;
    private String 				fuzzysetModelName ;
    private List<Parameter> 	degrees;


    public GenericFuzzyOperatorCommand(GenericFuzzyOperator fgo) {
    	instruction 				= fgo;
    	genericFuzzyOperatorName	= fgo.genericFuzzyOperator;
    	parameters 					= fgo.parameters;
		precondition 				= fgo.precondition;
		fuzzysetModelName 			= fgo.fuzzyTypeName;
		evaluate 					= fgo.evaluate;
		polylines 					= fgo.polylines;
		degrees 					= fgo.degrees;
		
    }   

        
	@Override
	public int getFunctionEvaluatorType() {
		return FUZZY_GENERIC_OPERATOR;
	}

	@Override
	public String getFunctionEvaluatorName() {
        return genericFuzzyOperatorName;
	}

	public String getGenericFuzzyOperatorName() {
        return genericFuzzyOperatorName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Condition getPrecondition() {
    	return precondition;
    }

    public List<Expression>getEvaluate() {
        return evaluate;
    }

    public List<FuzzyPolyline> getPolylines() {
        return polylines;
    }
    
    public String getFuzzysetModelName() {
		return fuzzysetModelName;
	}
    
    public List<Parameter> getDegrees() {
		return degrees;
	}

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
