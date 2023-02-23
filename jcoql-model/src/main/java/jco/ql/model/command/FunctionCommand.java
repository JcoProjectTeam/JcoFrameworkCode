package jco.ql.model.command;

public interface FunctionCommand {
	public static int UNDEFINED_FUNCTION  = -1;
	public static int JAVA_FUNCTION  = 0;
	public static int JAVASCRIPT_FUNCTION  = 1;

	public int getType ();
	public String getFunctionName ();
}
