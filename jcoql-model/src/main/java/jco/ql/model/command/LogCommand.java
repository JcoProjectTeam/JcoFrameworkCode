package jco.ql.model.command;

import jco.ql.parser.model.Instruction;

public class LogCommand implements ICommand {
	private Instruction instruction = null;

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
