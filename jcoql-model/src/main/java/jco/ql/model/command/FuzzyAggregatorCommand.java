package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.PointDefinition;
import jco.ql.parser.model.FuzzyAggregator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.ForAllClause;
import jco.ql.parser.model.util.Parameter;

public class FuzzyAggregatorCommand implements ICommand {
	private Instruction instruction = null;
	
	private String fuzzyAggregatorName;
	private List<Parameter> parameters;
	private Condition preCondition;
	private int versus;
	private List<ForAllClause> forAll;
	private Expression evaluate;
	private List<PointDefinition> polyline;
	

	public FuzzyAggregatorCommand(FuzzyAggregator fa) {
		instruction = fa;
		fuzzyAggregatorName = fa.fuzzyAggregator;
		preCondition = fa.preCondition;
		versus = fa.versus;
		evaluate = fa.evaluate;
		
		parameters = fa.parameters;
		forAll = fa.forAll;
		polyline = new ArrayList<>();
		
		for(FuzzyPoint p : fa.polyline)
			polyline.add(new PointDefinition(Float.parseFloat(p.x), Float.parseFloat(p.y)));
	}
	
	public String getFuzzyAggregatorName() {
		return fuzzyAggregatorName;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Condition getPreCondition() {
		return preCondition;
	}

	public int getVersus() {
		return versus;
	}

	public List<ForAllClause> getForAll() {
		return forAll;
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
