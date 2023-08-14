package jco.ql.engine.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.evaluator.ConditionEvaluator;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.threads.SynchronizedDuplicateRemover;
import jco.ql.engine.executor.threads.SynchronizedExpandCycle;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.ExpandCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.parser.model.util.Field;
import jco.ql.parser.model.util.Unpack;


@Executor(ExpandCommand.class)
public class ExpandExecutor implements IExecutor<ExpandCommand> {

	@Override
	// PF. New implementation 1
	public void execute(Pipeline pipeline, ExpandCommand command) throws ExecuteProcessException {
		List<Unpack> unpacks = command.getUnpack();
		SimpleDocumentCollection outCollection = new SimpleDocumentCollection("Expand", new ArrayList<DocumentDefinition>());
		LinkedBlockingQueue<DocumentDefinition> queue = new LinkedBlockingQueue<DocumentDefinition>();

		SynchronizedDuplicateRemover sdr = new SynchronizedDuplicateRemover(queue, false);
		sdr.start();		

		// Preconditions to run EXPAND
		if (unpacks != null && pipeline.getCurrentCollection() != null) {
			IDocumentCollection collection = pipeline.getCurrentCollection();

			// for each doc in collection
			for (DocumentDefinition currentDoc: collection.getDocumentList()) {
				Boolean discardedDoc = true;
				Pipeline tempPipeline = new Pipeline(pipeline);
				tempPipeline.setCurrentDoc(currentDoc);

				// for each UNPACK in EXPAND
				for (Unpack unpack : unpacks) {
					// check if current doc matches the unpack condition
					if (ConditionEvaluator.matchCondition(unpack.getCondition(), tempPipeline)) {
						// Array container
						List<JCOValue> listValues = null;
						Field arrayField = unpack.getArryField();
						// retrieve source array field, and remove from the current doc
						JCOValue array = currentDoc.removeValue(arrayField.toString());

						if (array == null || array.getType() != EValueType.ARRAY)
							JMH.addJCOMessage("[EXPAND]: The inputField is not existing or it's not array: " + arrayField.toString());
						else {
							listValues = ((ArrayValue) array).getValues();
	
							if (listValues != null && !listValues.isEmpty()) {
								// PF threads declaration
								SynchronizedExpandCycle[] threads;
								int nThreads = 1;
								sdr.setInfomer(listValues.size(), 20);
								//PF per il filter uso tutti i processori logici meno uno
								if (EngineConfiguration.getInstance().getNProcessors() > 1)
									nThreads = 2*EngineConfiguration.getInstance().getNProcessors()-1;
								
								// PF threads creation
								// save current doc other fields
								List<FieldDefinition> outputList2 = currentDoc.getFields();
								for (int ndx=0; ndx<outputList2.size(); ndx++) {
									if (outputList2.get(ndx).getName().equals("_id")) {
										outputList2.remove(ndx);
										break;
									}
								}
								threads = new SynchronizedExpandCycle[nThreads];
								for (int i=0; i<nThreads; i++) 										
									threads[i] = new SynchronizedExpandCycle(i, nThreads, queue, unpack, listValues, outputList2);
	
								// PF threads launching
								for (int i=0; i<nThreads; i++)
									threads[i].start();
	
								// PF thread synchro
								for (int i=0; i<nThreads; i++)
									try {
										threads[i].join();
									} catch (InterruptedException e) {
										e.printStackTrace();
										throw new ExecuteProcessException("[EXPAND]: Failed Thread Sychronization");
									}

							}
							
						}
						discardedDoc = false; 
						break;
					}
				}	// end for each unpack

				// add discarded docs if KEEP option
				if (discardedDoc && command.isKeepOthers()) {
					try {
						queue.put(currentDoc);
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new ExecuteProcessException("[EXPAND]: Failed to fill queue");
					}
				}					
			} 	// end for each doc
		}	// end precondition if

		// GET the final collection and return it
		sdr.interrupt();
		try {
			sdr.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ExecuteProcessException("[FILTER]: Failed Remover Thread Sychronization");
		}
		List<DocumentDefinition> outDocs = sdr.getDocs();
		outCollection = new SimpleDocumentCollection("Expand", outDocs);

		
		pipeline.addCollection(outCollection);
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents loaded");
	}
	
	
}
