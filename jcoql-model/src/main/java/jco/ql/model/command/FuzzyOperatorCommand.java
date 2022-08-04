package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.PointDefinition;
import jco.ql.parser.model.FuzzyOperator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.Parameter;

public class FuzzyOperatorCommand implements ICommand {
	private Instruction instruction = null;
    private String fuzzyOperatorName;
    private List<Parameter> parameters;
    private Condition precondition;
    private Expression evaluate;
    private List<PointDefinition> polyline;


    public FuzzyOperatorCommand(FuzzyOperator fo) {
    	instruction = fo;
    	fuzzyOperatorName = fo.fuzzyOperator;

    	parameters = fo.parameters;
		precondition = fo.preCondition;
		evaluate = fo.evaluate;
		
		polyline = new ArrayList<>();
		for(FuzzyPoint p : fo.polyline)
			polyline.add(new PointDefinition(Float.parseFloat(p.x), Float.parseFloat(p.y)));
    }   

        
    public String getFuzzyOperatorName() {
        return fuzzyOperatorName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Condition getPrecondition() {
    return precondition;
}


    public Expression getEvaluate() {
        return evaluate;
    }

    public List<PointDefinition> getPolyline() {
        return polyline;
    }

	@Override
	public Instruction getInstruction() {
		return instruction;
	}
}
