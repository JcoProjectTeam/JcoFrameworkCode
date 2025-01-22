package jco.ql.engine.executor.threads;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.Pipeline;
import jco.ql.engine.evaluator.CaseEvaluator;
import jco.ql.engine.evaluator.GenerateEvaluator;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.Case;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.FilterCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.parser.model.util.GenerateSection;

public class SynchronizedFilterCycle extends Thread implements JCOConstants {

	private int id, nThreads;
	private ArrayList<DocumentDefinition> docs;
	private LinkedBlockingQueue<DocumentDefinition> queue;

	private Pipeline pipeline;
	private IDocumentCollection collection;
	private Case caseFilter ;
	private GenerateSection generateSection;
	
	public SynchronizedFilterCycle(int id, int nThread, Pipeline pipeline,
									LinkedBlockingQueue<DocumentDefinition> queue, FilterCommand command) {
		this.id = id;
		this.nThreads = nThread;
		this.docs = (ArrayList<DocumentDefinition>) docs;
		this.queue = queue;
		setPriority(10);

		this.pipeline = new Pipeline (pipeline, id);
		collection = pipeline.getCurrentCollection();
		caseFilter = command.getCaseFilter();
		generateSection = command.getGenerateSection();
	}

	
    @Override
    public void run() {
    	// fundamental
    	int i = id;   				

    	DocumentDefinition evaluatuedDocument;    	
    	while (i < collection.getDocumentList().size()) {
    		evaluatuedDocument = collection.getDocumentList().get(i);
			Pipeline docPipeline = new Pipeline(pipeline);
			docPipeline.setCurrentDoc(evaluatuedDocument);

			DocumentDefinition doc = null;
			try {
				/* CASES and GENERATE SECTION are complementary */
				if (caseFilter != null)
					doc = CaseEvaluator.evaluate(docPipeline, caseFilter);
				else
					doc = GenerateEvaluator.evaluate(docPipeline, generateSection);
				if(doc != null)
					try {
						queue.put(doc);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
						JMH.addExceptionMessage("[SynchronizedFilterCycle]: terminated\n" + e2.getMessage());
		            	throw new ExecuteProcessException("[SynchronizedFilterCycle]: terminated");
					}
			} catch (ScriptException e1) {
				e1.printStackTrace();
				JMH.addExceptionMessage("[SynchronizedFilterCycle]: terminated\n" + e1.getMessage());
            	throw new ExecuteProcessException("[SynchronizedFilterCycle]: terminated");
			}

			ZunTicker.tick();    		
    		// fundamental
    		i += nThreads;  
    	}
    }



}
