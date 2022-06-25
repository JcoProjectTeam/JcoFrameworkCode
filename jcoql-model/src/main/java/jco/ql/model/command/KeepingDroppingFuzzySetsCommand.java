package jco.ql.model.command;

import java.util.List;
import jco.ql.parser.model.fuzzy.KeepingDroppingFuzzySets;


public class KeepingDroppingFuzzySetsCommand {

    private List<String> fuzzySets;
    private int type;


    public KeepingDroppingFuzzySetsCommand(KeepingDroppingFuzzySets kdfs) {
    	fuzzySets = kdfs.fuzzySets;
    	type = kdfs.type;
    }

    public List<String> getFuzzySets() {
        return fuzzySets;
    }

    public int getType() {
        return type;
    }
}
