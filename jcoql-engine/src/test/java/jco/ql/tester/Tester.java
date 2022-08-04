package jco.ql.tester;

import jco.ql.engine.executor.threads.Primer;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Field;

public class Tester {

    public static void main (String[] args){
    	int prime = 102307;
    	System.out.println(Primer.getNextIteratively(prime));
    	System.out.println(Primer.getNextIterativelyFast(prime));
    	System.out.println(Primer.getNextRecursively(prime));
    	System.out.println(Primer.getNextRecursivelyFast(prime));
    	
    	FieldDefinition fd;
    	DocumentDefinition d1 = new DocumentDefinition();
    	fd = new FieldDefinition("paolo.fosci", new SimpleValue (3));
    	d1.addField(fd);
       	fd = new FieldDefinition("alpha.beta", new SimpleValue ("dammi la la penna"));
    	d1.addField(fd);
       	fd = new FieldDefinition("gamma.delta.omega", new SimpleValue ("mi hai dato la la penna"));
    	d1.addField(fd);
    	System.out.println("\n"+d1.hashCode()+"\n"+d1);   	

    	DocumentDefinition d2 = new DocumentDefinition();
    	fd = new FieldDefinition("paolo.fosci", new SimpleValue (3));
    	d2.addField(fd);
       	fd = new FieldDefinition("alpha.beta", new SimpleValue ("dammi la la penna"));
    	d2.addField(fd);
       	fd = new FieldDefinition("gamma.delta.omega", new SimpleValue ("mi hai dato la la penna"));
    	d2.addField(fd);
    	System.out.println("\n"+d2.hashCode()+"\n"+d2);   	
    	
    	DocumentDefinition d3 = new DocumentDefinition();
    	fd = new FieldDefinition("aspso", new SimpleValue (1));
    	d3.addField(fd);
       	fd = new FieldDefinition("cispa", new SimpleValue ("la penna"));
    	d3.addField(fd);
       	fd = new FieldDefinition("dada", new SimpleValue ("il martello"));
    	d3.addField(fd);
    	System.out.println("\n"+d3.hashCode()+"\n"+d3);   	

    	DocumentValue dv = new DocumentValue(d3);
    	fd = new FieldDefinition("subDoc", dv);
    	d1.addField(fd);
    	System.out.println("\n"+d1.hashCode()+"\n"+d1);   	

    	System.out.println("");
    	
    	DocumentDefinition d4 = new DocumentDefinition();
    	Field f = new Field ();
       	f.addField(".subDoc");
       	f.addField(".cispa");
        fd = d1.getField(f);
    	d4.insertField(f, fd.getValue());
    	System.out.println("\n"+d4.hashCode()+"\n"+d4);   	

    	f = new Field ();
       	f.addField(".teresarossanoaa");
        fd = d1.getField(f);
        System.out.println("sssss\t"+JCOValue.isNull(fd.getValue()));
    	d4.insertField(f, fd.getValue());
    	System.out.println("\n"+d4.hashCode()+"\n"+d4);   	
    	
    	SimpleValue v1 = new SimpleValue (true);
    	SimpleValue v2 = new SimpleValue (false);
    	System.out.println(v1.compareTo(v2));
    	
	} 
    
}