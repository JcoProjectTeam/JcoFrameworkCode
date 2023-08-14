package jco.ql.tester;

import jco.ql.model.value.ArrayValue;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;

public class ZunTester {
	
	public ZunTester() {	
		System.out.println("Cominciamo");
		JCOValue jv = new SimpleValue ("paolo");
		FieldDefinition f = new FieldDefinition("attr1", jv);
		DocumentDefinition d = new DocumentDefinition (f);
		d = new DocumentDefinition ("ciccio");
		System.out.println(d);
		
		jv = new SimpleValue ("fosci");
		f = new FieldDefinition("attr1", jv);
		d.addField(f);		
		System.out.println(d);
		
		f = d.getField("ciccio");
		System.out.println(f.getName()+":"+f.getValue());

		ArrayValue v2 = new ArrayValue();
		v2.add(new SimpleValue (2));
		v2.add(new SimpleValue (3.1));
		v2.add(new SimpleValue ("true"));
		v2.add(new SimpleValue ("paolo"));
		v2.add(new SimpleValue (false));
		d.addField(new FieldDefinition("arrays", v2));
		System.out.println(d);

		f = new FieldDefinition("ASCA", new SimpleValue ("asd"));
		DocumentDefinition d2 = new DocumentDefinition (f);
		d2.addField(new FieldDefinition("sca2", new SimpleValue ("paol")));
		d2.addField(new FieldDefinition("sca2", new SimpleValue ("paol")));
		d.addField(new FieldDefinition("prova", new DocumentValue(d2)));
	
		System.out.println(d);
	}

	public static void main(String[] args) {
		new ZunTester();
	}


}
