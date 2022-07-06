package jco.ql.model.command;

import jco.ql.model.Case;
import jco.ql.parser.model.Filter;
import jco.ql.parser.model.Instruction;

public class FilterCommand implements ICommand {
	private Instruction instruction = null;
	private Case caseFilter;
	private boolean removeDuplicates;

	
	public FilterCommand(Filter filterInstr) {
		instruction = filterInstr;
		removeDuplicates = filterInstr.removeDuplicates;
		caseFilter = new Case (filterInstr.caseClause);
	}

	
	public Case getCaseFilter() {
		return caseFilter;
	}

	
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
