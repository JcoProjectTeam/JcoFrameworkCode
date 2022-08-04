package jco.ql.engine.executor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jco.ql.engine.Pipeline;
import jco.ql.engine.annotation.Executor;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.engine.registry.DatabaseRegistry;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.command.TrajectoryMatchingCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;

@Executor(TrajectoryMatchingCommand.class)
public class TrajMatchingExecutor2 implements IExecutor<TrajectoryMatchingCommand>, JCOConstants  {
//	private DatabaseRegistry databaseRegistry;
	List<DocumentDefinition> keepedDocuments;

	@Autowired
	public TrajMatchingExecutor2(DatabaseRegistry databaseRegistry) {
//		this.databaseRegistry = databaseRegistry;
	}
/* ZUN CHECK*
	private Map<String, EDRvalue> map;
	List<Elemento> listX;
	List<Elemento> listY;
*/
	//NUOVO EC
//	private Map<Integer, List<Integer>> matchedPair;

	@Override
	public void execute(Pipeline pipeline, TrajectoryMatchingCommand command) throws ExecuteProcessException {
/* ZUN CHECK*
		final IDocumentCollection targetCollection;
		final IDocumentCollection inputCollection;
		String dbTargetName = command.getTargetCollection().getDatabaseName();
		String dbInputName = command.getInputCollection().getDatabaseName();

		IDatabase db;
		keepedDocuments = new ArrayList<>();

		IDocumentCollection outCollection = new SimpleDocumentCollection("Matchings");


		//EC NUOVO: aggiunti controlli se si vuole utilizzare la collezione temporanea

		if (dbTargetName != null) {

			db = databaseRegistry.getDatabase(dbTargetName);
			if (db == null) {
				throw new ExecuteProcessException("[TRAJ MATCHING]: Invalid target database " + dbTargetName);
			}

			targetCollection = db.getCollection(command.getTargetCollection().getCollectionName());

			pipeline.add(targetCollection, command.getTargetCollection().getCollectionName());
		} else {
			if(command.getTargetCollection().getCollectionName().equals(TEMPORARY_COLLECTION_NAME))
			{
				targetCollection = pipeline.getCollection(TEMPORARY_COLLECTION_NAME);
			}
			else
			{
				targetCollection = pipeline.getCollection(command.getTargetCollection().getCollectionName());
			}
		}
		if (dbInputName != null) {
			db = databaseRegistry.getDatabase(dbInputName);
			if (db == null) {
				throw new ExecuteProcessException("[TRAJ MATCHING]: Invalid input database " + dbInputName);
			}
			inputCollection = db.getCollection(command.getInputCollection().getCollectionName());
			pipeline.add(inputCollection, command.getInputCollection().getCollectionName());
		} else {
			if(command.getInputCollection().getCollectionName().equals(TEMPORARY_COLLECTION_NAME))
			{
				inputCollection = pipeline.getCollection(TEMPORARY_COLLECTION_NAME);
			}
			else
			{
				inputCollection = pipeline.getIRCollection(command.getInputCollection().getCollectionName());
			}
		}

		if (targetCollection != null && inputCollection != null) {

			List<TrajectoryPartitionCondition> partitions = command.getPartitions();
			List<DocumentDefinition> targetDocs = targetCollection.getDocument();
			List<DocumentDefinition> inputDocs = inputCollection.getDocument();

			for (TrajectoryPartitionCondition partition : partitions) {
				listX = new ArrayList<>();
				listY = new ArrayList<>();
				map = new HashMap<String, EDRvalue>();

				SimpleDocumentCollection satisfiedTargetDocs = new SimpleDocumentCollection("Partition");

				targetDocs.stream().forEach(d -> {
					Pipeline p = new Pipeline();
					p.add(d, "Partition");
					try {
						if (checkPartitionConditions(partition, p)) {

							satisfiedTargetDocs.addDocument(d);
						}
						else {
							keepedDocuments.add(d);

						}
					} catch (ScriptException e) {
						e.printStackTrace();
					}

				});


				// NTI and spatial initialization
				List<PartitionMatchingCondition> matchings = partition.getMatchings();
				init(matchings, inputDocs);

				for (DocumentDefinition td : satisfiedTargetDocs.getDocument()) {

					DocumentDefinitionBuilder DOCbuilder = new DocumentDefinitionBuilder();
					DOCbuilder.getNewFieldDefintionBuilder().fromDocument("target", new DocumentValue(td)).add();

					for (PartitionMatchingCondition currentMatching : matchings) {

						List<DocumentDefinition> current_docs = new ArrayList<DocumentDefinition>();

						SimpleDocumentCollection satisfiedInputDocs = new SimpleDocumentCollection("Matching");

						if (currentMatching.hasSelectionCondition()) {
							inputDocs.stream().forEach(d -> {
								Pipeline p = new Pipeline();
								p.add(d, "Matching");
								try {
									if (checkMatchingConditions(currentMatching, p)) {
										satisfiedInputDocs.addDocument(d);
									}
									else {
										keepedDocuments.add(d);
									}
								} catch (ScriptException e) {
									e.printStackTrace();
								}

							});

						} else {

							inputDocs.forEach(d -> {
								satisfiedInputDocs.addDocument(d);
							});
						}

						if (td.hasField(currentMatching.getMatchingTarget().getFieldName())) {

							List<DocumentDefinition> nearInputs = getNearInputs(satisfiedInputDocs, td, currentMatching);

							if (!nearInputs.isEmpty()) {
								boolean isFirstTime = true;
								double edrRef = 0;
								for (DocumentDefinition id : nearInputs) {

									if(id.hasField(currentMatching.getWrt().getFieldName())) {

										double currentEDR = computeEDR(currentMatching, id, td, current_docs, edrRef);

										if (isFirstTime && currentEDR > 0) {
											edrRef = currentEDR;
											isFirstTime = false;

										}

										//NUOVO EC
										//implementazione clausola ADDING path TO INPUT
										if(!matchedPair.isEmpty()) {
											JCOValue v = id.getValue(currentMatching.getWrt().getFieldName().toString());
											if(v instanceof ArrayValue) {
												ArrayValue av = (ArrayValue) v;
												for (Map.Entry<Integer, List<Integer>> entry : matchedPair.entrySet()) {
													for(int j = 0; j < entry.getValue().size(); j++) {
														DocumentValue dv = (DocumentValue)av.getValues().get(entry.getValue().get(j));
														dv.document.addField(FieldDefinition.create().fromInteger(currentMatching.getPathToInput().toString().substring(1), entry.getKey() + 1).build());
													}
												}
											}

										}

									}


								}

								// ho fatto tutti gli input doc per quel td
								DOCbuilder.getNewFieldDefintionBuilder().fromDocumentList(currentMatching.getInto().toString(), current_docs)
										.add();

							} else {
								DOCbuilder.getNewFieldDefintionBuilder()
										.fromString(currentMatching.getInto().toString(), "No nearby input found").add();
							}

						}


					} // passa al prossimo match
					// ho finito tutti i matching per il primo td posso passare al td successivo

					outCollection.addDocument(DOCbuilder.build());

				}



			}
			// finite tutte le partizioni

			List<DocumentDefinition> outDocs = outCollection.getDocument();
			if (command.isKeepOthers())
			{
				List<DocumentDefinition> keepedDocuments2 = keepedDocuments.stream().distinct().collect(Collectors.toList());
				outDocs.addAll(keepedDocuments2);
			}


			pipeline.addCollection(outCollection);

		}
*/
		JMH.addJCOMessage("[" + command.getInstruction().getInstructionName() + "] executed:\t");
	}

