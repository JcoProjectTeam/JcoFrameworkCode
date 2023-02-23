package jco.ql.model.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* *************************
 * JMH: JCo Messsage Handler
 * ************************* */
public class JMH {
	public static final int CONFIGURATION_CHANNEL = 0;
	public static final int MAIN_CHANNEL = 1;
	public static final int PARSER_CHANNEL = 2;
	public static final int JCO_CHANNEL = 3;
	public static final int IO_CHANNEL = 4;
	public static final int DS_CHANNEL = 5;
	public static final int JS_CHANNEL = 6;
	public static final int FUZZY_CHANNEL = 7;
	public static final int EXCEPTION_CHANNEL = 8;
	
	// singleton
    private static JMH only = new JMH();
    
    // available channels: mainChannel includes all the other ones except configurationChannel that can't be resetted
    private List<String> configurationChannel;
    private List<String> mainChannel;
    private List<String> parserChannel;
    private List<String> jcoChannel;
    private List<String> ioChannel;
    private List<String> dsChannel;
    private List<String> jsChannel;
    private List<String> fuzzyChannel;
    private List<String> exceptionChannel;
    // For developers: same as previous ones but with JCO code reference
    private List<String> configurationChannelDev;
    private List<String> mainChannelDev;
    private List<String> parserChannelDev;
    private List<String> jcoChannelDev;
    private List<String> ioChannelDev;
    private List<String> dsChannelDev;
    private List<String> jsChannelDev;
    private List<String> fuzzyChannelDev;
    private List<String> exceptionChannelDev;
    private boolean toScreen;

    public JMH() {
        this.toScreen = false;
        
        this.configurationChannel = new ArrayList<>();
        this.mainChannel = new ArrayList<>();
        this.parserChannel = new ArrayList<>();
        this.jcoChannel = new ArrayList<>();
        this.ioChannel = new ArrayList<>();
        this.dsChannel = new ArrayList<>();
        this.jsChannel = new ArrayList<>();
        this.fuzzyChannel = new ArrayList<>();
        this.exceptionChannel = new ArrayList<>();

        this.configurationChannelDev = new ArrayList<>();
        this.mainChannelDev = new ArrayList<>();
        this.parserChannelDev = new ArrayList<>();
        this.jcoChannelDev = new ArrayList<>();
        this.ioChannelDev = new ArrayList<>();
        this.dsChannelDev = new ArrayList<>();
        this.jsChannelDev = new ArrayList<>();
        this.fuzzyChannelDev = new ArrayList<>();
        this.exceptionChannelDev = new ArrayList<>();

        //    	SimpleDateFormat formatter= new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS");
    	SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss.SSS");
    	Date date = new Date(System.currentTimeMillis());
        this.mainChannelDev.add(formatter.format(date) + " -> JCO Message Handler started");
    }

    // restart JMH
    public static void reset () {
        only.mainChannel = new ArrayList<>();
        only.parserChannel = new ArrayList<>();
        only.jcoChannel = new ArrayList<>();
        only.ioChannel = new ArrayList<>();
        only.dsChannel = new ArrayList<>();
        only.jsChannel = new ArrayList<>();
        only.fuzzyChannel = new ArrayList<>();
        only.exceptionChannel = new ArrayList<>();

        only.mainChannelDev = new ArrayList<>();
        only.parserChannelDev = new ArrayList<>();
        only.jcoChannelDev = new ArrayList<>();
        only.ioChannelDev = new ArrayList<>();
        only.dsChannelDev = new ArrayList<>();
        only.jsChannelDev = new ArrayList<>();
        only.fuzzyChannelDev = new ArrayList<>();
        only.exceptionChannelDev = new ArrayList<>();

        //    	SimpleDateFormat formatter= new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSS");
    	SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss.SSS");
    	Date date = new Date(System.currentTimeMillis());
        only.mainChannelDev.add(formatter.format(date) + " -> Reset JCO Message Handler");
    }

    
    public static void toggleToScreen(boolean toScreen) {
    	only.toScreen = toScreen;
    }


    public static boolean reportErrors () {
    	return (only.parserChannel.size() > 0);
    }
    public static boolean reportNoErrors () {
    	return (only.parserChannel.size() == 0);
    }

    public static String add (String msg) {
    	return only.execAddMessage(MAIN_CHANNEL, msg);
    }

    public static String add (int channel, String msg) {
    	return only.execAddMessage(channel, msg);
    }

    public static String addConfigurationMessage (String msg) {
    	return only.execAddMessage(CONFIGURATION_CHANNEL, msg);
    }

    public static String addParserMessage (String msg) {
    	return only.execAddMessage(PARSER_CHANNEL, msg);
    }

    public static String addJCOMessage (String msg) {
    	return only.execAddMessage(JCO_CHANNEL, msg);
    }

    public static String addIOMessage (String msg) {
    	return only.execAddMessage(IO_CHANNEL, msg);
    }

    public static String addDSMessage (String msg) {
    	return only.execAddMessage(DS_CHANNEL, msg);
    }

    public static String addJSMessage (String msg) {
    	return only.execAddMessage(JS_CHANNEL, msg);
    }

    public static String addFuzzyMessage (String msg) {
    	return only.execAddMessage(FUZZY_CHANNEL, msg);
    }

    public static String addExceptionMessage (String msg) {
    	return only.execAddMessage(EXCEPTION_CHANNEL, msg);
    }


