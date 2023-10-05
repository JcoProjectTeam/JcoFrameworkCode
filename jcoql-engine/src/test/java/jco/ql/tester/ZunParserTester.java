package jco.ql.tester;


import java.io.FileReader;

import org.antlr.runtime.ANTLRReaderStream;

import jco.ql.byZun.ZunProperties;
import jco.ql.parser.*;
import jco.ql.parser.model.Instruction;

public class ZunParserTester {
	
	
	public static void main(String[] args) {
		JCoQLParser parser;
//		String fileIn = "..\\.zunTestScripts\\testFQAS - stations and sensors.txt";
//		String fileIn = "..\\.zunTestScripts\\t.txt";
//		String fileIn = "..\\.zunTestScripts\\testInformation2021.txt";
//		String fileIn = "..\\.zunTestScripts\\testFQAS.txt";
//		String fileIn = "..\\.zunTestScripts\\testFQAS - normalize data2.txt";
//  	String fileout = "..\\.zunTestScripts\\output.file";
		String fileIn = ZunProperties.getScriptPath() + "testInformation2021 2-3.txt";
		fileIn = ZunProperties.getScriptPath() + "scriptTest.txt";
		fileIn = ZunProperties.getScriptPath() + "prova.txt";
		fileIn = ZunProperties.getScriptPath() + "testAbdis.txt";
		fileIn = ZunProperties.getScriptPath() + "5.TEST IJGI\\testIJGI.txt";
		fileIn = ZunProperties.getScriptPath() + "5.TEST IJGI\\test.txt";
		fileIn = ZunProperties.getScriptPath() + "\\testJava\\test.txt";
		fileIn = ZunProperties.getScriptPath() + "\\2.TEST NeuroComputing\\1.testNeurocomputingJs.txt";
//		fileIn = ZunProperties.getScriptPath() + "\\2.TEST NeuroComputing\\2.testNeurocomputingJavaEmbedded.txt";
//		fileIn = ZunProperties.getScriptPath() + "\\2.TEST NeuroComputing\\3.testNeurocomputingJava.txt";
		fileIn = ZunProperties.getScriptPath() + "\\10.TEST Soco 2023\\script.txt";
		fileIn = ZunProperties.getScriptPath() + "\\scriptFA.txt";
		fileIn = ZunProperties.getScriptPath() + "\\11.TEST Webist 2023\\script.txt";

  	try {
  		// Inizializzazione del parser (antlr docet):
  		//		1. Si inizializza il lexer
  		//		2. si crea un token stream - DEPRECATO
  		//		3. si istanzia il parser passandogli lo stream dei token - DEPRECATO
  		//		3new. si istanzia il parser passandogli lo scanner

  		// 1.
  			JCoQLLexer lexer = new JCoQLLexer(new ANTLRReaderStream(new FileReader(fileIn))); 
			// 2. deprecato
//		CommonTokenStream tokens = new CommonTokenStream(lexer);
			// 3. deprecato
//    parser = new JcoParser(tokens);
			// 3. new
			parser = new JCoQLParser(lexer);

			
	    // si lancia il parser
	    parser.start();
	    
	    // la classe Environment contiene tutte le info relative all'analisi del parser
	    Environment env =	parser.getEnvironment();
	    

	    // numero delle istruzioni
	    System.out.println("Parser " + env.getVersion() + " - Release " + env.getRelease() + ":\tNumero di istruzioni analizzate: " + env.getNInstruction());

	    System.out.println("\nLista degli errori (" + env.getErrorList().size() + "):");
	    // lista degli errori (eventuali)
	    for (int i=0;i<env.getErrorList().size();i++) {
	    	System.out.println((i+1) + ".\t" + parser.getErrorList().get(i));
	    }
	    
	    System.out.println("\nLista delle istruzioni:");
	    // lista delle istruzioni
	    if (env.hasNoError())
		    for (int i=0;i<env.getInstructionList().size();i++) {
		    	doSomethingWithInstruction (env.getInstructionList().get(i));
	    }	    
  	
  	} catch (Exception e) {
			System.out.println ("Parsing con ANTLR abortito\n\n");
			e.printStackTrace();
		}
  }

	
	// in this case I just print out instruction text
	static void doSomethingWithInstruction (Instruction instr) {
		System.out.println(instr.toMultilineString());
		System.out.println();
	}
}
