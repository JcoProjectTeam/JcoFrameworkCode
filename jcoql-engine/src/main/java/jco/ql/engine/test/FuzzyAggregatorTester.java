package jco.ql.engine.test;

import jco.ql.engine.Pipeline;
import jco.ql.engine.byZunEvaluator.FuzzyAggregatorEvaluator;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.command.FuzzyAggregatorCommand;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.Environment;
import jco.ql.parser.JCoQLLexer;
import jco.ql.parser.JCoQLParser;
import jco.ql.parser.model.FuzzyAggregator;
import jco.ql.parser.model.predicate.UsingAggregatorPredicate;
import jco.ql.parser.model.util.Parameter;
import java.io.FileReader;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRReaderStream;

public class FuzzyAggregatorTester {

	public static void main(String[] args) {
		JCoQLParser parser = null;
		// file da parsare
		String fileIn = ".\\resources\\input.file";
  	
		try {
			JCoQLLexer lexer = new JCoQLLexer(new ANTLRReaderStream(new FileReader(fileIn))); 
			parser = new JCoQLParser(lexer);
			parser.start();
			Environment env = parser.getEnvironment();
			
			System.out.println("Parser " + env.getVersion() + 
							" - Release " + env.getRelease() + 
							":\tNumero di istruzioni analizzate: " + env.getNInstruction());

			System.out.println("\nNumero di errori:\t" + env.getErrorList().size());
		    // lista degli errori (eventuali)
		    for (int i=0;i<env.getErrorList().size();i++)
		    	System.out.println((i+1) + ".\t" + parser.getErrorList().get(i));
	    
		    // lista delle istruzioni
		    if (env.hasNoError()) {
			    System.out.println("\nLista delle istruzioni:");
			    for (int i=0;i<env.getInstructionList().size();i++) 
					System.out.println(env.getInstructionList().get(i).toString());
			    System.out.println("\n\nLista delle istruzioni formattata:");
			    for (int i=0;i<env.getInstructionList().size();i++) 
					System.out.println(env.getInstructionList().get(i).toMultilineString());
		    }
		    JMH.toggleToScreen(true);
		    FuzzyAggregator fa = (FuzzyAggregator) env.getInstructionList().get(0);
		    FuzzyAggregatorCommand fac = new FuzzyAggregatorCommand(fa);
		    Pipeline pipeline = new Pipeline();
		    ArrayList<String> inst = new ArrayList<String>();
		    inst.add("agg");
		    pipeline.setIstructions(inst);
		    pipeline.addFuzzyAggregator(fac);
		    FieldDefinition fd2 = new FieldDefinition("Young", new SimpleValue(new Double(0.8)));
		    FieldDefinition fd1 = new FieldDefinition("Rich", new SimpleValue(new Double(0.7)));
		    
		    FieldDefinition fd3 = new FieldDefinition("HighEducation", new SimpleValue(new Double(0.9)));
		    ArrayList<FieldDefinition> fuzzyFields = new ArrayList<>();
		    fuzzyFields.add(fd2);
		    fuzzyFields.add(fd1);	    
		    fuzzyFields.add(fd3);
		    
		    DocumentValue value = new DocumentValue(fuzzyFields);
		   		   
		    FieldDefinition fields = new FieldDefinition(JCOConstants.FUZZYSETS_FIELD_NAME, value);
		    DocumentDefinition df = new DocumentDefinition("nome");
		    df.addField(fields);
		    pipeline.setCurrentDoc(df);
		   
		    
		    UsingAggregatorPredicate usingAggregatorPredicate = new UsingAggregatorPredicate("agg");
		    //USING AGGREGATE THROUGH agg (MEMBERSHIPS OF [Rich, Young, HighEducation] );
		    usingAggregatorPredicate.aggregatorType = UsingAggregatorPredicate.SELECTED_FUZZY_SET_IN_DOCUMENT;
		    usingAggregatorPredicate.fuzzySetsSelected.add("Rich");
		    usingAggregatorPredicate.fuzzySetsSelected.add("Young");
		    usingAggregatorPredicate.fuzzySetsSelected.add("HighEducation");
		    
		    
		    System.out.println(FuzzyAggregatorEvaluator.evaluate(usingAggregatorPredicate, pipeline).toString());
		    
	  	} catch (Exception e) {
			System.out.println ("Parsing con ANTLR abortito\n\n");
			e.printStackTrace();
		}
		
		
		/*FuzzyAggregator fa = new FuzzyAggregator(0, "Aggregatore");
		
		
		FuzzyAggregatorCommand fac = new FuzzyAggregatorCommand(fa);*/
	}

}
