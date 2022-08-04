package jco.ql.model;
// ZUN CHECK* può essere eliminata?
public enum EOperator {
	EQUALS("="),
	GREATER_THAN(">"),
	GREATER_EQUAL(">="),
	LESS_THAN("<"),
	LESS_EQUAL("<="),
	NOT_EQUALS("!=");
	
	private String operator;
	
	private EOperator(String operator) {
		this.operator = operator;
	}
	
	public String operator() {
		return operator;
	}
	
	public EOperator fromString(String operatorString) {
		EOperator operator = null;
		for(EOperator op : values()) {
			if(op.operator().equals(operatorString)) {
				operator = op;
				break;
			}
		}
		return operator;
	}
	
	@Override
	public String toString() {
		return operator;
	}

}
