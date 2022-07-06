package jco.ql.engine.executor;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.threads.SynchronizedDuplicateRemover;
import jco.ql.engine.executor.threads.SynchronizedLookupFromWebCycle;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.LookupFromWebCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;


@Executor(LookupFromWebCommand.class)
public class LookupFromWebExecutor implements IExecutor<LookupFromWebCommand>, JCOConstants {

	@Override
	public void execute(Pipeline pipeline, LookupFromWebCommand command) throws ExecuteProcessException, ScriptException {
		IDocumentCollection collection = pipeline.getCurrentCollection();

		LinkedBlockingQueue<DocumentDefinition> queue = new LinkedBlockingQueue<DocumentDefinition>();
		// ZUN CHECK : introdurre REMOVE DULICATES?
		boolean removeDuplicates = false;
		SynchronizedDuplicateRemover sdr = new SynchronizedDuplicateRemover(queue, removeDuplicates);
		sdr.start();		
		sdr.setDimensions(collection.getDocumentList().size());
		sdr.setInfomer(collection.getDocumentList().size(), 20);
		if (collection.getDocumentList().size() > 100)
			ZunTicker.reset("LookupFromWeb", collection.getDocumentList().size(), 100);
		else
			ZunTicker.reset("LookupFromWeb", collection.getDocumentList().size(), collection.getDocumentList().size());

		// PF threads declaration
		SynchronizedLookupFromWebCycle[] threads;
		int nThreads = 1;
		//PF per il LookupFromWeb uso tutti i processori fisici meno uno
		if (EngineConfiguration.getInstance().getNProcessors() > 1)
			nThreads = EngineConfiguration.getInstance().getNProcessors()-1;

		// PF threads creation
		threads = new SynchronizedLookupFromWebCycle[nThreads];
		for (int i=0; i<nThreads; i++)
			threads[i] = new SynchronizedLookupFromWebCycle(i, nThreads, pipeline, queue, command);

		// PF threads launching
		for (int i=0; i<nThreads; i++)
			threads[i].start();

		// PF thread synchro
		for (int i=0; i<nThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new ExecuteProcessException("[GATHER FROM WEB]: Failed Thread Sychronization");
			}
		}
		
		// GET the final collection and return it
		sdr.interrupt();
		try {
			sdr.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ExecuteProcessException("[GATHER FROM WEB]: Failed Remover Thread Sychronization");
		}
		List<DocumentDefinition> outDocs = sdr.getDocs();

		SimpleDocumentCollection outCollection = new SimpleDocumentCollection(LOOKUPFROMWEB_COLLECTION_NAME, outDocs);
		pipeline.addCollection(outCollection);

		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents gathered");
		
	}
		
}