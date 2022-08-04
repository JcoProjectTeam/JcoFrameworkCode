package jco.ql.engine.executor.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Unpack;

public class SynchronizedExpandCycle extends Thread implements JCOConstants {

	private int id, nThreads;
	private LinkedBlockingQueue<DocumentDefinition> queue;

	private Unpack unpack;
	private List<JCOValue> listValues; 
	private List<FieldDefinition> outputList;

	public SynchronizedExpandCycle(int id, int nThread, LinkedBlockingQueue<DocumentDefinition> queue, Unpack unpack, List<JCOValue> listValues, List<FieldDefinition> outputList) {
		this.id = id;
		this.nThreads = nThread;
		this.queue = queue;
		setPriority(10);

		this.unpack = unpack; 
		this.listValues = listValues;
		this.outputList = outputList;
	}

	
    @Override
    public void run() {
    	// fundamental
    	int index  = id;   				
    	
    	while (index < listValues.size()) {
			DocumentDefinition newDoc = new DocumentDefinition(outputList);
			JCOValue v = listValues.get(index);
			List<FieldDefinition> arrayElements = new ArrayList<FieldDefinition>();
			arrayElements.add(new FieldDefinition(POSISTION_FIELD_NAME, new SimpleValue(index+1)));
			arrayElements.add(new FieldDefinition(ITEM_FIELD_NAME, v));
			DocumentValue arrayField = new DocumentValue(new DocumentDefinition(arrayElements));

			newDoc.insertField(unpack.to, arrayField);

			if(newDoc != null) {
				try {
					queue.put(newDoc);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
	            	throw new ExecuteProcessException("[SynchronizedFilterCycle]: terminated");
				}
			}

			// fundamental
			index += nThreads;  
    	}
    }


}
