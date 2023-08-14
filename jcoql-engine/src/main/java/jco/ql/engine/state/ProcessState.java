package jco.ql.engine.state;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.command.FunctionCommand;
import jco.ql.model.command.FuzzyFunctionCommand;
import jco.ql.model.command.FuzzySetModelCommand;
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

	// variabili per lo stato Create Fuzzy Function (Operator, Aggregator, Generic Operator)
	private boolean isCreateFuzzyFunction;
	
	// added by Balicco 27/1/23
	private boolean isCreateFuzzySetModel;

	// variabili per lo stato Create JS/Java Function
	private boolean isCreateUserFunction;


	public ProcessState() {
		currentCollection = new SimpleDocumentCollection();
		istruction = "";
		emptyState = true;
		useDb = false;
		setIntermediateAs = false;
		isCreateFuzzyFunction = false;
		isCreateUserFunction = false;
	}

	public ProcessState(IDocumentCollection coll) {
		this ();
		currentCollection = coll;
	}

	public ProcessState(FuzzyFunctionCommand fuzzyOp, IDocumentCollection coll) {
		this();
		isCreateFuzzyFunction = true;
		currentCollection = coll;
	}
	
	// added by Balicco 27/1/2023
	public ProcessState(FuzzySetModelCommand fuzzyAg, IDocumentCollection coll) {
		this();
		currentCollection = coll;
		isCreateFuzzySetModel = true;
	}
	

	public ProcessState(FunctionCommand userFunction, IDocumentCollection coll) {
		this();
		isCreateUserFunction = true;
		currentCollection = coll;
	}

	// ------------------------------------------------
	
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

	public boolean isCreateFuzzyFunction() {
		return isCreateFuzzyFunction;
	}
	
	// added by Balicco 27/1/2023
	public boolean isCreateFuzzySetType() {
		return isCreateFuzzySetModel;
	}
	
	public boolean isCreateUserFunction() {
		return isCreateUserFunction;
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
