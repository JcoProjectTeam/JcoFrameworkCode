package jco.ql.tester;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;

public class Tester {
	class MyPoint {
		public double x;
		public double y;
		public MyPoint (double x, double y) {
			this.x=x;
			this.y=y;
		}
	}

    public static void main (String[] args){
    	Tester t = new Tester();
    	t.go();
    	
	} 
    
    
    public double dArea(MyPoint p0, MyPoint p1) {
    	return (p1.x-p0.x)*(p1.y+p0.y)/2;
    }
    public void go() {
    	ArrayList<MyPoint> p = new ArrayList<MyPoint>();
    	p.add(new MyPoint (0,0));
    	p.add(new MyPoint (0,1));
    	p.add(new MyPoint (1,1));
    	p.add(new MyPoint (1,0));
    	p.add(new MyPoint (0,0));
    	
    	double a1 =0;
    	double a2 =0;
    	
    	for (int i=0; i<p.size()-1; i++) {    		
    		MyPoint p0, p1;
    		p0=p.get(i);
    		p1=p.get(i+1);
    		a1 +=dArea (p0, p1);
    		a2 +=dArea (p1, p0);
    	}
    	
    	System.out.println("A1:\t" + a1);
    	System.out.println("A2:\t" + a2);
    }
    
    
    
    
    public void geoCalc () {
//    	Geometry geo = new Geo;
    }
}