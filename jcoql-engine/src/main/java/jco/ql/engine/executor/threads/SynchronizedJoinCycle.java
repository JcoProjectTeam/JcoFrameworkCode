package jco.ql.engine.executor.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.ScriptException;

import org.locationtech.jts.geom.Geometry;

import jco.ql.byZun.ZunTicker;
import jco.ql.engine.Pipeline;
import jco.ql.engine.evaluator.CaseEvaluator;
import jco.ql.engine.evaluator.ExpressionFactorEvaluator;
import jco.ql.engine.evaluator.GenerateEvaluator;
import jco.ql.engine.evaluator.GeometryEvaluator;
import jco.ql.engine.evaluator.SetFuzzySetsEvaluator;
import jco.ql.engine.evaluator.SpatialFunctionEvaluator;
import jco.ql.engine.exception.ExecuteProcessException;
import jco.ql.model.Case;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.JoinCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.GeometryValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.JoinCollections;
import jco.ql.parser.model.util.AddField;
import jco.ql.parser.model.util.GenerateSection;
import jco.ql.parser.model.util.SpatialFunction;

public class SynchronizedJoinCycle extends Thread implements JCOConstants {

	private int id, nThreads;
	private DocumentDefinition ld;
	private ArrayList<DocumentDefinition> rightDocs;
	private LinkedBlockingQueue<DocumentDefinition> queue;
	private JoinCommand command;
	private Pipeline pipeline;

	public SynchronizedJoinCycle(int id, int nThread, Pipeline pipeline, DocumentDefinition ld, List<DocumentDefinition> rightDocs,
										LinkedBlockingQueue<DocumentDefinition> queue, JoinCommand command) {
		this.id = id;		
		this.nThreads = nThread;
		this.pipeline = new Pipeline (pipeline, id);
		this.ld = ld;
		this.rightDocs = (ArrayList<DocumentDefinition>) rightDocs;
		this.queue = queue;
		this.command = command;
		setPriority(10);
	}

	
    @Override
    public void run() {
    	// fundamental
    	int i = id;   				

    	DocumentDefinition rd;    	
    	while (i < rightDocs.size()) {
    		rd = rightDocs.get(i);
			DocumentDefinition doc = performJoin (ld, rd, command); 
			if(doc != null) {
				try {
					queue.put(doc);
				} catch (InterruptedException e) {
					e.printStackTrace();
	            	throw new ExecuteProcessException("[SynchronizedJoinCycle]: terminated");
				}
			}

    		ZunTicker.tick();
    		// fundamental
    		i += nThreads;  
    	}
    }


	private DocumentDefinition performJoin (DocumentDefinition lDoc, DocumentDefinition rDoc, JoinCommand command) {
		DocumentDefinition newDoc = null;
		
		DocumentValue leftDocumentValue = new DocumentValue(lDoc);
		FieldDefinition leftField = new FieldDefinition(command.getLeftCollection().getAlias(), leftDocumentValue);
		DocumentValue rightDocumentValue = new DocumentValue(rDoc);
		FieldDefinition rightField = new FieldDefinition(command.getRightCollection().getAlias(), rightDocumentValue);

		List<FieldDefinition> newDocFields = new ArrayList<>();
		newDocFields.add(leftField);
		newDocFields.add(rightField);
		newDoc = new DocumentDefinition(newDocFields);

		SpatialFunction onGeometry = command.getOnGeometryCondition();
		if(onGeometry != null) {
			GeometryValue lGeo = (GeometryValue) lDoc.getValue(GEOMETRY_FIELD_NAME);
			GeometryValue rGeo = (GeometryValue) rDoc.getValue(GEOMETRY_FIELD_NAME);
			if (lGeo == null || rGeo == null)
				return null;

            Geometry lg = lGeo.getGeometry();
            Geometry rg = rGeo.getGeometry();

            if (!SpatialFunctionEvaluator.matchSpatialCondition(onGeometry, lg, rg))
				return null;
		}


		if (newDoc != null) {
			Pipeline docPipeline = new Pipeline(pipeline);
			docPipeline.setCurrentDoc(newDoc);

			/* SET GEOMETRY */
			int geometryOperation = command.getSetGeometryOperation();
			if(geometryOperation != JoinCollections.GEOMETRY_UNDEFINED) {
				GeometryValue outGeo = GeometryEvaluator.evaluate(geometryOperation, lDoc, rDoc);
				newDoc.addField(new FieldDefinition(GEOMETRY_FIELD_NAME, outGeo));
			}
			
			/* ADD FIELDS */
			for (AddField af : command.getAddField()) {
				JCOValue v = new SimpleValue();		// null value;
				if (af.getType() ==  AddField.FACTOR_FIELD)
					v = ExpressionFactorEvaluator.evaluate(af.getFactor(), docPipeline);
				else {
					GeometryValue lGeo = (GeometryValue) lDoc.getValue(GEOMETRY_FIELD_NAME);
					GeometryValue rGeo = (GeometryValue) rDoc.getValue(GEOMETRY_FIELD_NAME);
					if (lGeo != null && rGeo != null) {
			            Geometry lg = lGeo.getGeometry();
			            Geometry rg = rGeo.getGeometry();
						v = SpatialFunctionEvaluator.evaluate(af.getSpatialFunction(), lg, rg);
					}					
				}
				if (!JCOValue.isNull(v))
					newDoc.insertField(af.field, v);
			}
	
			/* SET FUZZY SETS */
			if(command.getSetFuzzySetsCommand() != null) {
				List<FieldDefinition> fuzzySetList = new ArrayList<>();
				fuzzySetList = SetFuzzySetsEvaluator.evaluate(ld, rDoc, command.getSetFuzzySetsCommand());
				if (fuzzySetList.size() > 0)
					newDoc.addField(new FieldDefinition(FUZZYSETS_FIELD_NAME, new DocumentValue(new DocumentDefinition(fuzzySetList))));
			}
			
			/* CASES and GENERATE SECTION are complementary */
			Case caseFilter = command.getCaseFilter();
			if(caseFilter != null) {
				try {
					newDoc = CaseEvaluator.evaluate(docPipeline, caseFilter);				
				} catch (ScriptException e) {
					e.printStackTrace();
					JMH.addExceptionMessage("[SynchronizedJoinCycle]: CASE terminated\n" + e.getMessage());
	            	throw new ExecuteProcessException("[SynchronizedJoinCycle]: CASE terminated");
				}
			} 
			/* GENERATE SECTION */
			GenerateSection generateSection = command.getGenerateSection();
			if (generateSection != null)
				newDoc = GenerateEvaluator.evaluate(docPipeline, generateSection);

		} 

		return newDoc;
	}

}
