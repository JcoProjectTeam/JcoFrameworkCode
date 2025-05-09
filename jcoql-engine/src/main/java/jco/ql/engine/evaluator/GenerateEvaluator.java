package jco.ql.engine.evaluator;

import java.util.List;

import jco.ql.engine.Pipeline;
import jco.ql.model.DocumentDefinition;
import jco.ql.parser.model.fuzzy.FuzzySetDefinition;
import jco.ql.parser.model.util.BuildAction;
import jco.ql.parser.model.util.GenerateSection;

public class GenerateEvaluator {

	public static DocumentDefinition evaluate (Pipeline pipeline, GenerateSection gs) {
		DocumentDefinition outDoc = pipeline.getCurrentDoc();
		/* GEOMETRIC OPTION */
		if (gs.hasGeometricOption()) 
			outDoc = GeometricOptionEvaluator.evaluateGeometricOption(pipeline, gs.geometricOption);						

		/* CHECK FOR FUZZY SETS */
		if (gs.hasFuzzyCheck()) {
				Pipeline checkForPipeline = new Pipeline(pipeline);
				checkForPipeline.setCurrentDoc(outDoc);
			List<FuzzySetDefinition> generateFuzzy = gs.fuzzySetDefinitions;
			for (FuzzySetDefinition checkForFuzzySet : generateFuzzy) 
				outDoc = CheckForFuzzySetEvaluator.evaluate (checkForFuzzySet, checkForPipeline);
		}
		
		/* ALPHA-CUT */
		if (gs.hasAlphaCut())
			outDoc = AlphaCutEvaluator.evaluate(outDoc, gs.alphaCuts);

		if (outDoc != null) {
			// BUILD
			if (gs.hasBuildAction()) 
				for (int i=0; i<gs.buildActions.size(); i++) {
					Pipeline buildPipeline = new Pipeline(pipeline);
					buildPipeline.setCurrentDoc(outDoc);
					BuildAction buildAction =  gs.buildActions.get(i);
					outDoc = GenerateCommandEvaluator.evaluateBuildAction(buildPipeline, buildAction);							
			}

			/* FUZZY OPTION - DROPPING/KEEPING (ALL) FUZZY SETS (fuzzy sets, ...) */
			if(gs.hasKeepDropFuzzySets()) {
				outDoc = KeepingDroppingFuzzySetsEvaluator.evaluate(outDoc, gs.keepDropFuzzySets);
			}

			/* DROPPING GEOMETRY */
			if(gs.hasDropGeometry()) {
				outDoc = GeometricOptionEvaluator.removeGeometry(outDoc);
			}
		}	
		return outDoc;
	}

}
