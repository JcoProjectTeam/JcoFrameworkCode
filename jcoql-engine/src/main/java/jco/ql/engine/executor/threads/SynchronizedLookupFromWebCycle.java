package jco.ql.engine.executor.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.Pipeline;
import jco.ql.engine.byZunEvaluator.ConditionEvaluator;
import jco.ql.engine.byZunEvaluator.ExpressionPredicateEvaluator;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.GetCollectionExecutor;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.LookupFromWebCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.JCOValue;
import jco.ql.parser.model.util.ForEach;

public class SynchronizedLookupFromWebCycle extends Thread implements JCOConstants {

	private int id, nThreads;
	private ArrayList<DocumentDefinition> docs;
	private LinkedBlockingQueue<DocumentDefinition> queue;

	private Pipeline pipeline;
	private IDocumentCollection collection;
	private List<ForEach> forEachList;
	
	public SynchronizedLookupFromWebCycle(int id, int nThread, Pipeline pipeline,
									LinkedBlockingQueue<DocumentDefinition> queue, LookupFromWebCommand command) {
		this.id = id;
		this.nThreads = nThread;
		this.docs = (ArrayList<DocumentDefinition>) docs;
		this.queue = queue;
		setPriority(10);

		this.pipeline = new Pipeline (pipeline, id);
		collection = pipeline.getCurrentCollection();
		forEachList = command.getForEachList();
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

			try {
				DocumentDefinition docFromWeb = evaluate(docPipeline, forEachList);				
				if(docFromWeb != null)
					try {
							queue.put(docFromWeb);
					} catch (InterruptedException e2) {
						e2.printStackTrace();
						JMH.addExceptionMessage("[SynchronizedLookupFromWebCycle]: terminated\n" + e2.getMessage());
		            	throw new ExecuteProcessException("[SynchronizedLookupFromWebCycle]: terminated");
					}
			} catch (ScriptException e1) {
				e1.printStackTrace();
				JMH.addExceptionMessage("[SynchronizedLookupFromWebCycle]: terminated\n" + e1.getMessage());
            	throw new ExecuteProcessException("[SynchronizedLookupFromWebCycle]: terminated");
			}

			ZunTicker.tick();    		
    		// fundamental
    		i += nThreads;  
    	}
    }


	public DocumentDefinition evaluate(Pipeline pipeline, List<ForEach> forEachList) throws ScriptException {
		DocumentDefinition docFromWeb = null;

		for(ForEach fe : forEachList) {
			if (ConditionEvaluator.matchCondition(fe.forEachCondition, pipeline)) {
				JCOValue jv = ExpressionPredicateEvaluator.calculate(fe.getCallExpression(), pipeline);
				if (!JCOValue.isStringValue(jv))
					JMH.addParserMessage("Lookup From Web:\t cannot calculate a proprer end-point url to call");
				else
					docFromWeb = GetCollectionExecutor.getDocumentFromWeb(jv.getStringValue(), pipeline.getCurrentDoc());

				// PF. Once the 1st where condition has met with the current document there's no need to go on.
				break;	// exit for cycle
			} 
		} 
		return docFromWeb;
	}

}
