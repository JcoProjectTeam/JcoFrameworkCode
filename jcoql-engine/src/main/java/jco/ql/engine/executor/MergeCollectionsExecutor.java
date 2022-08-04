package jco.ql.engine.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.MergeCollectionsCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.reference.CollectionReference;

@Executor(MergeCollectionsCommand.class)
public class MergeCollectionsExecutor implements IExecutor<MergeCollectionsCommand> {

	private DatabaseRegistry databaseRegistry;

	@Autowired
	public MergeCollectionsExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	@Override
	public void execute(Pipeline pipeline, MergeCollectionsCommand command) throws ExecuteProcessException {

		/*
		 * Prima fase, vengono istanziati: 1) La collezione che andrà in output
		 * nella pipeline 2) Una collezione temporanea che serve come
		 * riferimento per la collezione corrente da elaborare 3) La lista di
		 * documenti corrente da elaborare
		 */
		final SimpleDocumentCollection outCollection;
		IDocumentCollection singleCollection;
		List<DocumentDefinition> docs;
		IDatabase database;
		List<DocumentDefinition> finalDocs = new ArrayList<>();

		/* Iteratore per la lista di collezioni su cui eseguire il merge */
		ListIterator<CollectionReference> litr = command.getCollections().listIterator();

		while (litr.hasNext()) {

			/*
			 * Viene estratta una collezione alla volta (e i suoi documenti)
			 * dall'input. L'input è dato dal database che si ottiene dal
			 * databaseRegistry in questo modo è possibile sia fare il merge di
			 * collezioni che provengono da database diversi, sia evitare di
			 * salvare le collezioni che si prelevano di volta in volta nella
			 * pipeline.
			 *
			 */
			CollectionReference collectionReference = litr.next();
			String collectionName = collectionReference.getAlias();
			String dbName = collectionReference.getDatabaseName();

			/*
			 * Pezzo aggiunto per gestire collezioni salvate nella pipeline a
			 * cui, quindi, non si fa riferimento con il nome del database.
			 * (esempio)
			 *
			 * GET COLLECTION buildings2@test;
			 * MERGE COLLECTIONS buildings2,restaurants@test;
			 *
			 */

			if (dbName != null) {
				database = databaseRegistry.getDatabase(dbName);
				if (database == null) {
					throw new ExecuteProcessException("[MERGE COLLECTIONS]: Invalid database " + dbName);
				}

				singleCollection = database.getCollection(collectionName);

			} else {

				singleCollection = pipeline.getCollection(collectionName);

			}

			docs = singleCollection.getDocumentList();

			if (docs != null) {

				/*
				 * Il parametro removeDuplicates definisce due condizioni: 
				 * 1) removeDuplicates = false; si esegue il merge duplicando i documenti uguali 
				 * 2) removeDuplicates = true; si esegue il merge mantenendo una sola copia dei documenti duplicati
				 */

				if (command.isRemoveDuplicates()) {
					/*
					 * Si verifica se nella collezione di output è già presente
					 * il ducumento corrente. In tal caso il documento corrente
					 * (quello iterato dal foreach) non viene aggiunto.
					 */
					finalDocs.removeAll(docs);
					finalDocs.addAll(docs);

				} else {
					/* Si aggiungono tutti i documenti all'output */
					finalDocs.addAll(docs);
				}
			}

		}
		/* Alla fine la collezione di output viene aggiunta alla pipeline */
		outCollection = new SimpleDocumentCollection("merge", finalDocs);
		pipeline.addCollection(outCollection);
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " merged");

	}

}
