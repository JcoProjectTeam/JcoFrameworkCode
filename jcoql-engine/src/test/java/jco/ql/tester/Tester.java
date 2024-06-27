package jco.ql.tester;


public class Tester {

	// yw : must be in the form "YYYY-WW"
	public static int getWeek(String yw)  {
    	try {
    		return Integer.parseInt(yw.substring(5,7));
    	}
    	catch (Exception e) {
    		return 0;
    	}    	
    }  
	// yw : must be in the form "YYYY-WW"
	public static int getYear(String yw)  {
    	try {
    		return Integer.parseInt(yw.substring(0,4));
    	}
    	catch (Exception e) {
    		return 0;
    	}    	
    }  

	//driver code  
    public static void main (String args[])  
    {  
    	String yw = "2023-1434";
    	System.out.println ( getYear(yw));
    	System.out.println ( getWeek(yw));
    }  

}
	 
	 