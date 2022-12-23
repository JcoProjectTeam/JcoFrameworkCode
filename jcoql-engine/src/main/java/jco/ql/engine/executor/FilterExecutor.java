package jco.ql.engine.executor;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import jco.ql.byZun.ZunTicker;
import jco.ql.byZun.ZunTimer;
import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.threads.SynchronizedDuplicateRemover;
import jco.ql.engine.executor.threads.SynchronizedFilterCycle;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.FilterCommand;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;


@Executor(FilterCommand.class)
public class FilterExecutor implements IExecutor<FilterCommand>, JCOConstants {

	@Override
	public void execute(Pipeline pipeline, FilterCommand command) throws ExecuteProcessException, ScriptException {
		ZunTimer.getInstance().reset();
		IDocumentCollection collection = pipeline.getCurrentCollection();

		LinkedBlockingQueue<DocumentDefinition> queue = new LinkedBlockingQueue<DocumentDefinition>();
		SynchronizedDuplicateRemover sdr = new SynchronizedDuplicateRemover(queue, command.isRemoveDuplicates());
		sdr.start();		
		sdr.setDimensions(collection.getDocumentList().size());
		sdr.setInfomer(collection.getDocumentList().size(), 20);
		if (collection.getDocumentList().size() > 100)
			ZunTicker.reset("Filter", collection.getDocumentList().size(), 100);
		else
			ZunTicker.reset("Filter", collection.getDocumentList().size(), collection.getDocumentList().size());

		// PF threads declaration
		SynchronizedFilterCycle[] threads;
		int nThreads = 1;
		//PF per il filter uso tutti i processori fisici meno uno
		if (EngineConfiguration.getInstance().getNProcessors() > 1)
			nThreads = EngineConfiguration.getInstance().getNProcessors()-1;

		// PF threads creation
		threads = new SynchronizedFilterCycle[nThreads];
		for (int i=0; i<nThreads; i++)
			threads[i] = new SynchronizedFilterCycle(i, nThreads, pipeline, queue, command);

		// PF threads launching
		for (int i=0; i<nThreads; i++)
			threads[i].start();

		// PF thread synchro
		for (int i=0; i<nThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new ExecuteProcessException("[FILTER]: Failed Thread Sychronization");
			}
		}
		
		// GET the final collection and return it
		sdr.interrupt();
		try {
			sdr.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ExecuteProcessException("[FILTER]: Failed Remover Thread Sychronization");
		}
		List<DocumentDefinition> outDocs = sdr.getDocs();

		SimpleDocumentCollection outCollection = new SimpleDocumentCollection(FILTER_COLLECTION_NAME, outDocs);
		pipeline.addCollection(outCollection);
		ZunTimer.getInstance().getMilliPartial("Tempo totale filter");
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents filtered");
		
	}
		
}