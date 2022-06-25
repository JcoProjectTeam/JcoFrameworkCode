package jco.ql.model.command;

import jco.ql.model.engine.JCOConstants;
import jco.ql.parser.model.condition.Condition;

// ZUN CHECK* eliminare
public class FuzzySetDefinitionCommand implements JCOConstants {

    private String fuzzySetName;
    private Condition foUsingCond;

    public FuzzySetDefinitionCommand(String fuzzySetName, Condition foUsingCond) {
        this.fuzzySetName = fuzzySetName;
        this.foUsingCond = foUsingCond;
    }

    public Condition getFoUsingCond() {
        return foUsingCond;
    }

    public String getFuzzySetName() {
        return fuzzySetName;
    }

    public String getName () {
    	// PF. Check
    	return "GenerateFuzzy";
    }

}
