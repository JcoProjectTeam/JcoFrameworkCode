package jco.ql.tester;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.antlr.runtime.RecognitionException;

import jco.ql.byZun.ZunProperties;
import jco.ql.byZun.ZunTimer;
import jco.ql.byZun.ZunWarningTracker;
import jco.ql.engine.EngineConfiguration;
import jco.ql.engine.parser.ParserLauncher;
import jco.ql.model.command.FuzzyOperatorCommand;
import jco.ql.model.engine.JMH;
import jco.ql.parser.JCoQLParser;

public class ZunScriptTester {
	static String ver = "1.02";
	FuzzyOperatorCommand f;

	static String getScript (String fileIn) throws IOException, RecognitionException {
		ZunProperties.setVer(ver);
		
	  	StringBuffer data = new StringBuffer ();
		File myObj = new File(ZunProperties.getScriptPath() + fileIn);
		Scanner myReader = new Scanner(myObj);
		while (myReader.hasNextLine()) {
			data.append(myReader.nextLine()+"\n");
		}
		myReader.close();	  	

		return data.toString();
	}

	
	public static void main(String[] args) {
		String propFileName = ZunProperties.getScriptPath() + "jco.properties";
		
		if (args.length>0)
			propFileName = args[0];

		try {
			ZunWarningTracker.getInstance().addWarning("*****\t\tStart J-co Engine Ver. " + ver + "\tTest:\t" + propFileName + "\t*****");
			FileReader reader=new FileReader(propFileName);
			Properties prop = new Properties();
			prop.load(reader);
			String fileInProlog = prop.getProperty("fileInProlog");		
			String fileInTest  = prop.getProperty("fileInTest");		
			String testName  = prop.getProperty("testName");		
			boolean toScreen = "true".equalsIgnoreCase(prop.getProperty("toScreen"));
			reader.close();
			
			ZunWarningTracker.getInstance().toggleToScreen(toScreen);
			ZunWarningTracker.getInstance().addWarning("Test Name:   \t" + testName);
			ZunWarningTracker.getInstance().addWarning("Parser ver.: \t" + JCoQLParser.release);
			ZunWarningTracker.getInstance().addWarning("Properties:  \t" + propFileName);
			ZunWarningTracker.getInstance().addWarning("FileInProlog:\t" + fileInProlog);
			ZunWarningTracker.getInstance().addWarning("FileInTest:  \t" + fileInTest);
			ZunWarningTracker.getInstance().addWarning("th:  \t" + EngineConfiguration.getNProcessors()+ "\n");

			
		  	String prolog = getScript (fileInProlog);
		  	String script = getScript (fileInTest); 
 
			for (int i=0; i<1; i++) {
				//				ZunTimer.getInstance().reset();
//				ZunTimer.getInstance().getPartial("***** Start Cycle " + (i+1) + ":");
				ParserLauncher p = new ParserLauncher();
				System.gc();
			
				ZunWarningTracker.getInstance().addWarning("Test. Start executing prolog instructions:\n " + prolog);
				p.parse(prolog);
	
				ZunWarningTracker.getInstance().addWarning("Test. Start executing test instructions:\n " + script);
				p.parse(script);
//				ZunTimer.getInstance().getPartial("***** End Cycle " + i);
//				ZunTimer.getInstance().getTotal("##### Total execution time");
//				ZunTimer.getInstance().getMilliTotal("##### Total execution time");
			}
			ZunWarningTracker.getInstance().addWarning("th:  \t" + EngineConfiguration.getNProcessors()+ "\n");
			ZunWarningTracker.getInstance().println();
		    List<String> list = JMH.getConfigurationChannelDev();
		    System.out.println("-------------------- Configuration ChannelDev ------------------------");
			ZunWarningTracker.add("-------------------- Configuration ChannelDev ------------------------");
		    int i=0;
		    for (String msg : list) {
				ZunWarningTracker.add(i + ".\t" + msg);
				System.out.println(i++ + ".\t" + msg);
			}
		    list = JMH.getJcoChannel();
		    System.out.println("-------------------- JCO Channel ---------------------------");
		    i=0;
		    for (String msg : list) {
				System.out.println(i++ + ".\t" + msg);
			}
		    list = JMH.getMainChannel();
		    System.out.println("-------------------- Main Channel ---------------------------");
			ZunWarningTracker.add("-------------------- Main ChannelDev ------------------------");
		    i=0;
		    for (String msg : list) {
				ZunWarningTracker.add(i + ".\t" + msg);
				System.out.println(i++ + ".\t" + msg);
			}
		    list = JMH.getMainChannelDev();
		    System.out.println("-------------------------------------------------------------");

		    ZunWarningTracker.getInstance().saveToFile();		
		    ZunTimer.getInstance().println();
		    ZunTimer.getInstance().saveToFile();
		   
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			ZunWarningTracker.getInstance().addWarning("ECCEZIONE:\n" + sw.toString());
			ZunWarningTracker.getInstance().println();
			ZunWarningTracker.getInstance().saveToFile();
			ZunTimer.getInstance().getTotal("##### CRASH ##### \n+ " + sw.toString());
			ZunTimer.getInstance().saveToFile();			
		    List<String> list = JMH.getMainChannel();
		    System.out.println("----------------------------------");
		    int i=0;
		    for (String msg : list) {
				System.out.println(i++ + ".\t" + msg);
			}
		    list = JMH.getMainChannelDev();
		    System.out.println("----------------------------------");
		    i=0;
		    for (String msg : list) {
				System.out.println(i++ + ".\t" + msg);
			}
		}
		System.exit(0);
	}

}
