package jco.ql.model.command;

import jco.ql.model.Case;
import jco.ql.parser.model.Filter;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.util.GenerateSection;

public class FilterCommand implements ICommand {
	private Instruction instruction = null;
	private Case caseFilter;
	private GenerateSection generateSection;
	private boolean removeDuplicates;

	
	public FilterCommand(Filter filterInstr) {
		instruction = filterInstr;
		caseFilter = null;
		generateSection = null;
		if (filterInstr.isCaseFilter())
			caseFilter = new Case (filterInstr.caseClause);
		else
			generateSection = filterInstr.generateSection;
		removeDuplicates = filterInstr.removeDuplicates;
	}

	
	public Case getCaseFilter() {
		return caseFilter;
	}
	public GenerateSection getGenerateSection() {
		return generateSection;
	}

	
	public boolean hasCaseFilter() {
		return caseFilter != null;
	}
	public boolean hasGenerateSection() {
		return generateSection != null;
	}
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
