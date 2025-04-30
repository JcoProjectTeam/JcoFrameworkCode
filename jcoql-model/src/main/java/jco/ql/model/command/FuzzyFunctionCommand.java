package jco.ql.model.command;

public interface FuzzyFunctionCommand {
	public static int UNDEFINED_FUNCTION  	= -1;
	public static int OPERATOR  			= 0;
	public static int GENERIC_OPERATOR  	= 1;
	public static int AGGREGATOR  			= 2;
	public static int EVALUATOR  			= 3;
	public static int GENERIC_EVALUATOR		= 4;
		

	public int getType ();
	public String getFuzzyFunctionName ();
}