    public static List<String> getChannel (int channel) {
        if (channel == MAIN_CHANNEL)
        	return only.parserChannel;
        if (channel == PARSER_CHANNEL)
        	return only.parserChannel;
        if (channel == JCO_CHANNEL)
        	return only.jcoChannel;
        if (channel == IO_CHANNEL)
        	return only.ioChannel;
        if (channel == DS_CHANNEL)
        	return only.dsChannel;
        if (channel == JS_CHANNEL)
        	return only.jsChannel;
        if (channel == FUZZY_CHANNEL)
        	return only.fuzzyChannel;
        if (channel == EXCEPTION_CHANNEL)
        	return only.exceptionChannel;
        return null;
    }

    public static List<String> getChannelDev (int channel) {
        if (channel == MAIN_CHANNEL)
        	return only.parserChannelDev;
        if (channel == PARSER_CHANNEL)
        	return only.parserChannelDev;
        if (channel == JCO_CHANNEL)
        	return only.jcoChannelDev;
        if (channel == IO_CHANNEL)
        	return only.ioChannelDev;
        else if (channel == DS_CHANNEL)
        	return only.dsChannelDev;
        else if (channel == JS_CHANNEL)
        	return only.jsChannelDev;
        else if (channel == FUZZY_CHANNEL)
        	return only.fuzzyChannelDev;
        else if (channel == EXCEPTION_CHANNEL)
        	return only.exceptionChannelDev;
        return null;
    }

    public static List<String> getConfigurationChannel() {
        return only.configurationChannel;
    }

    public static List<String> getConfigurationChannelDev() {
        return only.configurationChannelDev;
    }

    public static List<String> getMainChannel() {
        return only.mainChannel;
    }

    public static List<String> getMainChannelDev() {
        return only.mainChannelDev;
    }

    public static List<String> getParserChannel() {
        return only.parserChannel;
    }

    public static List<String> getParserChannelDev() {
        return only.parserChannelDev;
    }

    public static List<String> getJcoChannel() {
        return only.jcoChannel;
    }

    public static List<String> getJcoChannelDev() {
        return only.jcoChannelDev;
    }

    public static List<String> getIOChannel() {
        return only.ioChannel;
    }

    public static List<String> getIOChannelDev() {
        return only.ioChannelDev;
    }

    public static List<String> getDSChannel() {
        return only.dsChannel;
    }

    public static List<String> getDSChannelDev() {
        return only.dsChannelDev;
    }

    public static List<String> getJSChannel() {
        return only.jsChannel;
    }

    public static List<String> getJSChannelDev() {
        return only.jsChannelDev;
    }

    public static List<String> getFuzzyChannel() {
        return only.fuzzyChannel;
    }

    public static List<String> getFuzzyChannelDev() {
        return only.fuzzyChannelDev;
    }

    public static List<String> getExceptionChannel() {
        return only.exceptionChannel;
    }

    public static List<String> getExceptionChannelDev() {
        return only.exceptionChannelDev;
    }


    private String execAddMessage(int channel, String msg) {
    	SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss.SSS");
    	Date date = new Date(System.currentTimeMillis());
    	String tm = formatter.format(date);

		// Nello stacktrace: il 0 è questo metodo, il 1 è in chiamante interno, il 2 è il chimante esterno
		StackTraceElement ste = new Throwable().getStackTrace()[2];
		String [] pcn = ste.getClassName().split("\\.");
		String cn = pcn [pcn.length-1];
		String x = cn + "." + ste.getMethodName() + "(" + ste.getLineNumber() + ") ";

		String st = tm + " -> " + x + "\n" + msg;

		this.mainChannel.add(msg);
		this.mainChannelDev.add(st);

		if (channel == CONFIGURATION_CHANNEL) {
        	this.configurationChannel.add(msg);
        	this.configurationChannelDev.add(st);
        }
		if (channel == PARSER_CHANNEL) {
        	this.parserChannel.add(msg);
        	this.parserChannelDev.add(st);
        }
        else if (channel == JCO_CHANNEL) {
        	this.jcoChannel.add(msg);
        	this.jcoChannelDev.add(st);
        }
        else if (channel == IO_CHANNEL) {
        	this.ioChannel.add(msg);
        	this.ioChannelDev.add(st);
        }
        else if (channel == DS_CHANNEL) {
        	this.dsChannel.add(msg);
        	this.dsChannelDev.add(st);
        }
        else if (channel == JS_CHANNEL) {
        	this.jsChannel.add(msg);
        	this.jsChannelDev.add(st);
    	}
        else if (channel == FUZZY_CHANNEL) {
        	this.fuzzyChannel.add(msg);
        	this.fuzzyChannelDev.add(st);
		}
        else if (channel == EXCEPTION_CHANNEL) {
        	this.exceptionChannel.add(msg);
        	this.exceptionChannelDev.add(st);
		}    	

        if (toScreen)
    		System.out.println(st);
        return st;
    }


    public static void saveToFile (String fileName, String path, boolean deleteOnExit) {
    	fileName = "JMHLog - " + fileName + ".txt";
    	String fileOut = path + fileName;

		try {
			File file = new File(fileOut);
			file.createNewFile();
			if (deleteOnExit)
				file.deleteOnExit();

			FileWriter fw = new FileWriter(file);
			BufferedWriter b = new BufferedWriter(fw);
			for (String msg: JMH.getMainChannelDev()) {
				b.write(msg);
				b.newLine();
			}
			b.close();
		} 
		catch (Exception e) {
			System.out.println ("*************************\n" +
								"**** SAVE LOG FAILED ****\n" +
								"*************************\n");
			e.printStackTrace();
		}
	}

}
