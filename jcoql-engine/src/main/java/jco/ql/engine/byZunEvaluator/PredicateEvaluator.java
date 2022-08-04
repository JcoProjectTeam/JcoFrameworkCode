package jco.ql.engine.byZunEvaluator;

import jco.ql.engine.Pipeline;
import jco.ql.model.engine.JMH;
import jco.ql.model.value.JCOValue;
import jco.ql.model.value.SimpleValue;
import jco.ql.parser.model.predicate.ComparisonPredicate;
import jco.ql.parser.model.predicate.Expression;
import jco.ql.parser.model.predicate.InRangePredicate;
import jco.ql.parser.model.predicate.NullPredicate;
import jco.ql.parser.model.predicate.Predicate;
import jco.ql.parser.model.predicate.UsingPredicate;
import jco.ql.parser.model.predicate.WUKPredicate;
import jco.ql.parser.model.predicate.WithPredicate;
import jco.ql.parser.model.predicate.WithoutPredicate;


/* All evaluations from now on is on pipeline current doc which is NOT NULL by construction */
public class PredicateEvaluator {

	public static JCOValue evaluate(Predicate predicate, Pipeline pipeline) {
		JCOValue value = new SimpleValue(false);

		if (predicate.getType() == Predicate.EXPRESSION_PREDICATE)
			value = ExpressionPredicateEvaluator.calculate((Expression)predicate, pipeline);

		else if (predicate.getType() == Predicate.COMPARISON_PREDICATE)
			value = ComparisonPredicateEvaluator.evaluate((ComparisonPredicate)predicate, pipeline);
		
		else if (predicate.getType() == Predicate.IN_RANGE_PREDICATE)
			value = ComparisonPredicateEvaluator.evaluateInRange((InRangePredicate)predicate, pipeline);
		
		else if (predicate.getType() == Predicate.NULL_PREDICATE)
			value = NullPredicateEvaluator.evaluate((NullPredicate)predicate, pipeline);
		
		else if (predicate.getType() == Predicate.WITH_PREDICATE)
			value = WithPredicateEvaluator.evaluate((WithPredicate)predicate, pipeline);
		
		else if (predicate.getType() == Predicate.WITHOUT_PREDICATE)
			value = WithoutPredicateEvaluator.evaluate((WithoutPredicate)predicate, pipeline);
		
		else if (predicate.getType() == Predicate.WUK_PREDICATE)
			value = WUKPredicateEvaluator.evaluate((WUKPredicate)predicate, pipeline);
		
		else
			JMH.add("Predicate non allowed:\t" + predicate.getType() + " - " + predicate.toString());

		return value;
	}

	
	public static SimpleValue fuzzyEvaluate(Predicate predicate, Pipeline pipeline) {
		SimpleValue value = new SimpleValue();		// null type
		
		if (predicate.getType() == Predicate.USING_PREDICATE)
			value = UsingPredicateEvaluator.fuzzyEvaluate((UsingPredicate)predicate, pipeline);

		else
			JMH.add("Predicate non allowed:\t" + predicate.getType() + " - " + predicate.toString());

		return value;
	}

}
