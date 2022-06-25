package jco.ql.tester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import jco.ql.byZun.ZunProperties;

public class ZunDatasetDuplicator{  // originale Hal

	static String readFile (String fn) throws Exception {
		String data = "";
		File myObj = new File(fn);
		Scanner myReader = new Scanner(myObj);
		long l=0;
		boolean first = true;
		while (myReader.hasNextLine()) {
			l++;
			if (l % 10000 == 0)
				System.out.println(fn + "\t" + l);
			String line = myReader.nextLine().trim();
			if (line.indexOf("NumberLong(")>0) 
				line = line.replace("NumberLong(", "").replace(")", "");

			if (line.startsWith("/*")) {
				if (first)
					first = false;
				else
					  data += ",\n";					
			} 

			if (!line.startsWith("/*") && !line.startsWith("\"_id\""))
			  data += line ;
		}
		myReader.close();	  	
		return data;
	}

	private static void saveTo(String buf, String fn, int i) throws Exception {
		String fnx = ZunProperties.getScriptPath() + "dataset\\" + fn + i + ".txt";
		File file = new File(fnx);
		file.createNewFile();

		System.out.println("\nsave to:\t" + fnx);
		FileWriter fw = new FileWriter(file);
		BufferedWriter b = new BufferedWriter(fw);
		b.write(buf);
		b.close();
		
	}

	
	private static void duplicate(String data, String fn) throws Exception  {
		String buf="[\n";
		System.out.println(fn);
		boolean first = true;
		for (int i = 0; i < 100; i++) {
			if (first)
				first = false;
			else
				buf +=",\n";
			buf += data;
			System.out.print((i+1) +"\t");
			if ((i == 9) || (i == 19) || (i == 24) || (i == 49) || (i == 74))
				saveTo (buf + "\n]", fn, i+1);
		}	
		buf += "\n]";
		saveTo (buf, fn, 100);

	}

	public static void main(String[] args) throws Exception{
		String fnb10 = ZunProperties.getScriptPath() + "dataset\\bikelanes.txt";
		String fnd10 = ZunProperties.getScriptPath() + "dataset\\districts.txt";
		 String current = new java.io.File( "." ).getCanonicalPath();
	        System.out.println("Current dir:"+current);
	 String currentDir = System.getProperty("user.dir");
	        System.out.println("Current dir using System:" +currentDir);
		String b10 = readFile (fnb10);
		String d10 = readFile (fnd10);
		
		duplicate (d10, "Districts");
		duplicate (b10, "BikeLanes");
		
		
            // print a message
        /*        Robot hal = new Robot();
        Random random = new Random();
        while(true){
            hal.delay(100 * 60);
            int x = Math.abs(random.nextInt() % 640);
            int y = Math.abs(random.nextInt() % 480);
            System.out.println("x:\t"+x + "\t\ty:\t"+y);
            hal.mouseMove(x,y);
            
        }
 */   }

}