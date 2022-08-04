package jco.ql.model.command;

import jco.ql.parser.model.util.AddField;

import java.util.List;

import jco.ql.model.Case;
import jco.ql.model.reference.CollectionReference;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.JoinCollections;
import jco.ql.parser.model.util.SpatialFunction;

public class JoinCommand implements ICommand {
	private Instruction instruction = null;
	private CollectionReference leftCollection;
	private CollectionReference rightCollection;
	private SpatialFunction onGeometryCondition;
	private int setGeometryOperation;
	private List<AddField> addFields;
	private SetFuzzySetsCommand setFuzzySets;
	private Case caseFilter;
	private boolean removeDuplicates;

	
	public JoinCommand (JoinCollections jc) {
		instruction = jc;		
		leftCollection = new CollectionReference(jc.collection1);
		rightCollection = new CollectionReference(jc.collection2);
		
        onGeometryCondition = jc.on;
        setGeometryOperation = jc.setGeometry;
        addFields = jc.addFields; 
		
        setFuzzySets = null;
		if (jc.hasSetFuzzySets())
			setFuzzySets = new SetFuzzySetsCommand(jc.setFuzzySets);
		
		caseFilter = null;
		if (jc.hasCaseClause())
			caseFilter = new Case(jc.caseClause);

		removeDuplicates = jc.isRemoveDuplicates();
	}


	public CollectionReference getLeftCollection() {
		return leftCollection;
	}


	public CollectionReference getRightCollection() {
		return rightCollection;
	}

	
	public SpatialFunction getOnGeometryCondition() {
		return onGeometryCondition;
	}

	
	public int getSetGeometryOperation() {
		return setGeometryOperation;
	}

	
	public Case getCaseFilter() {
		return caseFilter;
	}

	
	public List<AddField> getAddField() {
		return addFields;
	}

	
	public SetFuzzySetsCommand getSetFuzzySetsCommand () {
		return setFuzzySets;
	}

	
	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

    @Override
	public Instruction getInstruction() {
		return instruction;
	}

}
