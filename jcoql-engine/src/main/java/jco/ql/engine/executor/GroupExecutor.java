package jco.ql.engine.executor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.byZunEvaluator.ConditionEvaluator;
import jco.ql.engine.byZunEvaluator.GenerateEvaluator;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.threads.Primer;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.GroupCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Field;
import jco.ql.parser.model.util.GenerateSection;
import jco.ql.parser.model.util.Partition;
import jco.ql.parser.model.util.SortField;


@Executor(GroupCommand.class)
public class GroupExecutor implements IExecutor<GroupCommand>, JCOConstants {

	@Override
	public void execute(Pipeline pipeline, GroupCommand command) throws ExecuteProcessException {
		DocumentDefinition curDoc;
		IDocumentCollection collection = pipeline.getCurrentCollection();
		
		IDocumentCollection unselectedDocCollection = new SimpleDocumentCollection();
		// for each partition there's a collection map where the key is constituted by the hashmap of the sub-document made with grouping fields
		List<Hashtable<Integer, DocumentDefinition>> partitionCollection = new ArrayList<Hashtable<Integer, DocumentDefinition>>();
		for (int p=0; p<command.getPartitions().size(); p++)
			partitionCollection.add(new Hashtable<Integer, DocumentDefinition>(Primer.getNextRecursivelyFast(collection.getDocumentList().size())));

		if (collection.getDocumentList().size() > 100)
			ZunTicker.reset("Group", collection.getDocumentList().size(), 10000);
		else
			ZunTicker.reset("Group", collection.getDocumentList().size(), collection.getDocumentList().size());
		
		int i=0;
		Pipeline groupPipeline = new Pipeline (pipeline);
    	while (i < collection.getDocumentList().size()) {												// for each document
    		Boolean unselectedDoc = true;
    		curDoc = collection.getDocumentList().get(i);
    		groupPipeline.setCurrentDoc(curDoc);

    		for (int p=0; p<command.getPartitions().size(); p++) {											// for each partition    			
    			Partition partition = command.getPartitions().get(p);
    			if (ConditionEvaluator.matchCondition(partition.whereCondition, groupPipeline)) {				// if doc match a partition
    				unselectedDoc = false;
    				DocumentDefinition groupDoc = getGroupDoc (partition, curDoc);

    				if (groupDoc != null) {
    					groupDoc = checkPartitionCollection (partitionCollection.get(p), groupDoc, partition.into);
    	
    					// remove grouping fields
    					if (partition.dropGroupingFields)
    						for (Field f : partition.by)
    							curDoc.removeValue(f.toString());

    					insertDocInGroupArray (partition, groupDoc, curDoc);
    				} 
    				break;																							// exit for after 1st partition matching
    			}																								// end if doc match a partition
			}																								// end for each partitito
    		if (unselectedDoc && command.isKeepOthers())
    			unselectedDocCollection.addDocument(curDoc);    		
    		i++;
    	}																								// end of while (for each document)
		
    	// apply generate clause for each partition if there's any
		for (int p=0; p<command.getPartitions().size(); p++) {
			Partition partition = command.getPartitions().get(p);
			if (partition.hasGenerateSection()) {
				// apply GenerateSection in each document partition
				GenerateSection gs = partition.generateSection;
				Pipeline generatePipeline = new Pipeline (pipeline);
				Hashtable<Integer, DocumentDefinition> hashtable = partitionCollection.get(p);	
				Enumeration<Integer> eInt = hashtable.keys();
				while (eInt.hasMoreElements()) {
					int hashcode = eInt.nextElement();
					DocumentDefinition genDoc = hashtable.get(hashcode);
					generatePipeline.setCurrentDoc(genDoc);
					genDoc = GenerateEvaluator.evaluate(generatePipeline, gs);
					if (genDoc == null)
						hashtable.remove(hashcode);
					else
						hashtable.put(hashcode, genDoc);
				}				
			}			
		}
		
		IDocumentCollection outCollection = new SimpleDocumentCollection(GROUP_COLLECTION_NAME);
    	// merge partitionCollection and unselectedDocCollection
		for (int p=0; p<command.getPartitions().size(); p++) {
			Hashtable<Integer, DocumentDefinition> hashtable = partitionCollection.get(p);
			Enumeration<DocumentDefinition> docEnumeration = hashtable.elements();
			while (docEnumeration.hasMoreElements()) {
				DocumentDefinition doc = docEnumeration.nextElement();
				outCollection.addDocument(doc);
			}
		}
		outCollection.getDocumentList().addAll(unselectedDocCollection.getDocumentList());
		pipeline.addCollection(outCollection);
				
	}	

		
	// insert curDoc into the arrayField in groupDoc, considering sort order if there's one. The doc if uncomparable could be discarded
	private void insertDocInGroupArray(Partition partition, DocumentDefinition groupDoc, DocumentDefinition doc) {
		ArrayValue array = groupDoc.getArrayValue(partition.into.toString());
		DocumentValue docValue = new DocumentValue (doc);
		if (!partition.hasSortedBy())
			array.getValues().add(docValue);

		else if (isComparable(partition, doc)) 
			insertDocInArray (partition, groupDoc, doc); 
	
		else if (partition.hasKeepUncomparable())
			insertDocInArray (partition, groupDoc, doc); 
	}


