package jco.ql.engine.state;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.command.JavascriptFunctionCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.SimpleDocumentCollection;

public class ProcessState {

	private IDocumentCollection currentCollection;
	private String istruction;

	private boolean emptyState;

	//variabili per lo stato "USEDB"
	private boolean useDb;
	private ArrayList<String> dbNames;

	//variabili per lo stato "SET INTERMEDIATE AS"
	private boolean setIntermediateAs;
	private String collectionAlias;
	private String fileName;

	// variabili per lo stato Create Fuzzy Operator
	private boolean isCreateFuzzyOperator;

	// variabili per lo stato Create JS Function
	private boolean isCreateJsFunction;


	public ProcessState() {
		currentCollection = new SimpleDocumentCollection();
		istruction = "";
		emptyState = true;
		useDb = false;
		setIntermediateAs = false;
		isCreateFuzzyOperator = false;
		isCreateJsFunction = false;
	}

	public ProcessState(IDocumentCollection coll) {
		emptyState = false;
		useDb = false;
		setIntermediateAs = false;
		this.currentCollection = coll;
		isCreateFuzzyOperator = false;
		isCreateJsFunction = false;
	}

	public ProcessState(FuzzyOperatorCommand fuzzyOp, IDocumentCollection coll) {
		isCreateFuzzyOperator = true;
		useDb = false;
		emptyState = false;
		setIntermediateAs = false;
		this.currentCollection = coll;
		isCreateJsFunction = false;
		istruction = "";
	}

	public ProcessState(JavascriptFunctionCommand jsFunction, IDocumentCollection coll) {
		isCreateFuzzyOperator = false;
		useDb = false;
		emptyState = false;
		setIntermediateAs = false;
		istruction = "";
		isCreateJsFunction = true;
		this.currentCollection = coll;
	}


	public IDocumentCollection getCollection() {
		return currentCollection;
	}

	public void setUsedb(List<String> dbnames) {
		dbNames = new ArrayList<>();
		emptyState = false;
		useDb = true;
		this.dbNames.addAll(dbnames);
	}

	public void setSetIntermediateAs(String collectionAlias, String fileName) {
		emptyState = false;
		setIntermediateAs = true;
		this.collectionAlias = collectionAlias;
		this.fileName = fileName;
	}

	public boolean isUseDb() {
		return useDb;
	}

	public boolean isSetIntermediateAs() {
		return setIntermediateAs;
	}

	public boolean isCreateFuzzyOperator() {
		return isCreateFuzzyOperator;
	}

	public boolean isCreateJsFunction() {
		return isCreateJsFunction;
	}

	public void setIstruction(String istr) {
		this.istruction = istr;
	}

	public String getIstruction() {
		return istruction;
	}

	public boolean isEmptyState() {
		return emptyState;
	}

	public String getCollectionAlias() {
		return collectionAlias;
	}

	public String getFileName() {
		return fileName;
	}

	public ArrayList<String> getDbNames(){
		return dbNames;
	}
}
