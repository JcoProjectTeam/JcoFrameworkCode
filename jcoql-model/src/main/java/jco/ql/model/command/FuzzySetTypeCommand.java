package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.FuzzySetType;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyOperatorDefinition;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.Parameter;

/**
*
* @author Balicco Matteo
*
*/

public class FuzzySetTypeCommand implements ICommand {
	private Instruction 			instruction = null;
    private String 					fuzzySetTypeName;
    private List<Parameter> 		degrees;
	private List<String> 			derivedDegrees;
	private List<Expression> 		derivedExpr;
	private Condition 				constraint;
	private FuzzyOperatorDefinition defOr;
	private FuzzyOperatorDefinition defAnd;
	private FuzzyOperatorDefinition defNot;


    public FuzzySetTypeCommand(FuzzySetType ft) {
    	instruction 				= ft;
        fuzzySetTypeName			= ft.fuzzySetType;
        degrees						= ft.degrees;
        derivedDegrees				= ft.derivedDegrees;
    	derivedExpr 				= ft.derivedExpr;
    	constraint					= ft.constraint;
    	defOr						= ft.defOr;
    	defAnd						= ft.defAnd;
    	defNot						= ft.defNot;
    }   

        
    public String getFuzzySetTypeName() {
        return fuzzySetTypeName;
    }

    public List<Parameter> getDegrees() {
        return degrees;
    }

    public List<String> getDerivedDegrees() {
    return derivedDegrees;
    }


    public List<Expression> getDerivedExpr() {
        return derivedExpr;
    }

    public Condition getConstraint() {
        return constraint;
    }
    
    public FuzzyOperatorDefinition getDefOr() {
        return defOr;
    }
    
    public FuzzyOperatorDefinition getDefAnd() {
        return defAnd;
    }
    
    public FuzzyOperatorDefinition getDefNot() {
        return defNot;
    }

	@Override
	public Instruction getInstruction() {
		return instruction;
	}
}
