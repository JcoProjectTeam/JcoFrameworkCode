package jco.ql.engine.parser;

import jco.ql.model.command.ExpandCommand;
import jco.ql.model.command.FilterCommand;
import jco.ql.model.command.FuzzyEvaluatorCommand;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.command.FuzzySetModelCommand;
import jco.ql.model.command.GenericFuzzyOperatorCommand;
import jco.ql.model.command.LookupFromWebCommand;
import jco.ql.model.command.GetCollectionCommand;
import jco.ql.model.command.GetDictionaryCommand;
import jco.ql.model.command.GroupCommand;
import jco.ql.model.command.ICommand;
import jco.ql.model.command.IntersectCollectionsCommand;
import jco.ql.model.command.JavaFunctionCommand;
import jco.ql.model.command.JavascriptFunctionCommand;
import jco.ql.model.command.MergeCollectionsCommand;
import jco.ql.model.command.SaveCollectionCommand;
import jco.ql.model.command.JoinCommand;
import jco.ql.model.command.SubtractCollectionsCommand;
import jco.ql.model.command.TrajectoryMatchingCommand;
import jco.ql.model.command.UseDbCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.parser.model.Expand;
import jco.ql.parser.model.Filter;
import jco.ql.parser.model.FuzzyEvaluator;
import jco.ql.parser.model.FuzzyOperator;
import jco.ql.parser.model.FuzzySetModel;
import jco.ql.parser.model.GenericFuzzyOperator;
import jco.ql.parser.model.LookupFromWeb;
import jco.ql.parser.model.GetCollection;
import jco.ql.parser.model.GetDictionary;
import jco.ql.parser.model.Group;
import jco.ql.parser.model.Instruction;
import jco.ql.parser.model.IntersectCollections;
import jco.ql.parser.model.JavaFunction;
import jco.ql.parser.model.JavascriptFunction;
import jco.ql.parser.model.MergeCollections;
import jco.ql.parser.model.JoinCollections;
import jco.ql.parser.model.SaveCollection;
import jco.ql.parser.model.SubtractCollections;
import jco.ql.parser.model.TrajectoryMatching;
import jco.ql.parser.model.UseDb;


public class Translator implements JCOConstants {

	static public ICommand translate(Instruction instr) {
		if (instr instanceof GetCollection) {
			GetCollection getCollectionInstr = (GetCollection) instr;
			return new GetCollectionCommand(getCollectionInstr);
		} 
		else if (instr instanceof SaveCollection) {
			SaveCollection sc = (SaveCollection) instr;
			return new SaveCollectionCommand (sc);
		} 
		else if(instr instanceof JoinCollections) {
	        JoinCollections jc = (JoinCollections) instr;
	        return new JoinCommand(jc);
		}
		else if (instr instanceof Filter) {
			Filter f = (Filter) instr;
			return new FilterCommand(f);
		} 
		else if (instr instanceof Group) {
			Group g = (Group) instr;
			return new GroupCommand (g);
		}
		else if(instr instanceof Expand){
			Expand e = (Expand) instr;
			return new ExpandCommand (e);
		}
		else if (instr instanceof MergeCollections) {
			MergeCollections mc = (MergeCollections) instr;
			return new MergeCollectionsCommand(mc);
		} 
		else if (instr instanceof IntersectCollections) {
			IntersectCollections ic = (IntersectCollections) instr;
			return new IntersectCollectionsCommand (ic);
		} 
		else if (instr instanceof SubtractCollections) {
			SubtractCollections sc = (SubtractCollections) instr;
			return new SubtractCollectionsCommand (sc);
		} 
		else if(instr instanceof UseDb) {
			UseDb ud = (UseDb) instr;
			return new UseDbCommand (ud);
		}
		else if (instr instanceof TrajectoryMatching) {
			TrajectoryMatching tm = (TrajectoryMatching) instr;
			return new TrajectoryMatchingCommand(tm);
		}
		else if (instr instanceof FuzzyOperator) {
			FuzzyOperator fo = (FuzzyOperator) instr;
			return new FuzzyOperatorCommand (fo);
		} 
		else if (instr instanceof JavascriptFunction) {
			JavascriptFunction jsf = (JavascriptFunction) instr;
			return new JavascriptFunctionCommand(jsf);
		} 
		else if (instr instanceof JavaFunction) {
			JavaFunction jf = (JavaFunction) instr;
			return new JavaFunctionCommand(jf);
		} 
		else if(instr instanceof GetDictionary) {
			GetDictionary gd = (GetDictionary) instr;
			return new GetDictionaryCommand (gd);
		}
		else if(instr instanceof LookupFromWeb) {
			LookupFromWeb gfw = (LookupFromWeb) instr;
			return new LookupFromWebCommand (gfw);
		}
		// added by Federici 27/11/2022 - PF 02.05.2024 ... FuzzyEvaluator can be either Evaluator or Aggregator
		else if(instr instanceof FuzzyEvaluator) {
			FuzzyEvaluator fe = (FuzzyEvaluator) instr;
			return new FuzzyEvaluatorCommand(fe);
		}
		// added by Balicco 26/1/2023
		else if(instr instanceof FuzzySetModel) {
			FuzzySetModel fm = (FuzzySetModel) instr;
			return new FuzzySetModelCommand(fm);
		}
		//added by Balicco 26/1/2023
		else if(instr instanceof GenericFuzzyOperator) {
			GenericFuzzyOperator fgo = (GenericFuzzyOperator) instr;
			return new GenericFuzzyOperatorCommand(fgo);
		}

		return null;
	}
	
}
