package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.FuzzyEvaluator;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.condition.Condition;
import jco.ql.parser.model.fuzzy.FuzzyPolyline;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.util.FEInternalClause;
import jco.ql.parser.model.util.Parameter;

public class FuzzyEvaluatorCommand implements ICommand, FunctionEvaluatorInterface {
	private Instruction instruction = null;

	private String fuzzyEvaluatorName;
	private List<Parameter> parameters;
	private Condition preCondition;
	public List<FEInternalClause> feInternalClauseList;
	private Expression evaluate;
	private FuzzyPolyline polyline;
	private int type;

	// GB
	private String fuzzyEvaluatorType;
	private List<Expression> genericEvaluate;
	private List<FuzzyPolyline> genericPolylines;
	private List<Parameter> genericDegrees;

	public FuzzyEvaluatorCommand(FuzzyEvaluator fe) {
		instruction = fe;
		fuzzyEvaluatorName = fe.fuzzyEvaluatorName;
		type = FUZZY_EVALUATOR;
		if (fe.isFuzzyAggregator())
			type = FUZZY_AGGREGATOR;
		else if (fe.isGenericFuzzyEvaluator())
			type = FUZZY_GENERIC_EVALUATOR;
		else if (fe.isCrispEvaluator())
			type = CRISP_EVALUATOR;
		parameters = fe.parameters;
		preCondition = fe.preCondition;
		feInternalClauseList = fe.feInternalClauseList;
		evaluate = fe.evaluate;
		polyline = fe.polyline;
				
		// GB
		fuzzyEvaluatorType = fe.fuzzyEvaluatorType;
		genericEvaluate = fe.genericEvaluate;
		genericPolylines = fe.genericPolylines;
		genericDegrees = fe.genericDegrees;
	}

	@Override
	public int getFunctionEvaluatorType() {
		return type;
	}

	@Override
	public String getFunctionEvaluatorName() {
		return fuzzyEvaluatorName;
	}

	public String getFuzzyEvaluatorType() {
		if (type == FUZZY_AGGREGATOR)
			return "Fuzzy Aggregator";
		return "Fuzzy Evaluator";	
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Condition getPreCondition() {
		return preCondition;
	}

	public List<FEInternalClause> getFeInternalClauseList() {
		return feInternalClauseList;
	}

	public Expression getEvaluate() {
		return evaluate;
	}
	
	public FuzzyPolyline getPolyline() {
		return polyline;
	}
	public boolean hasPolyline() {
		return ((FuzzyEvaluator) instruction).hasPolyline();
	}

	// GB metodi di get generici mancanti 
	public String getFuzzySetModelName() {
		return fuzzyEvaluatorType;
	}
	
	public List<Expression> getEvaluates() {
		return genericEvaluate;
	}
	
	public List<FuzzyPolyline> getPolylines() {
		return genericPolylines;
	}
	
	public List<Parameter> getDegrees() {
		return genericDegrees;
	}
	
	public boolean isGenericEvaluator() {
		return type == FUZZY_GENERIC_EVALUATOR;
	}
	
	public boolean isCrispEvaluator() {
		return type == CRISP_EVALUATOR;
	}
	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
