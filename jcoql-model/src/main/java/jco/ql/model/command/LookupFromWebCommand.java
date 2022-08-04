package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.LookupFromWeb;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.util.ForEach;

public class LookupFromWebCommand implements ICommand {
	private Instruction instruction = null;
	private List<ForEach> forEachList;

	
	public LookupFromWebCommand(LookupFromWeb gfw) {
		instruction = gfw;
		forEachList = gfw.getForEachList();
	}

	
	public List<ForEach> getForEachList() {
		return forEachList;
	}

	
	public ForEach getForEach(int i) {
		if (i < forEachList.size())
			return forEachList.get(i);
		return null;
	}

	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
