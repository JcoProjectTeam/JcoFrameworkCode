package jco.ql.model.command;

public interface FunctionEvaluatorInterface {
	public static int UNDEFINED_FUNCTION  		= -1;
	public static int JAVA_FUNCTION  			= 0;
	public static int JAVASCRIPT_FUNCTION  		= 1;
	public static int CRISP_EVALUATOR			= 2;
	public static int FUZZY_OPERATOR  			= 10;
	public static int FUZZY_GENERIC_OPERATOR  	= 11;
	public static int FUZZY_AGGREGATOR  		= 12;
	public static int FUZZY_EVALUATOR  			= 13;
	public static int FUZZY_GENERIC_EVALUATOR	= 14;

	public int getFunctionEvaluatorType ();
	public String getFunctionEvaluatorName ();
}
