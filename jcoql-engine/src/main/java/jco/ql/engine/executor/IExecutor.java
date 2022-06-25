package jco.ql.engine.executor;

import javax.script.ScriptException;

import jco.ql.engine.Pipeline;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.ICommand;

public interface IExecutor<C extends ICommand> {
	
	public void execute(Pipeline pipeline, C command) throws ExecuteProcessException, ScriptException;

}
