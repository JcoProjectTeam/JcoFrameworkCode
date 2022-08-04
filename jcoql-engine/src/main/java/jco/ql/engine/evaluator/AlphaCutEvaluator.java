package jco.ql.engine.evaluator;

import java.util.List;

import jco.ql.model.DocumentDefinition;
import jco.ql.model.engine.JCOConstants;
import jco.ql.model.value.EValueType;
import jco.ql.model.value.JCOValue;
import jco.ql.parser.model.fuzzy.AlphaCut;

public class AlphaCutEvaluator implements JCOConstants {
    public static DocumentDefinition evaluate(DocumentDefinition document, List<AlphaCut> accList) {
        if(accList.size() > 0) {
            for (AlphaCut acc : accList) {
                if (!evaluateAlphaCut(document, acc))
                    return null;
            }
        }
        return document;
    }

    
    private static boolean evaluateAlphaCut(DocumentDefinition doc, AlphaCut alphacut) {
    	JCOValue fuzzySet = doc.getValue(FUZZYSETS_FIELD_NAME + FIELD_SEPARATOR + alphacut.on);
    	if (fuzzySet != null && fuzzySet.getType() != EValueType.NULL) {
    		Double v = Double.parseDouble(fuzzySet.getStringValue());
            return (v >= alphacut.alphacutValue);
        }
        return false;
    }
}
