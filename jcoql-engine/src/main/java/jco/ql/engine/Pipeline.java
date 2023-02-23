package jco.ql.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.json.JSONHandler;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.engine.state.ProcessState;
import jco.ql.model.Dictionary;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FunctionCommand;
import jco.ql.model.command.FuzzyAggregatorCommand;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;

import java.util.TreeMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Pipeline implements  JCOConstants {
	// Collections
	private List<IDocumentCollection> collections;

	// TODO Eliminare PF
	private Map<String, Object> objects;

	// Intermediate Files
	private Map<String, String> setIntermediateFiles;

	// Current Collection
	private IDocumentCollection currentCollection;

	// Current Collection name
	private String currentCollectionName;

	private JSONHandler jsonHandler;

	private LinkedList<ProcessState> state;

	// contiene le istruzioni che sono state inviate dall'utente. Serve per il
	// messaggio "##GET-PROCESS##"
	// ZUN CHECK* sostituire String con Instruction
	private LinkedList<String> istructions;


	// Fuzzy Operator List
	private Hashtable<String, FuzzyOperatorCommand> fuzzyOperators;
	// JavaScript Function List
	private Hashtable<String, FunctionCommand> jsFunctions;

	private LinkedList<ScriptEngine> jsEngines;
	private int currentThread = -1;


	// PF. added on 22.07.2021
	private Map<String, Dictionary> dictionaries;
	// PF. added on 22.07.2021
	private DocumentDefinition currentDoc;
	
	//FI added on 27.10.2022
	private LinkedList<FuzzyAggregatorCommand> fuzzyAggregators;
	private int fuzzyAggregatorIndex;


	public Pipeline() {
		currentCollection = new SimpleDocumentCollection("empty");
		setIntermediateFiles = new TreeMap<String, String>();
		collections = new LinkedList<>();
		istructions = new LinkedList<>();
		jsonHandler = new JSONHandler();

		state = new LinkedList<>();
		//creo lo stato iniziale
		ProcessState emptyState = new ProcessState();
		state.add(emptyState);

		fuzzyOperators = new Hashtable<String, FuzzyOperatorCommand>();
		jsFunctions = new Hashtable<String, FunctionCommand>();
		jsEngines = new LinkedList<>();
		for (int i=0; i< (2*EngineConfiguration.getInstance().getNProcessors());i++) {
				ScriptEngineManager manager = new ScriptEngineManager();
	//	        ScriptEngine engine = manager.getEngineByName("JavaScript");
	        ScriptEngine engine = manager.getEngineByName("nashorn");
	        jsEngines.add(engine);
		}
		currentThread = -1;
		
		// PF. added on 22.07.2021
		dictionaries = new TreeMap<String, Dictionary>();
		currentDoc = null;
		
		//FI added on 27.10.2022
		fuzzyAggregators = new LinkedList<>();
	}
	public Pipeline(Pipeline sourcePipeline, int currentThread) {
		retrieveEnvironment (sourcePipeline);
		currentCollection = new SimpleDocumentCollection("empty");
		setIntermediateFiles = new TreeMap<String, String>();
		collections = new LinkedList<>();
		istructions = new LinkedList<>();
		jsonHandler = new JSONHandler();
		currentDoc = null;
		this.currentThread = currentThread;
	}
	public Pipeline(Pipeline sourcePipeline ) {
		this (sourcePipeline, -1);
	}
	public ScriptEngine getEngine () {
		if (currentThread == -1)
			return jsEngines.get(0);
		return jsEngines.get(currentThread);			
	}

	// PF. added on 26.07.2021
	private void retrieveEnvironment (Pipeline sourcePipeline) {
		this.state = sourcePipeline.state;
		this.fuzzyOperators = sourcePipeline.fuzzyOperators;
		this.jsFunctions = sourcePipeline.jsFunctions;
		this.dictionaries = sourcePipeline.dictionaries;		
		this.jsEngines = sourcePipeline.jsEngines;
		//FI added on 27.10.2022
		this.fuzzyAggregators = sourcePipeline.fuzzyAggregators;
		this.fuzzyAggregatorIndex = sourcePipeline.fuzzyAggregatorIndex;
	}

	
	private Map<String, Object> getObjects() {
		if (objects == null) {
			objects = new TreeMap<String, Object>();
		}
		return objects;
	}


	public void setCurrentDoc (DocumentDefinition currentDoc) {
		this.currentDoc = currentDoc;
	}
	public DocumentDefinition getCurrentDoc () {
		return currentDoc;
	}
	
	public void add(Object object, String alias) {
		getObjects().put(alias, object);
		this.currentCollectionName = alias;
	}

	public Object get(String alias) {
		return getObjects().get(alias);
	}

	public DocumentDefinition getAsDocument() {
		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
		for (Entry<String, Object> entry : objects.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			JCOValue value = null;

			if (val instanceof DocumentDefinition) {
				value = new DocumentValue((DocumentDefinition) val);
			}

			if (value != null) {
				fields.add(new FieldDefinition(key, value));
			}
		}
		return new DocumentDefinition(fields);
	}

	public void addCollection(IDocumentCollection collection) {
		collections.add(collection);
		currentCollection = collection;
		ProcessState s = new ProcessState(collection);
		//commenta!
		s.setIstruction(istructions.removeFirst());
		state.add(s);
	}


	//in questo caso lo stato di USEDB
	public void addCollection(List<String> dbnames) {
		ProcessState s = new ProcessState();
		s.setUsedb(dbnames);
		s.setIstruction(istructions.removeFirst());
		state.add(s);
	}

	
	//in questo caso lo stato � SET INTERMEDIATE AS
	public void addCollection(String collectionAlias, String fileName) {
		ProcessState s = new ProcessState();
		s.setSetIntermediateAs(collectionAlias, fileName);
		s.setIstruction(istructions.removeFirst());
		state.add(s);
	}

	
	// salva il nuovo FUZZY OPERATOR
	public void addFuzzyOperator(FuzzyOperatorCommand fuzzyOp) {
		ProcessState s = new ProcessState(fuzzyOp, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		fuzzyOperators.put(fuzzyOp.getFuzzyOperatorName(), fuzzyOp);
	}
	// aggiorna il FUZZY OPERATOR
	// TODO eliminare
	public void updateFuzzyOperator(FuzzyOperatorCommand fuzzyOp) {
		ProcessState s = new ProcessState(fuzzyOp, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		fuzzyOperators.put(fuzzyOp.getFuzzyOperatorName(), fuzzyOp);
	}

	// salva la nuova USER FUNCTION
	public void addUserFunction(FunctionCommand jsFun) {
		ProcessState s = new ProcessState(jsFun, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		jsFunctions.put(jsFun.getFunctionName(), jsFun);
	}
	// TODO eliminare
	// aggiorna la USER FUNCTION
	public void updateUserFunction(FunctionCommand jsFun) {
		ProcessState s = new ProcessState(jsFun, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		jsFunctions.put(jsFun.getFunctionName(), jsFun);
	}

	public IDocumentCollection getCollection(String alias) {
		// Se è temporary restituisci currentCollection
		if (alias.equals(TEMPORARY_COLLECTION_NAME)) 
			return currentCollection;
		else if (setIntermediateFiles.get(alias) != null) 			
			return jsonHandler.createCollection(alias, setIntermediateFiles.get(alias));
		else {
			// devo scorrere la lista degli stati
			for (ProcessState s : state) {
				if (s.getCollection() != null && 
					s.getCollection().getName() != null && 
					s.getCollection().getName().equals(alias)) {
					return s.getCollection();
				}
			}
		}
		// la collection non esiste da nessuna parte
		List<DocumentDefinition> list = new ArrayList<>();
		IDocumentCollection collection = new SimpleDocumentCollection(alias, list);
		JMH.addIOMessage("[GC] Collection " + alias + " does not exist:\n" + collection.toString()); 
		return collection;
	}

	public IDocumentCollection getCurrentCollection() {
		return currentCollection;
	}


	public String getCurrentCollectionName() {
		return currentCollectionName;
	}

	public void addFiles(String collectionName, String fileName) {
		setIntermediateFiles.put(collectionName, fileName);
	}

	public void setCurrentCollectionName(String alias) {
		currentCollectionName = alias;
	}

	// TODO rivedere tutta la politica del backtrack
	public void backtrack(DatabaseRegistry registry) {
		if (!(state.getLast().isEmptyState())) {
			if (state.getLast().isUseDb()) {
				for(String name: state.getLast().getDbNames())
					registry.deleteDatabase(name);
			} else if (state.getLast().isSetIntermediateAs()) {
				File file = new File(state.getLast().getFileName());
		        file.delete();
				setIntermediateFiles.remove(state.getLast().getCollectionAlias());
			} else if(state.getLast().isCreateFuzzyOperator()) {
			// TODO PF	2023.01
//				fuzzyOperators.removeLast();
			} else if(state.getLast().isCreateJsFunction()) {
				// TODO PF	2023.01
//				jsFunctions.removeLast();
			}
			// rimuovo l'ultimo stato
			state.removeLast();
			// aggiorno la collezione corrente
			currentCollection = state.getLast().getCollection();
		} else
			throw new ExecuteProcessException("[BACKTRACK]: empty state, cannot go back anymore");
	}

	public List<IDocumentCollection> getListCollections() {
		return collections;
	}

	public void setIstructions(List<String> istr) {
		istructions = new LinkedList<>();
		istructions.addAll(istr);
	}

	public LinkedList<String> getIstructions() {
		LinkedList<String> istr = new LinkedList<>();
		for(ProcessState s: state) {
			if(!(s.isEmptyState()))
				istr.add(s.getIstruction());
		}
		return istr;
	}

	public Collection<String> getIRList() {
		return setIntermediateFiles.keySet();
	}

	public IDocumentCollection getIRCollection(String collectionName) {
		if (setIntermediateFiles.get(collectionName) != null)
			return jsonHandler.createCollection(collectionName, setIntermediateFiles.get(collectionName));
		else
			throw new ExecuteProcessException(
					"[GET IR COLLECTION]: IR collection " + collectionName + " does not exits");
	}

	public Hashtable<String, FunctionCommand> getJsFunctions() {
		return jsFunctions;
	}

	public Hashtable<String, FuzzyOperatorCommand> getFuzzyOperators() {
		return fuzzyOperators;
	}

	// PF. added on 22.07.2021
	public void addDictionary(IDocumentCollection collection, String dictionaryNane) {
		String key, value;
		Dictionary dictionary = new Dictionary(dictionaryNane);
		dictionaries.put(dictionaryNane, dictionary);
		Dictionary dictionaryNoCase = new Dictionary(dictionaryNane);
		dictionaries.put(dictionaryNane + CASE_UNSENSITIVE_SUFFIX, dictionaryNoCase);

		for (DocumentDefinition dd:collection.getDocumentList()) {
			key = null;
			value = null;
			if (dd.getValue("key") != null && dd.getValue("key").getType() == EValueType.STRING)
				key = dd.getValue("key").getStringValue();
			if (dd.getValue("value") != null && dd.getValue("value").getType() == EValueType.STRING) 
				value = dd.getValue("value").getStringValue();
			if (key != null && value != null) {
				dictionary.put(key, value);
				dictionaryNoCase.put(key.toLowerCase(), value);
			}
		}
		
	}
	// PF. added on 22.07.2021
	public String getDictionaryValue (String dictionary, JCOValue key, boolean caseSensitive) {
		if ((dictionary == null) || (key == null) || !dictionaries.containsKey(dictionary))
			return null;
		if (caseSensitive)
			return dictionaries.get(dictionary).get(key.getStringValue());
		return dictionaries.get(dictionary + CASE_UNSENSITIVE_SUFFIX).get(key.getStringValue().toLowerCase());		
	}
	public  Map<String, Dictionary> getDictionaries () {
		return dictionaries;
	}
	public void setDictionaries (Map<String, Dictionary> dictionaries) {
		this.dictionaries = dictionaries;
	}
	
	public LinkedList<FuzzyAggregatorCommand> getFuzzyAggregators(){
		return fuzzyAggregators;
	}
	
	//FI added on 27.10.2022
	public void addFuzzyAggregator(FuzzyAggregatorCommand fuzzyAg) {
		ProcessState s = new ProcessState(fuzzyAg, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		fuzzyAggregators.add(fuzzyAg);
	}
	
	public void updateFuzzyAggregator(FuzzyAggregatorCommand fuzzyAg, int ndx) {
		ProcessState s = new ProcessState(fuzzyAg, state.getLast().getCollection());
		s.setIstruction(istructions.removeFirst());
		state.add(s);
		fuzzyAggregators.set(ndx, fuzzyAg);
	}
	
	public int getFuzzyAggregatorIndex(){
		return fuzzyAggregatorIndex;
	}
	public void setFuzzyAggregatorIndex(int i){
		this.fuzzyAggregatorIndex = i;
	}
	public void incFuzzyAggregatorIndex(int i){
		this.fuzzyAggregatorIndex = this.fuzzyAggregatorIndex + i;
	}

}
