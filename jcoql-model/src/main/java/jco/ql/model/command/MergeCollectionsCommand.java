package jco.ql.model.command;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.reference.CollectionReference;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.MergeCollections;
import jco.ql.parser.model.util.DbCollection;

public class MergeCollectionsCommand implements ICommand {
	private Instruction instruction = null;	
	private List<CollectionReference> collections;
	private boolean removeDuplicates;
	
	public MergeCollectionsCommand (MergeCollections mc) {
		instruction = mc;

		collections = new ArrayList<CollectionReference>();		
		for (DbCollection coll : mc.collectionList)
			collections.add(new CollectionReference (coll));

		removeDuplicates = mc.removeDuplicates;
	}

	
	public List<CollectionReference> getCollections() {
		return collections;
	}

	
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

	
	@Override
	public Instruction getInstruction() {
		return instruction;
	}

}
