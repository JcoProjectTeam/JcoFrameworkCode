package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.PointDefinition;
import jco.ql.parser.model.FuzzyEvaluator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPoint;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.ForAllDeriveElement;
import jco.ql.parser.model.util.Parameter;
import jco.ql.parser.model.util.SortFuzzyEvaluatorArray;

public class FuzzyEvaluatorCommand implements ICommand, FuzzyFunctionCommand {
	private Instruction instruction = null;

	private String fuzzyEvaluatorName;
	private List<Parameter> parameters;
	private Condition preCondition;
	public List<SortFuzzyEvaluatorArray> sortList;
	public List<ForAllDeriveElement> forAllDeriveList;
	private Expression evaluate;
	private List<PointDefinition> polyline;
	private int type;


	public FuzzyEvaluatorCommand(FuzzyEvaluator fe) {
		instruction = fe;
		fuzzyEvaluatorName = fe.fuzzyEvaluatorName;
		type = EVALUATOR;
		if (fe.isFuzzyAggregator)
			type = AGGREGATOR;
		parameters = fe.parameters;
		preCondition = fe.preCondition;
		sortList = fe.sortList;
		forAllDeriveList = fe.forAllDeriveList;
		polyline = new ArrayList<>();		
		for(FuzzyPoint p : fe.polyline)
			polyline.add(new PointDefinition(Float.parseFloat(p.x), Float.parseFloat(p.y)));
		evaluate = fe.evaluate;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getFuzzyFunctionName() {
		return fuzzyEvaluatorName;
	}

	public String getFuzzyEvaluatorType() {
		if (type == AGGREGATOR)
			return "Fuzzy Aggregator";
		return "Fuzzy Evaluator";	
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
