package jco.ql.byZun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ZunWarningTracker {

    private static ZunWarningTracker only = new ZunWarningTracker();
    private List<String> warnings;
    private boolean toScreen;
    private int nResets;
    private boolean bool;

    private ZunWarningTracker() {
        this.nResets = 0;
        this.warnings = new ArrayList<>();
        this.toScreen = false;
    	SimpleDateFormat formatter= new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS");
    	Date date = new Date(System.currentTimeMillis());
        this.warnings.add("ZunWarnigTracker started at:\t" + formatter.format(date));
        this.bool = true;
    }

    public static ZunWarningTracker getInstance() {
        return only;
    }
    // shortcut for ZunWarningTracker.getInstance ().addWarning();
    public static String add (String warning) {
    	return only.execAddWarning(0, warning);
    }
    // shortcut for ZunWarningTracker.getInstance ().addWarning();
    public static String add (int lev, String warning) {
    	return only.execAddWarning(lev, warning);
    }


    public void check () {
    	if (bool) {
    		bool=false;
    		String s="";
    		StackTraceElement[] ste = new Throwable().getStackTrace();
    		for (int i = 0; i < ste.length; i++) {
				s+=ste[i].toString()+"\n";
			}
    		addWarning("******* CHECK FIELD *******\n"+s);
    	}
    }

    public void toggleToScreen(boolean toScreen) {
    	this.toScreen = toScreen;
    }

    public String addWarning(String warning) {
    	return execAddWarning(0, warning);
    }
    public String addWarning(int lev, String warning) {
    	return execAddWarning(lev, warning);
    }
    private String execAddWarning(int lev, String warning) {
    	SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss.SSS");
    	Date date = new Date(System.currentTimeMillis());
    	String tm = formatter.format(date);

    	String tab = "";
    	for (int i=0; i<lev; i++)
    		tab += "\t";
		// Nello stacktrace: il 0 è questo metodo, il 1 è in chiamante interno, il 2 è il chimante esterno
		StackTraceElement ste = new Throwable().getStackTrace()[2];
		String [] pcn = ste.getClassName().split("\\.");
		String cn = pcn [pcn.length-1];
		String x = cn + "." + ste.getMethodName() + "(" + ste.getLineNumber() + ") ";
		int l = 70 - x.length();
		for (int i=0; i<l; i++)
			x+=" ";
		x += "\t";
		String st = tm + " -> " + nResets + "-" + x + tab + warning;
        this.warnings.add(st);
    	if (toScreen)
    		System.out.println(st);
        return st;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public void reset () {
    	nResets++;
    }

    public void println () {
		System.out.println("\n*ZUN cycles:\t" + nResets + "*\n");
		for (String warning: ZunWarningTracker.getInstance().getWarnings()) {
			System.out.println(warning);
		}
    }


    public void saveToFile () {
    	SimpleDateFormat formatter= new SimpleDateFormat("yyyy.MM.dd'T'HH.mm.ss");
    	Date date = new Date(System.currentTimeMillis());
    	String fileOut = formatter.format(date);
    	saveToFile(fileOut, false);
	}    
    public void saveToFile (String fileName) {
    	saveToFile(fileName, true);
	}    
    public void saveToFile (String fileName, boolean deleteOnExit) {
    	fileName = "ZunLog V." + ZunProperties.getVer() + "-" + fileName + "-0" + ZunProperties.getRnd() + ".txt";
    	String path = ZunProperties.getLogPath();
    	String fileOut = path + fileName;

		try {
			File file = new File(fileOut);
			file.createNewFile();
			if (deleteOnExit)
				file.deleteOnExit();

			FileWriter fw = new FileWriter(file);
			BufferedWriter b = new BufferedWriter(fw);
			for (String warning: ZunWarningTracker.getInstance().getWarnings()) {
				b.write(warning);
				b.newLine();
			}
			b.close();

		} catch (Exception e) {
			System.out.println ("*************************\n" +
								"**** SAVE LOG FAILED ****\n" +
								"*************************\n");
			e.printStackTrace();
		}
	}


}