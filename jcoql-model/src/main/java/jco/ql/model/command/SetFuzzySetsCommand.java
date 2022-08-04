package jco.ql.model.command;

import jco.ql.parser.model.fuzzy.SetFuzzySets;


public class SetFuzzySetsCommand extends SetFuzzySets {

	public SetFuzzySetsCommand (SetFuzzySets sfs) {
		policyType = sfs.policyType;
		policyStr = sfs.policyStr;
		setType = sfs.setType;
		setByKeepStr = sfs.setByKeepStr;
		fuzzySetsList = sfs.fuzzySetsList;
	}

	
	public String getName () {
    	return "Set Fuzzy Sets";
    }

}
