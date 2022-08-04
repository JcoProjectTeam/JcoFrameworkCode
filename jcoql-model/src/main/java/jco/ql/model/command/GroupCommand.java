package jco.ql.model.command;

import java.util.List;

import jco.ql.parser.model.Group;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.util.Partition;

public class GroupCommand implements ICommand {
	private Instruction instruction = null;	
	private List<Partition> partitions;
	private boolean keepOthers;
	
	public GroupCommand(Group group) {
		instruction = group;
		partitions = group.partitions;

		keepOthers = group.isKeepOthers();
	}

	public List<Partition> getPartitions() {
		return partitions;
	}

	public boolean isKeepOthers() {
		return keepOthers;
	}
	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
