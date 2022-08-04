package jco.ql.byZun;


public class ZunTicker {

    private static ZunTicker only = new ZunTicker();
    private String label;
    private int step, maxStep, stint, nStint, stintLen;
    private long t0, t1, t2;

    private ZunTicker() {
    	label = "Default task";
    	step = 0;
    	maxStep = 10000000;
    	nStint = 100;
    	stintLen = maxStep / nStint;
        t0 = System.nanoTime();
        t1 = t0;
        t2 = t0;
    }
    
    public static ZunTicker getInstance() {
        return only;
    }

    public static void reset (String label, int maxStep, int nStint) {
    	only.doReset(label, maxStep, nStint);
    }
    
	public  static void tick () {
    	only.doTick();
    }

    private void doReset(String label, int maxStep, int nStint) {
        t0 = System.nanoTime();
        t1 = t0;
        t2 = t0;
    	
        this.label = label;
    	step = 0;
    	stint = 0;
    	if (maxStep == 0)
    		maxStep = 1;
    	if (nStint == 0)
    		nStint = 1;
    	if (nStint > maxStep)
    		nStint = maxStep;
    	stintLen = maxStep / nStint;
    	if (stintLen == 0)
    		stintLen = 1;
    	nStint = maxStep / stintLen;
    	this.nStint = nStint;
    	this.maxStep = maxStep;
    	printReport ();
	}

	private void doTick() {
    	step++;
    	if (step % stintLen == 0) {
        	t1 = t2;
        	t2 = System.nanoTime();
    		stint++;
    	   	printReport ();
    	}
	}

	
	private void printReport() {
		System.out.print("Ticker - " + label + ":\t" + stint + "/" + nStint + "\t[" + ((1000 * stint / nStint)/10.0) + "%]\t");		
		System.out.println("Tpar: " + ((t2-t1)/1000000) + "ms\tTtot: " + ((t2-t0)/1000000) + "ms");		
	}

    
}