	/*
	private boolean checkPartitionConditions(TrajectoryPartitionCondition partitionCondition, Pipeline pipeline) throws ScriptException {
		boolean matches = true;
		List<ICondition> conditions = partitionCondition.getSelectionCondition();
		if (conditions != null) {
			for (ICondition c : conditions) {
				IMatcher matcher = getMatcher(c);
				if (matcher != null && !matcher.matches(c, pipeline)) {
					matches = false;
				}
			}
		}
		return matches;
	}

	private boolean checkMatchingConditions(PartitionMatchingCondition partitionCondition, Pipeline pipeline) throws ScriptException {
		boolean matches = true;
		List<ICondition> conditions = partitionCondition.getSelectionCondition();

		if (conditions != null) {
			for (ICondition c : conditions) {
				IMatcher matcher = getMatcher(c);
				if (matcher != null && !matcher.matches(c, pipeline)) {
					matches = false;
				}
			}
		}
		return matches;
	}

	private GeoJsonValue createGeometry(DocumentDefinition doc, FieldName fn) {

		doc.toString();
		SimpleDocumentCollection myColl = new SimpleDocumentCollection("myCollection");
		myColl.addDocument(doc);
		FieldReference fr = new FieldReference(myColl.getName(), fn);
		GeometryDefinition gd = new GeometryDefinition(fr, 3);
		// ZUN CHECK... da mettere a posto
//		GenerateCommand gc = new GenerateCommand(doc, EGeometryAction.GENERATE, gd);

		Pipeline myPipeline = new Pipeline();
		myPipeline.add(doc, "myCollection");
		// ZUN CHECK... da mettere a posto dopo il check precedente
//		DocumentDefinition geo_doc = GenerateCommandEvaluator.evaluate(myPipeline, gc);   // ZUN valuta la geometria?
//		GeoJsonValue geo = (GeoJsonValue) geo_doc.getValue(GEOMETRY_FIELD_NAME);
//		return geo;
		return null;
	}

	private SimpleValue fromEDRtoSimilarity(double edrDistance, GeoJsonValue tv, GeoJsonValue iv) {
		int dimTarget = tv.getGeometry().getCoordinates().length;
		int dimInput = iv.getGeometry().getCoordinates().length;
		SimpleValue similarity;

		if (edrDistance == -1) {
			similarity = new SimpleValue("Similarity not calculated: pruned by NTI ");

		} else {
			similarity = new SimpleValue(1 - (edrDistance / Math.max(dimTarget, dimInput)));

		}

		return similarity;

	}
*/
/*
	private double computeEDR(PartitionMatchingCondition currentMatching, DocumentDefinition currentInputDoc,
							  DocumentDefinition currentTargetDoc, List<DocumentDefinition> currentDocs, double edrRef) {

		TrajectoryMatchingEvaluator trajMatchingEvaluator = new TrajectoryMatchingEvaluator(currentMatching);
		FieldName inputFieldName = currentMatching.getWrt().getFieldName();
		FieldName targetFieldName = currentMatching.getMatchingTarget().getFieldName();
		double edrDistance = -1;

		GeoJsonValue tv = createGeometry(currentTargetDoc, targetFieldName);
		GeoJsonValue iv = createGeometry(currentInputDoc, inputFieldName);

		// distanza ED
		if (currentMatching.hasSimilarity()) {

			map.forEach((k, v) -> {
				k.toString();
				v.toString();

			});

			EDRvalue currentEDRvalue = map.get(currentInputDoc.hashCode() + inputFieldName.toString());

			double lower_bound = edrRef - currentEDRvalue.getEDRdistance() - currentEDRvalue.getDimension();

			int dimT = tv.getGeometry().getCoordinates().length;
			int dimI = iv.getGeometry().getCoordinates().length;

			double max_edr = (1 - currentMatching.getMinSimilarity()) * Math.max(dimT, dimI);

			if (lower_bound <= max_edr) {
				/*** /

				//NUOVO COSTRUTTORE
				edrDistance = trajMatchingEvaluator.evaluate(tv, iv, true);
				matchedPair = trajMatchingEvaluator.getMatchedPoint();

			}

		} else
		{
			edrDistance = trajMatchingEvaluator.evaluate(tv, iv, true);
			matchedPair = trajMatchingEvaluator.getMatchedPoint();
		}

		SimpleValue similarity = fromEDRtoSimilarity(edrDistance, tv, iv);
		addToResult(currentMatching, currentInputDoc, currentTargetDoc, currentDocs, similarity);


		return edrDistance;

	}

	private void addToResult(PartitionMatchingCondition currentMatching, DocumentDefinition currentInputDoc,
							 DocumentDefinition currentTargetDoc, List<DocumentDefinition> currentDocs, SimpleValue similarity) {

		if (currentMatching.hasSimilarity()) {
			Pipeline pipeline = new Pipeline();
			SimpleValue min_similarity = new SimpleValue(currentMatching.getMinSimilarity());
			BasicConditionMatcher bc = new BasicConditionMatcher();
			BasicExpression be = new BasicExpression(similarity, EOperator.GREATER_THAN, min_similarity);
			// sim > min_similarity
			if (bc.matches(new BasicCondition(be), pipeline)) {

				currentDocs.add(DocumentDefinitionBuilder.create().getNewFieldDefintionBuilder()
						.fromDocument("input", new DocumentValue(currentInputDoc)).add().getNewFieldDefintionBuilder()
						.fromValue("similarity", similarity).add().build());

			}

			/* else {

				// similairty < min_similarity oppure nessuna similarita'

				keepedDocuments.add(DocumentDefinitionBuilder.create().withField()
						.fromDocument("target", new DocumentValue(currentTargetDoc)).add().withField()
						.fromDocument("input", new DocumentValue(currentInputDoc)).add().withField()
						.fromValue("similarity", similarity).add().build());


			} * /

		}

		else {

			currentDocs.add(DocumentDefinitionBuilder.create().getNewFieldDefintionBuilder()
					.fromDocument("input", new DocumentValue(currentInputDoc)).add().getNewFieldDefintionBuilder()
					.fromValue("similarity", similarity).add().build());
		}

	}

	private void init(List<PartitionMatchingCondition> matchings, List<DocumentDefinition> inputDocs) {

		for (PartitionMatchingCondition matching : matchings) {
			NTIpruning(inputDocs, matching);
			SpatialPruning(inputDocs, matching);
		}

	}

	// popola l'hashMap
	private void NTIpruning(List<DocumentDefinition> inputDocs, PartitionMatchingCondition matching) {
		// ref input is always first element
		DocumentDefinition doc = inputDocs.get(0);
		doc.toString();

		FieldName currentMatchingInputFN = matching.getWrt().getFieldName();
		GeoJsonValue ref = createGeometry(doc, currentMatchingInputFN);
		TrajectoryMatchingEvaluator evaluator = new TrajectoryMatchingEvaluator(matching);
		for (DocumentDefinition cd : inputDocs) {
			GeoJsonValue currentValue = createGeometry(cd, currentMatchingInputFN);
			double currentDim = currentValue.getGeometry().getCoordinates().length;
			double edrDistance = evaluator.evaluate(ref, currentValue, false);

			map.put(cd.hashCode() + currentMatchingInputFN.toString(), new EDRvalue(currentDim, edrDistance));

		}
	}

	/* PF
	private List<DocumentDefinition> getNearInputsOLD(SimpleDocumentCollection satisfiedInputDocs,
													  DocumentDefinition targetDoc, PartitionMatchingCondition matching) {

		List<DocumentDefinition> nearInputs = new ArrayList<>();

		satisfiedInputDocs.getDocuments().forEach(d -> {

			nearInputs.add(d);
		});

		return nearInputs;

	}
	* /

	private List<DocumentDefinition> getNearInputs(SimpleDocumentCollection satisfiedInputDocs,
												   DocumentDefinition targetDoc, PartitionMatchingCondition matching) {

		double epsKM;
		double eps = matching.getThreshold();
		EUnitOfMeasure unit = matching.getUnit();

		switch (unit) {
			case METERS:
				eps = eps / 1000;
				break;
			case MILES:
				eps = eps * 1.60934;
				break;
			case KILOMETERS:

				break;
			default:
				break;

		}

		epsKM = eps;




		FieldName fn = matching.getMatchingTarget().getFieldName();
		GeoJsonValue g = createGeometry(targetDoc, fn);
		Envelope bb = g.getGeometry().getEnvelopeInternal();
		Comparatore c = new Comparatore();
		List<Elemento> tempListX = new ArrayList<>();
		List<Elemento> tempListY = new ArrayList<>();
		String currentInputFN = matching.getWrt().getFieldName().toString();



		for (int i = 0; i < listX.size(); i++) {
			Elemento ei = listX.get(i);

			if (ei.getField().equals(currentInputFN))
				tempListX.add(ei);

		}

		for (int i = 0; i < listY.size(); i++) {
			Elemento ei = listY.get(i);

			if (ei.getField().equals(currentInputFN))
				tempListY.add(ei);

		}




		// Y is latitude --> 1° = 110.574 KM
		double dY = epsKM / 110.574;
		// X is longitude --> 1°= 111.320 * cos(lat) KM
		// @ maxY
		double dX1 = epsKM / (111.320 * Math.cos(Math.toRadians(bb.getMaxY())));
		// @ minY
		double dX2 = epsKM / (111.320 * Math.cos(Math.toRadians(bb.getMinY())));

		double minX = bb.getMinX() - dX2;
		double maxX = bb.getMaxX() + dX1;
		double minY = bb.getMinY() - dY;
		double maxY = bb.getMaxY() + dY;

		tempListX.add(new Elemento(0, fn.toString(), minX));
		tempListX.add(new Elemento(0, fn.toString(), maxX));
		tempListY.add(new Elemento(0, fn.toString(), minY));
		tempListY.add(new Elemento(0, fn.toString(), maxY));

		tempListX.sort(c);
		tempListY.sort(c);

		List<Integer> matchingInputsX = checkPatternMatchings(tempListX);
		List<Integer> matchingInputsY = checkPatternMatchings(tempListY);

		List<Integer> commonInputs = new ArrayList<>();
		for (int i = 0; i < matchingInputsX.size(); i++) {
			Integer ei = matchingInputsX.get(i);
			if (matchingInputsY.contains(ei))
				commonInputs.add(ei);

		}

		List<DocumentDefinition> nearInputs = new ArrayList<>();

		satisfiedInputDocs.getDocument().forEach(d -> {

			if (commonInputs.contains(d.hashCode()))
			{
				nearInputs.add(d);
			}
		});

		return nearInputs;

	}

	private List<Integer> checkPatternMatchings(List<Elemento> lista) {

		boolean beforeTarget = true;
		List<Integer> matchingInputs = new ArrayList<>();

		for (int i = 0; i < lista.size(); i++) {
			Integer ei = lista.get(i).getHash();

			if (ei == 0 && beforeTarget)
			{
				beforeTarget = false;
			}
			if (ei == 0 && !beforeTarget)
			{
				continue;
			}
			if (beforeTarget) {

				if (matchingInputs.contains(ei))
				{
					matchingInputs.remove(ei);
				}
				else
				{
					matchingInputs.add(ei);
				}

			} else {
				if (!matchingInputs.contains(ei) && ei != 0)
				{
					matchingInputs.add(ei);
				}
			}

		}

		return matchingInputs;

	}

	private void SpatialPruning(List<DocumentDefinition> inputDocs, PartitionMatchingCondition matching) {

		TrajectoryMatchingEvaluator evaluator = new TrajectoryMatchingEvaluator(matching);
		double epsKM = evaluator.getEpsKM();

		for (DocumentDefinition doc : inputDocs) {

			FieldName fn = matching.getWrt().getFieldName();
			GeoJsonValue g = createGeometry(doc, fn);
			Envelope bb = g.getGeometry().getEnvelopeInternal();

			// Y is latitude --> 1° = 110.574 KM
			double dY = epsKM / 110.574;
			// X is longitude --> 1°= 111.320 * cos(lat) KM
			// @ maxY
			double dX1 = epsKM / (111.320 * Math.cos(Math.toRadians(bb.getMaxY())));
			// @ minY
			double dX2 = epsKM / (111.320 * Math.cos(Math.toRadians(bb.getMinY())));

			// increase bounding box of eps
			double minX = bb.getMinX() - dX2;
			double maxX = bb.getMaxX() + dX1;
			double minY = bb.getMinY() - dY;
			double maxY = bb.getMaxY() + dY;

			listX.add(new Elemento(doc.hashCode(), fn.toString(), minX));
			listX.add(new Elemento(doc.hashCode(), fn.toString(), maxX));
			listY.add(new Elemento(doc.hashCode(), fn.toString(), minY));
			listY.add(new Elemento(doc.hashCode(), fn.toString(), maxY));
		}

	}

}

class Comparatore implements Comparator<Elemento> {

	@Override
	public int compare(Elemento o1, Elemento o2) {
		if (o1.getCoord() < o2.getCoord())
			return -1;
		else if (o2.getCoord() < o1.getCoord())
			return 1;
		return 0;
	}

}

class Elemento {
	private int hash;
	private String fn;
	private double coord;

	public Elemento(int docHash, String fn, double coord) {
		this.hash = docHash;
		this.fn = fn;
		this.coord = coord;
	}

	public double getCoord() {
		return coord;
	}

	public int getHash() {
		return hash;
	}

	public String getField() {
		return fn;
	}

}

class EDRvalue {
	double dimension;
	double edrDistance;

	public EDRvalue(double dim, double edr) {

		this.dimension = dim;
		this.edrDistance = edr;
	}

	double getDimension() {
		return this.dimension;
	}

	double getEDRdistance() {
		return this.edrDistance;
	}
*/
}
