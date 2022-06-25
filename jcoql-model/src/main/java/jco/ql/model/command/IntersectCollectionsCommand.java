package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.reference.CollectionReference;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.IntersectCollections;

public class IntersectCollectionsCommand implements ICommand {
	private Instruction instruction = null;	
	private List<CollectionReference> collections;
	
	public IntersectCollectionsCommand(IntersectCollections ic) {
		instruction = ic;
		
		collections = new ArrayList<>();
		collections.add(new CollectionReference(ic.collection1));
		collections.add(new CollectionReference(ic.collection2));

	}
	public IntersectCollectionsCommand(List<CollectionReference> collections) {
		this.collections = collections;
	}

	public List<CollectionReference> getCollections() {
		return collections;
	}

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
