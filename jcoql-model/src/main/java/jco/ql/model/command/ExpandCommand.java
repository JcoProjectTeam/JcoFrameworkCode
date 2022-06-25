package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.Expand;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.util.Unpack;

public class ExpandCommand implements ICommand {
	private Instruction instruction = null;
	private List<Unpack> unpack;
	private boolean keepOthers;
	
	public ExpandCommand (Expand e) {
		instruction = e;
		unpack = e.unpacks;
		keepOthers = e.isKeepOthers();
		
	}

	public List<Unpack> getUnpack() {
		return unpack;
	}

	public boolean isKeepOthers() {
		return keepOthers;
	}
    
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
