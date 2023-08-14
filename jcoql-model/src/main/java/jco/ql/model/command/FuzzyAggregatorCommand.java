package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.PointDefinition;
import jco.ql.parser.model.FuzzyAggregator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.ForAllDeriveElement;
import jco.ql.parser.model.util.Parameter;
import jco.ql.parser.model.util.SortFuzzyAggregatorElement;

public class FuzzyAggregatorCommand implements ICommand, FuzzyFunctionCommand {
	private Instruction instruction = null;

	private String fuzzyAggregatorName;
	private List<Parameter> parameters;
	private Condition preCondition;
	public List<SortFuzzyAggregatorElement> sortList;
	public List<ForAllDeriveElement> forAllDeriveList;
	private Expression evaluate;
	private List<PointDefinition> polyline;


	public FuzzyAggregatorCommand(FuzzyAggregator fa) {
		instruction = fa;
		fuzzyAggregatorName = fa.fuzzyAggregator;
		parameters = fa.parameters;
		preCondition = fa.preCondition;
		sortList = fa.sortList;
		forAllDeriveList = fa.forAllDeriveList;
		polyline = new ArrayList<>();		
		for(FuzzyPoint p : fa.polyline)
			polyline.add(new PointDefinition(Float.parseFloat(p.x), Float.parseFloat(p.y)));
		evaluate = fa.evaluate;
	}

	@Override
	public int getType() {
		return AGGREGATOR;
	}

	@Override
	public String getFuzzyFunctionName() {
		return fuzzyAggregatorName;
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

	public List<ForAllDeriveElement> getForAllDeriveList() {
		return forAllDeriveList;
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
