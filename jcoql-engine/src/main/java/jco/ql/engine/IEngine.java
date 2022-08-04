package jco.ql.engine;

import java.util.List;

import javax.script.ScriptException;

import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.command.ICommand;

public interface IEngine {
	
	void execute(List<ICommand> commands) throws ExecuteProcessException, ScriptException;
	
	void execute(ICommand command) throws ExecuteProcessException, ScriptException;
}
