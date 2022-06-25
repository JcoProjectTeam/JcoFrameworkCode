package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.reference.CollectionReference;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.SubtractCollections;

public class SubtractCollectionsCommand implements ICommand {
	private Instruction instruction = null;	
	private List<CollectionReference> collections;
	
	public SubtractCollectionsCommand(SubtractCollections sc) {
		instruction = sc;

		collections = new ArrayList<CollectionReference>();
		collections.add(new CollectionReference(sc.collection1));
		collections.add(new CollectionReference(sc.collection2));
	}


	public List<CollectionReference> getCollections() {
		return collections;
	}

	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
