package jco.ql.engine.executor;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.executor.threads.SynchronizedDuplicateRemover;
import jco.ql.engine.executor.threads.SynchronizedJoinCycle;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.JoinCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;

@Executor(JoinCommand.class)
public class JoinExecutor implements IExecutor<JoinCommand>, JCOConstants {
	private DatabaseRegistry databaseRegistry;

	@Autowired
	public JoinExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}


	@Override
	public void execute(Pipeline pipeline, JoinCommand command) throws ExecuteProcessException {
//	PF
		LinkedBlockingQueue<DocumentDefinition> queue = new LinkedBlockingQueue<DocumentDefinition>();
		SynchronizedDuplicateRemover sdr = new SynchronizedDuplicateRemover(queue, command.isRemoveDuplicates());
		sdr.start();
//	PF	end
		final SimpleDocumentCollection outCollection;

		final IDocumentCollection leftCollection;// = getCollection (command.getLeftCollection(), pipeline);
		final IDocumentCollection rightCollection;// = getCollection (command.getRightCollection(), pipeline);
	
 //ZUN CHECK ... se le due righe precedenti funzionano, eliminare il resto
		IDatabase database;
		String dbNameLeft = command.getLeftCollection().getDatabaseName();
		if(dbNameLeft!=null){
			database = databaseRegistry.getDatabase(dbNameLeft);
			if (database == null) {
				throw new ExecuteProcessException("[JOIN]: Invalid database " + dbNameLeft);
			}
			leftCollection = database.getCollection(command.getLeftCollection().getCollectionName());
			pipeline.add(leftCollection, command.getLeftCollection().getCollectionName());
		}
		else{
			leftCollection = pipeline.getCollection(command.getLeftCollection().getCollectionName());
		}

		String dbNameRight = command.getRightCollection().getDatabaseName();
		if(dbNameRight!=null){
			database = databaseRegistry.getDatabase(dbNameRight);
			if (database == null) {
				throw new ExecuteProcessException("[JOIN]: Invalid database " + dbNameRight);
			}
			rightCollection = database.getCollection(command.getRightCollection().getCollectionName());
			pipeline.add(rightCollection, command.getRightCollection().getCollectionName());
		}
		else{
			rightCollection = pipeline.getCollection(command.getRightCollection().getCollectionName());
		}

		if(leftCollection != null && rightCollection != null) {
			/* PF. ok */
			List<DocumentDefinition> leftDocs = leftCollection.getDocumentList();
			List<DocumentDefinition> rightDocs = rightCollection.getDocumentList();
			// PF. set SDR size
			sdr.setDimensions(leftDocs.size(), rightDocs.size());
			ZunTicker.reset("Join", leftDocs.size()*rightDocs.size(), 1000);

			// PF - first foreach cycle is sequential. the 2nd is parallel
			for (DocumentDefinition ld : leftDocs) {			/* substitution */
				// PF threads declaration
				SynchronizedJoinCycle[] threads;
				int nThreads = 1;
				//PF per il filter uso tutti i processori fisici meno uno
				if (EngineConfiguration.getNProcessors() > 1)
					nThreads = EngineConfiguration.getNProcessors()-1;

				// PF threads creation
				threads = new SynchronizedJoinCycle[nThreads];
				for (int i=0; i<nThreads; i++)
					threads[i] = new SynchronizedJoinCycle(i, nThreads, pipeline, ld, rightDocs, queue, command);

				// PF threads launching
				for (int i=0; i<nThreads; i++)
					threads[i].start();

				// PF thread synchro
				for (int i=0; i<nThreads; i++)
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						throw new ExecuteProcessException("[JOIN]: Failed Thread Sychronization");
					}
			}
		}

		// GET the final collection and return it
		sdr.interrupt();
		try {
			sdr.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ExecuteProcessException("[JOIN]: Failed Remover Thread Sychronization");
		}
		List<DocumentDefinition> outDocs = sdr.getDocs();

		outCollection = new SimpleDocumentCollection(JOIN_COLLECTION_NAME, outDocs);
		pipeline.addCollection(outCollection);
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents generated");
	}
	
}
