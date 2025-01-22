package jco.ql.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.StringTokenizer;

public class CsvFilter {
	static SimpleDateFormat dtFormat;
	static Date d0, d1;
	static HashSet<String> sensori;

	public static void main(String[] args) throws ParseException {
		BufferedReader reader;
		StringBuffer buf = new StringBuffer();
		int i,j,c,m;
		String idSensore,data, record;
		dtFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		d0=dtFormat.parse("01/05/2023 00:00:00");
		d1=dtFormat.parse("31/05/2023 23:59:59");
		
		leggiSensori();
		
		try {
			String fileName = "C:\\Users\\Unibg\\Documents\\J-CO\\.data\\.zunTestScripts\\11.TEST Webist 2023\\dati\\datiPioggia2023.csv";
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			buf.append(line+"\n");
			i=0;
			c=0;
			m=0;
			data="";
			idSensore="";
			while (line != null) {
				i++;
				if (i%100000==0)
					System.out.println(i);
				StringTokenizer stRecord = new StringTokenizer(line, ",");
				j=0;
				while (stRecord.hasMoreTokens()) {
					j++;
					record = stRecord.nextToken();
					if (j==1)
						idSensore = record;
					else if (j==2)
						data = record;
				}
				if (checkData (data)) {
					m++;
					if (sensori.contains(idSensore)){
						buf.append(line+"\n");
						c++;
					}
				}
				// read next line
				line = reader.readLine();				
			}
			System.out.println("\n"+i+"\t"+m+"\t"+c);
			saveFile(buf.toString());
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

    private static void leggiSensori()  {
    	sensori = new HashSet<String>(1001);
		String fileName = "C:\\Users\\Unibg\\Documents\\J-CO\\.data\\.zunTestScripts\\11.TEST Webist 2023\\dati\\idSensori.txt";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				sensori.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	private static boolean checkData(String data) {
		Date dt;
		try {
			dt = dtFormat.parse(data);
		} catch (ParseException e) {
			return false;
		}
		if (dt.before(d0))
			return false;
		if (dt.after(d1))
			return false;
		return true;
	}

	public static void saveFile(String str) {

        try {
    		String fileName = "C:\\Users\\Unibg\\Documents\\J-CO\\.data\\.zunTestScripts\\11.TEST Webist 2023\\dati\\datiPioggia.csv";
            File newTextFile = new File(fileName);

            FileWriter fw = new FileWriter(newTextFile);
            fw.write(str);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
        }
    }
}