	// docs are inserted for sure according order
	private void insertDocInArray(Partition partition, DocumentDefinition groupDoc, DocumentDefinition doc) {
		ArrayValue array = groupDoc.getArrayValue(partition.into.toString());
		DocumentValue docValue = new DocumentValue (doc);
		
		// insert 1st element
		if (array.getValues().size() == 0)
			array.getValues().add(docValue);
		else {
			boolean inserted = false;
			for (int i=0; i< array.getValues().size(); i++) {
				DocumentDefinition actualDoc = ((DocumentValue)array.getValues().get(i)).getDocument();
				if (putDoc (partition, actualDoc, doc)) {
					inserted = true;
					array.getValues().add(i, docValue);
					break;
				}
			}

			// insert in tail
			if (!inserted)
				array.getValues().add(docValue);
		}
		
	}

	
	private boolean putDoc(Partition partition, DocumentDefinition actualDoc, DocumentDefinition doc) {
		for (SortField sf : partition.sortedBy) {
			FieldDefinition fd = doc.getField(sf.field);
			if (fd == null || JCOValue.isNull(fd.getValue()))
				return false;
			JCOValue v = fd.getValue();
			if (!(	(sf.fieldType == SortField.NUMERIC 	&& JCOValue.isNumericValue(v))	||
					(sf.fieldType == SortField.STRING 	&& JCOValue.isStringValue(v))	||	
					(sf.fieldType == SortField.BOOLEAN 	&& JCOValue.isBooleanValue(v))	)	)
				return false;																	// the new doc is uncomparable and insertion is postponed

			FieldDefinition fdActual = actualDoc.getField(sf.field);
			if (fdActual == null || JCOValue.isNull(fdActual.getValue()))
				return true;
			JCOValue vActual = fdActual.getValue();
			if (!(	(sf.fieldType == SortField.NUMERIC 	&& JCOValue.isNumericValue(vActual))	||
					(sf.fieldType == SortField.STRING 	&& JCOValue.isStringValue(vActual))	||	
					(sf.fieldType == SortField.BOOLEAN 	&& JCOValue.isBooleanValue(vActual))	)	)
				return true;																	// the acutal doc is uncomparable so the new doc is inserted
			
			SimpleValue sv = (SimpleValue) v;
			SimpleValue svActual = (SimpleValue) vActual;
			if (sv.compareTo(svActual) == GREATER_THAN)
				if (sf.versus == SortField.ASCENDING)
					return false;
				else
					return true;

			else if (sv.compareTo(svActual) == LESS_THAN)
				if (sf.versus == SortField.ASCENDING)
						return true;
				else
					return false;
		}
		// if all compare keys are equal, the new doc is inserted before the actual one
		return true;
	}


	private boolean isComparable (Partition partition, DocumentDefinition doc) {
		for (SortField sf : partition.sortedBy) {
			FieldDefinition fd = doc.getField(sf.field);
			if (fd == null || JCOValue.isNull(fd.getValue()))
				return false;
			JCOValue v = fd.getValue();
			if (!(	(sf.fieldType == SortField.NUMERIC 	&& JCOValue.isNumericValue(v))	||
					(sf.fieldType == SortField.STRING 	&& JCOValue.isStringValue(v))	||	
					(sf.fieldType == SortField.BOOLEAN 	&& JCOValue.isBooleanValue(v))	)	)
				return false;
		}
		return true;
	}
	
	
	// retrive a subdocument according partition grouping fields. if a grouping field is missing return null
	private DocumentDefinition getGroupDoc(Partition partition, DocumentDefinition curDoc) {
		DocumentDefinition keyDoc = new DocumentDefinition();
		
		for (Field f : partition.by) {
			FieldDefinition fd = curDoc.getField(f);
			if (JCOValue.isNull(fd.getValue()))
				return null;
			keyDoc.insertField(f, fd.getValue());
		}
		return keyDoc;
	}


	// if the doc is already in the partition collection, return the doc in the collection, otherwise insert the new doc in the collection adding an arrayfield name as reported in "into"
	private DocumentDefinition checkPartitionCollection(Hashtable<Integer, DocumentDefinition> hashtable, DocumentDefinition doc, Field into) {
		int hashCode = doc.hashCode();
		if (hashtable.containsKey(hashCode))
			return hashtable.get(hashCode);

		// the hash code is evaluated before adding the array field
		ArrayValue array = new ArrayValue();
		doc.insertField(into, array);
		hashtable.put(hashCode, doc);

		return doc;
	}

}
