package jco.ql.model;

import java.util.List;

import jco.ql.parser.model.util.CaseClause;
import jco.ql.parser.model.util.WhereCase;

public class Case {
	private List<WhereCase> whereCases;
	private boolean keepOthers;


	public Case(CaseClause caseClause) {
		keepOthers = (caseClause.othersType == CaseClause.OTHERS_KEEP);
		whereCases = caseClause.whereList;
	}

	public List<WhereCase> getWhereConditions() {
		return whereCases;
	}

	public boolean isKeepOthers() {
		return keepOthers;
	}

}
