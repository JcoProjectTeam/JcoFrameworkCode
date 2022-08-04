package jco.ql.engine.executor;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.IntersectCollectionsCommand;
import jco.ql.model.engine.IDatabase;
import jco.ql.model.engine.IDocumentCollection;
import jco.ql.model.engine.JMH;
import jco.ql.model.engine.SimpleDocumentCollection;
import jco.ql.model.reference.CollectionReference;

@Executor(IntersectCollectionsCommand.class)
public class IntersectCollectionsExecutor implements IExecutor<IntersectCollectionsCommand> {

	private DatabaseRegistry databaseRegistry;

	@Autowired
	public IntersectCollectionsExecutor(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	@Override
	public void execute(Pipeline pipeline, IntersectCollectionsCommand command) throws ExecuteProcessException {

		/*
		 * Prima fase, vengono istanziati: 1) La collezione che andrà in output
		 * nella pipeline 2) Una collezione temporanea che serve come
		 * riferimento per la collezione corrente da elaborare 3) La lista di
		 * documenti corrente da elaborare
		 */
		final SimpleDocumentCollection outCollection = new SimpleDocumentCollection("intersect");
		IDocumentCollection singleCollection;
		List<DocumentDefinition> docs;
		IDatabase database;
		CollectionReference collectionReference;
		String collectionName;
		String dbName;

		/* Iteratore per la lista di collezioni su cui eseguire l'intersect */
		ListIterator<CollectionReference> litr = command.getCollections().listIterator();

		/* Nomi della prima collezione e del suo database */
		collectionReference = litr.next();
		collectionName = collectionReference.getAlias();
		dbName = collectionReference.getDatabaseName();


		if(dbName != null){

		database = databaseRegistry.getDatabase(dbName);
		if(database == null) {
			throw new ExecuteProcessException("[INTERSECT COLLECTION]: Invalid database " + dbName);
		}

		/*
		 * Inizializzazione: viene prelevata la prima collezione dalla pipeline
		 */
		//singleCollection = pipeline.getCollection(litr.next().getAlias());
		singleCollection = database.getCollection(collectionName);
		}else{

			singleCollection = pipeline.getCollection(collectionName);


		}

		/*
		 * i documenti vengono subito salvati nella collezione di documenti che
		 * andrà in output dopodichè verranno filtrati dal momento che l'output
		 * è sicuramente un sottoinsieme di questo gruppo
		 */
		docs = singleCollection.getDocumentList();
		/* Se non viene trovata nessuna collezione la pipeline è vuota */
		if (docs == null)
			throw new ExecuteProcessException("Empty pipeline");

		Stream<DocumentDefinition> stream = docs.stream();

		while (litr.hasNext()) {

			/*
			 * Viene estratta una collezione alla volta (e i suoi documenti)
			 * dall'input
			 */
			collectionReference = litr.next();
			collectionName = collectionReference.getAlias();
			dbName = collectionReference.getDatabaseName();

			if(dbName!= null){

			database = databaseRegistry.getDatabase(dbName);
			if(database == null) {
				throw new ExecuteProcessException("Invalid database " + dbName);
			}

			//singleCollection = pipeline.getCollection(collectionName);
			singleCollection = database.getCollection(collectionName);

			}else{

				singleCollection = pipeline.getCollection(collectionName);

			}


			List<DocumentDefinition> tempDocs = singleCollection.getDocumentList();

			if (docs != null) {

				/* Lo stream di documenti viene filtrato ad ogni passo */
				stream = stream.filter(d -> {
					return (tempDocs.contains(d));
				});

			}
		}
		/* Alla fine la collezione di output viene aggiunta alla pipeline */
		stream.forEach(outCollection::addDocument);
		pipeline.addCollection(outCollection);
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t" + outCollection.getDocumentList().size() + " documents generated");
	}

}
