package jco.ql.byZun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jco.ql.model.engine.JCOConstants;


public class ZunTimer {

    private static ZunTimer only = new ZunTimer();
    private List<String> times;
    private boolean toScreen;
    private long t0, t1, t2;

    private ZunTimer() {
        this.t0 = System.nanoTime();
        this.t1 = System.nanoTime();
        this.t2 = System.nanoTime();
        this.times = new ArrayList<>();
        this.toScreen = false;
    	SimpleDateFormat formatter= new SimpleDateFormat(JCOConstants.DATE_FORMAT_EXT);
    	Date date = new Date(System.currentTimeMillis());
        this.times.add("ZunTimer started at:\t" + formatter.format(date));
    }

    public static String memStatus () {
    	String mem =  "\t" + (Runtime.getRuntime().freeMemory()/(1024*1024)) + "M\t" + 
				(Runtime.getRuntime().totalMemory()/(1024*1024)) + "M\t" + 
				(Runtime.getRuntime().maxMemory()/(1024*1024)) + "M";
    	return mem;
    }

    
    public static ZunTimer getInstance() {
        return only;
    }

    
    public void toggleToScreen(boolean toScreen) {
    	this.toScreen = toScreen;
    }
    

    // reset of partial timer
    public void reset () {
    	t1 = System.nanoTime();
    }
  
    // get partial time since last reset
    public String getPartialMem (String label) {
    	t2 = System.nanoTime();
    	String st = label + "\t" + (t2-t1) ;
    	times.add(st + memStatus());
    	if (toScreen)
    		System.out.println(st);
    	return st;
    }
  
    // get partial time since last reset
    public String getPartial (String label, int divide) {
    	t2 = System.nanoTime();
    	String st = label + "\t" + (t2-t1)/divide;
    	times.add(st);
    	if (toScreen)
    		System.out.println(st);
    	return st;
    }
  
    // get total time since beginning
    public String getTotal (String label, int divide) {
    	t2 = System.nanoTime();
    	String st = label + "\t" + (t2-t0)/divide;
    	times.add(st);
    	if (toScreen)
    		System.out.println(st);
    	return st;
    }

    // get partial time since last reset total time since beginning
    public String getPartialAndTotal (String label, int divide) {
    	t2 = System.nanoTime();
    	String st = label + "\t" + (t2-t1)/divide + "\t" + (t2-t0)/divide;
    	times.add(st);
    	if (toScreen)
    		System.out.println(st);
    	return st;
    }

    // get partial time since last reset
    public String getPartial (String label) {
    	return getPartial (label, 1);
    }
  
    // get total time since beginning
    public String getTotal (String label) {
    	return getTotal (label, 1);
    }

    // get partial time since last reset total time since beginning
    public String getPartialAndTotal (String label) {
    	return getPartialAndTotal (label, 1);
    }
  
    // get partial time since last reset
    public String getMicroPartial (String label) {
    	return getPartial (label, 1000);
    }
  
    // get total timetime since beginning
    public String getMicroTotal (String label) {
    	return getTotal (label, 1000);
    }

    // get partial time since last reset
    public String getMicroPartialAndTotal (String label) {
    	return getPartialAndTotal (label, 1000);
    }
  
    // get partial time since last reset
    public String getMilliPartial (String label) {
    	return getPartial (label, 1000000);
    }
  
    // get total timetime since beginning
    public String getMilliTotal (String label) {
    	return getPartialAndTotal (label, 1000000);
    }

    // get partial time since last reset
    public String getMilliPartialAndTotal (String label) {
    	return getPartialAndTotal (label, 1000000);
    }
  

    public List<String> getTimes() {
        return new ArrayList<>(this.times);
    }
        
    public void println () {
		System.out.println("\nLogged times:\t"+ ZunTimer.getInstance().getTimes().size());
		for (String time: ZunTimer.getInstance().getTimes()) {
			System.out.println(time);
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
    void saveToFile (String fileName, boolean deleteOnExit) {
    	fileName = "ZunTimer V." + ZunProperties.getVer() + "-" + fileName + "-0" + ZunProperties.getRnd() + ".txt" ;
    	String path = ZunProperties.getLogPath();
    	String fileOut = path + fileName;

		try {
			File file = new File(fileOut);
			file.createNewFile();
			if (deleteOnExit)
				file.deleteOnExit();

			FileWriter fw = new FileWriter(file);
			BufferedWriter b = new BufferedWriter(fw);
			for (String time: ZunTimer.getInstance().getTimes()) {
				b.write(time);
				b.newLine();
			}
			b.close();

	} catch (Exception e) {
			System.out.println ("********************************\n" +
								"**** SAVE LOG TIMER FALLITO ****\n" +
								"********************************\n");
			e.printStackTrace();
		}    	
    }
    
}