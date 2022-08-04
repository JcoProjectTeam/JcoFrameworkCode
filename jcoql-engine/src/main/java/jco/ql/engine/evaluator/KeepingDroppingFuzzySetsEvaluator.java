package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.List;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.FieldDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.DocumentValue;
import jco.ql.parser.model.fuzzy.KeepingDroppingFuzzySets;


public class KeepingDroppingFuzzySetsEvaluator implements JCOConstants {

    public static DocumentDefinition evaluate(DocumentDefinition document, KeepingDroppingFuzzySets keepingDroppingFuzzySets) {

        DocumentDefinition outDocument = null;
        List<FieldDefinition> fieldList = new ArrayList<>();
        DocumentDefinition fullDoc;
        List<FieldDefinition> fieldFuzzy = new ArrayList<>();

        int type = keepingDroppingFuzzySets.type;
        // DROPPING
        if(type == KeepingDroppingFuzzySets.DROPPING) {
            if(document != null) {
                if(document.getValue(FUZZYSETS_FIELD_NAME) == null) {
                    outDocument = document;
                } else {
                    DocumentDefinition ddFuzzy = new DocumentDefinition(((DocumentValue) document.getValue(FUZZYSETS_FIELD_NAME)).getFields());
                    for(FieldDefinition fd : document.getFields()) {
                        if(!fd.getName().equals(FUZZYSETS_FIELD_NAME)) {
                            fieldList.add(fd);
                        } else {
                            for(FieldDefinition fdFuzzy : ddFuzzy.getFields()) {
                                boolean equals = false;
                                for(String s : keepingDroppingFuzzySets.fuzzySets) {
                                    if(fdFuzzy.getName().equals(s)) {
                                        equals = true;
                                    }
                                }
                                if(!equals) {
                                    fieldFuzzy.add(fdFuzzy);
                                }
                            }
                        }
                        fullDoc = new DocumentDefinition(fieldList);
                        if(fieldFuzzy.size() > 0) {
                            fullDoc.addField(new FieldDefinition(FUZZYSETS_FIELD_NAME, new DocumentValue(fieldFuzzy)));
                        }
                        outDocument = fullDoc;
                    }
                }
            }
        }
        // KEEPING
        else if(type == KeepingDroppingFuzzySets.KEEPING) {
            if(document != null) {
                if(document.getValue(FUZZYSETS_FIELD_NAME) == null) {
                    outDocument = document;
                } else {
                    DocumentDefinition ddFuzzy = new DocumentDefinition(((DocumentValue) document.getValue(FUZZYSETS_FIELD_NAME)).getFields());
                    for(FieldDefinition fd : document.getFields()) {
                        if(!fd.getName().equals(FUZZYSETS_FIELD_NAME)) {
                            fieldList.add(fd);
                        } else {
                            for(FieldDefinition fdFuzzy : ddFuzzy.getFields()) {
                                for(String s : keepingDroppingFuzzySets.fuzzySets) {
                                    if(fdFuzzy.getName().equals(s)) {
                                        fieldFuzzy.add(fdFuzzy);
                                    }
                                }
                            }
                        }
                        fullDoc = new DocumentDefinition(fieldList);
                        if(fieldFuzzy.size() > 0) {
                            fullDoc.addField(new FieldDefinition(FUZZYSETS_FIELD_NAME, new DocumentValue(fieldFuzzy)));
                        }
                        outDocument = fullDoc;
                    }
                }
            }
        }
        // DROPPING ALL
        else if(type == KeepingDroppingFuzzySets.DROPPING_ALL ||
        		type == KeepingDroppingFuzzySets.DEFUZZIFY) {
            if(document != null) {
            	// PF. 03.11.2021 - The OutDoc will hold all fields except FUZZY_FIELD_NAME
                for(FieldDefinition fd : document.getFields()) {
                    if(!fd.getName().equals(FUZZYSETS_FIELD_NAME)) {
                        fieldList.add(fd);
                    }
                }
                outDocument = new DocumentDefinition(fieldList);
            }
        }
        // KEEPING ALL
        else if(type == KeepingDroppingFuzzySets.KEEPING_ALL) {
            outDocument = document;
        }

        return outDocument;
    }

}
