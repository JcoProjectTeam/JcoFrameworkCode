package jco.ql.tester;

import java.sql.Date;
import java.text.SimpleDateFormat;

import jco.ql.model.engine.JCOConstants;


public class ZunTester {

	public ZunTester() {	
    	SimpleDateFormat formatter= new SimpleDateFormat(JCOConstants.DATE_FORMAT_EXT);
    	Date date = new Date(System.currentTimeMillis());
    	System.out.println(formatter.format(date));
    	SimpleDateFormat formatter2= new SimpleDateFormat(JCOConstants.DATE_FORMAT);
    	Date date2 = new Date(System.currentTimeMillis());
    	System.out.println(formatter2.format(date2));
    	
		
	}

	public static void main(String[] args) {
		new ZunTester();
	}


}
