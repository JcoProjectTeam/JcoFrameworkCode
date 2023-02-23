package jco.ql.tester;



public class ZunTester {
	
	public ZunTester() {	
		double x = MyGeoClass2.GeoKmDistance(12.2, 14.21, 9.32, 14.34);
		System.out.println(x);
		double y = MyStringClass2.JaroWinklerSimilarity("Ciao Mondo","Ciao Mondino!");
		System.out.println(y);
    	
		
	}

	public static void main(String[] args) {
		new ZunTester();
	}


}
