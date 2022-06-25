package jco.ql.byZun;


public class ZunProperties {

    private static ZunProperties only = new ZunProperties();

	private int rnd;
	private String ver;
    private String scriptPath;
    private String logPath;
    private String reportPath;

    
    private ZunProperties() {
        this.rnd = (int) (1000*Math.random());
        this.scriptPath = "..\\..\\.data\\.zunTestScripts\\";
        this.logPath = "..\\..\\.data\\.zunLogs\\";
        this.reportPath = "..\\..\\.data\\.zunReports\\";
        this.ver = "";
    }

    public static ZunProperties getInstance() {
        return only;
    }

    public static void setVer(String ver) {
		only.ver = ver;
	}
    public static String getVer() {
		return only.ver;
	}

    public static int getRnd() {
		return only.rnd;
	}

	public static String getScriptPath() {
		return only.scriptPath;
	}

	public static String getLogPath() {
		return only.logPath;
	}

	public static String getReportPath() {
		return only.reportPath;
	}
}
