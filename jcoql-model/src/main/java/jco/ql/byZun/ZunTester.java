package jco.ql.byZun;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.value.DocumentValue;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.util.Field;

public class ZunTester {

	public static void main(String[] args) {
		JCOValue x;
		FieldDefinition fd;
		DocumentDefinition doc;
		
		doc = new DocumentDefinition();
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		fd = new FieldDefinition("a", new SimpleValue (1));
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");
		
		fd = new FieldDefinition("b", new SimpleValue (2));
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		x = new DocumentValue(doc);
		fd = new FieldDefinition("x", x);

		doc = new DocumentDefinition();
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		fd = new FieldDefinition("b", new SimpleValue (2));
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		fd = new FieldDefinition("c", new SimpleValue (3));
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");
		
		fd = new FieldDefinition("d", new SimpleValue (4));
		doc.addField(fd);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		
		
		SimpleValue sv = new SimpleValue("eccomi");
		Field nf = new Field();
		nf.addField(".b");
		nf.addField(".c");
		nf.addField(".e");
//		insertField (doc, nf, sv);
		doc.insertField(nf, sv);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		sv = new SimpleValue("ciccio");
		nf = new Field();
		nf.addField(".b");
		nf.addField(".e");
		nf.addField(".c");
		doc.insertField(nf, sv);
//		insertField (doc, nf, sv);
		System.out.println(doc.toString());
		System.out.println("________________________\n");

		sv = new SimpleValue("crasi");
		nf = new Field();
		nf.addField(".x");
		nf.addField(".a");
		nf.addField(".sa");
		insertField (doc, nf, sv);
		System.out.println(doc.toString());
		System.out.println("________________________\n");
		
		sv = new SimpleValue("ciuccio");
		nf = new Field();
		nf.addField(".x");
		nf.addField(".b");
		nf.addField(".ba");
		doc.insertField(nf, sv);
		System.out.println(doc.toString());
		System.out.println("________________________\n");
	}

	
	private static  void insertField(DocumentDefinition doc, Field field, JCOValue value) {
		FieldDefinition fd;
		String head = field.head();
		
		if (field.size() == 1) {
			fd = new FieldDefinition(head, value);
			doc.addField(fd);
		}
		else {
			DocumentValue subDoc;
			fd = doc.getField(head);
			if (fd == null || fd.getValue() == null || fd.getValue().getType() != EValueType.DOCUMENT) {
				subDoc = new DocumentValue();
				fd = new FieldDefinition(head, subDoc);
				doc.addField(fd);
			}
			subDoc = (DocumentValue)fd.getValue();
			insertField (subDoc.getDocument(), field.cloneSuffix(), value);
		}
	}

}
