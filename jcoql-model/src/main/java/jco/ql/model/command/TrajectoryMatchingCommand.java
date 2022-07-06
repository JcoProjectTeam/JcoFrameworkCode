package jco.ql.model.command;

import java.util.List;

import jco.ql.model.reference.CollectionReference;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.TrajectoryMatching;
import jco.ql.parser.model.util.TrajectoryPartition;

public class TrajectoryMatchingCommand implements ICommand {
	private Instruction instruction = null;
	private CollectionReference targetCollection;
	private CollectionReference inputCollection;
	private List<TrajectoryPartition> partitions;
	private boolean keepOthers;
	
	
	public TrajectoryMatchingCommand (TrajectoryMatching tm) {
		instruction = tm;
		targetCollection = new CollectionReference (tm.collection1);
		inputCollection = new CollectionReference (tm.collection2);
		partitions = tm.partitions;
		keepOthers = tm.isKeepOthers();
	}

	public CollectionReference getTargetCollection() {
		return targetCollection;
	}

	public CollectionReference getInputCollection() {
		return inputCollection;
	}
	
	public List<TrajectoryPartition> getPartitions() {
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
