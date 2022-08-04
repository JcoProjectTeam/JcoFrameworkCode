package jco.ql.engine.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import jco.ql.model.value.GeometryValue;

public class TrajectoryMatchingEvaluator {
	
	// PF. 06.02.2022 - valutare se renderla statica come gli altri evaluator
	
	private double epsKM;

	// NUOVO EC
	// mappa con chiavi multiple che conterrà le coppie degli indici di match, è
	// stato aggiunto anche il relativo metodo get
	private Map<Integer, List<Integer>> matchedPair = new TreeMap<Integer, List<Integer>>();

	
/* ZUN CHECK*	
	public TrajectoryMatchingEvaluator(PartitionMatchingCondition matchingCondition) {
		double eps = matchingCondition.getThreshold();
		EUnitOfMeasure unit = matchingCondition.getUnit();

		switch (unit) {
		case METERS:
			eps = eps / 1000;
			break;
		case MILES:
			eps = eps * 1.60934;
			break;
		case KILOMETERS:

			break;
		default:
			break;

		}

		epsKM = eps;
	}

*/
	public double getEpsKM() {
		return epsKM;
	}

	
	/*
	 * public boolean checkBBdistance(GeometricValue tv, GeometricValue iv) { // create
	 * bounding box Geometry bbt = tv.getGeometry().getEnvelope(); Geometry bbi =
	 * iv.getGeometry().getEnvelope();
	 *
	 * Envelope e = bbi.getEnvelopeInternal();
	 *
	 * // find nearest points Coordinate[] nearestPoints =
	 * DistanceOp.nearestPoints(bbi, bbt); GeometryFactory gf = new
	 * GeometryFactory(); List<Point> points = new ArrayList<Point>();
	 *
	 * for (Coordinate c : nearestPoints) { points.add(gf.createPoint(c)); }
	 *
	 * // computer distance between nearest points double distance =
	 * getDistanceInKm(points.get(0), points.get(1));
	 *
	 * if (distance < epsKM) return true; else return false;
	 *
	 * }
	 */

	
	/*
	 * il metodo evaluate viene invocato due volte nella classe
	 * TrajMatchingExecutor2, una volta per NTIpruning e l'altra per il computeEDR
	 * aggiungo il flag booleano per sapere quale dei due si tratta. In questo caso
	 * verranno calcolati gli indici di match della clausola ADDING .targetPos TO
	 * INPUT
	 *
	 */
	public double evaluate(GeometryValue gt, GeometryValue gi, boolean computeEdr) {

		Geometry geomTarget = gt.getGeometry();
		Geometry geomInput = gi.getGeometry();
		GeometryFactory gf = new GeometryFactory();

		List<Point> targetTrace = new ArrayList<Point>();
		Coordinate[] targetCoord = geomTarget.getCoordinates();

		for (Coordinate coord : targetCoord) {
			targetTrace.add(gf.createPoint(coord));
		}

		List<Point> inputTrace = new ArrayList<Point>();
		Coordinate[] inputCoord = geomInput.getCoordinates();

		for (Coordinate coord : inputCoord) {
			inputTrace.add(gf.createPoint(coord));
		}

//		double edrDistance = edr(targetTrace, inputTrace, epsKM);

		Map<Pair, Double> memo = new HashMap<Pair, Double>();
		double edrDistance = edrMemoization(targetTrace, 0, inputTrace, 0, epsKM, computeEdr, memo);

		// double edrDistance = edr2(targetTrace, 0, inputTrace, 0, epsKM, computeEdr);

		return edrDistance;
		// if (edrDistance != 0) return new SimpleValue(1/edrDistance);
		// else return new SimpleValue(1); // le tracce sono uguali

	}

	// EDR algorithm

	public double getDistanceInKm(Point p1, Point p2) {
		int R = 6371;
		double lon1 = p1.getX();
		double lat1 = p1.getY();
		double lon2 = p2.getX();
		double lat2 = p2.getY();

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;
		return d;
	}

	
	public boolean match(Point p1, Point p2, double eps) {
		double distance = getDistanceInKm(p1, p2);

		if (distance <= eps) 
			return true;
		return false;

	}

	
	// latitude y
	// longitude x
	public static List<Point> Rest(List<Point> list) {
		return list.subList(1, list.size());
	}

	
	public double edr(List<Point> list, List<Point> list2, double eps) {
		int len1 = list.size();
		int len2 = list2.size();

		if (len1 == 0)
			return len2;
		if (len2 == 0)
			return len1;
		else {

			int subcost = 1;
			Point head1 = list.get(0);
			Point head2 = list2.get(0);
			if (match(head1, head2, eps)) {

				subcost = 0;
			}

			return Math.min(edr(Rest(list), Rest(list2), eps) + subcost,
					Math.min(edr(Rest(list), list2, eps) + 1, edr(list, Rest(list2), eps) + 1));

		}

	}

	public double edr2(List<Point> list, int init1, List<Point> list2, int init2, double eps, boolean computeEdr) {

		int len1 = list.size() - init1;
		int len2 = list2.size() - init2;

		if (len1 == 0)
			return len2;
		if (len2 == 0)
			return len1;

		else {

			int subcost = 1;
			Point head1 = list.get(init1);
			Point head2 = list2.get(init2);

			if (match(head1, head2, eps)) {
				// EC NUOVO
				if (computeEdr) {
					if (matchedPair.containsKey(init1)) {
						if (!matchedPair.get(init1).contains(init2)) {
							matchedPair.get(init1).add(init2);
						}
					} else {
						List<Integer> listTemp = new ArrayList<Integer>();
						listTemp.add(init2);
						matchedPair.put(init1, listTemp);
					}
				}
				subcost = 0;
			}

			return Math.min(edr2(list, init1 + 1, list2, init2 + 1, eps, computeEdr) + subcost,
					Math.min(edr2(list, init1 + 1, list2, init2, eps, computeEdr) + 1,
							edr2(list, init1, list2, init2 + 1, eps, computeEdr) + 1));

		}

	}

	public double edrMemoization(List<Point> list, int init1, List<Point> list2, int init2, double eps,
			boolean computeEdr, Map<Pair, Double> memo) {
		int len1 = list.size() - init1;
		int len2 = list2.size() - init2;

		if (len1 == 0) {
			return len2;
		}

		if (len2 == 0) {
			return len1;
		}

		Point head1 = list.get(init1);
		Point head2 = list2.get(init2);
		Pair map = new Pair(head1, head2);

		if (memo.containsKey(map)) {
			return memo.get(map);
		}

		else {

			double opt[] = new double[3];

			int subcost = 1;

			if (match(head1, head2, eps)) {
				// EC NUOVO nuova clausola Adding
				if (computeEdr) {
					if (matchedPair.containsKey(init1)) {
						// if(!matchedPair.get(init1).contains(init2))
						// {
						matchedPair.get(init1).add(init2);
						// }
					} else {
						List<Integer> listTemp2 = new ArrayList<Integer>();
						listTemp2.add(init2);
						matchedPair.put(init1, listTemp2);
					}
				}
				subcost = 0;
			}

			opt[0] = edrMemoization(list, init1 + 1, list2, init2 + 1, eps, computeEdr, memo) + subcost;
			opt[1] = edrMemoization(list, init1 + 1, list2, init2, eps, computeEdr, memo) + 1;
			opt[2] = edrMemoization(list, init1, list2, init2 + 1, eps, computeEdr, memo) + 1;

			double min = opt[0];
			for (int i = 1; i < 3; i++) {
				if (opt[i] < min)
					min = opt[i];
			}

			if (memo.containsKey(map))
				memo.replace(map, min);
			else
				memo.put(map, min);

			return min;

		}

	}

	public Map<Integer, List<Integer>> getMatchedPoint() {
		return this.matchedPair;
	}

}

class Pair {
	private Point l;
	private Point r;

	public Pair(Point l, Point r) {
		this.l = l;
		this.r = r;
	}

	public Point getL() {
		return l;
	}

	public Point getR() {
		return r;
	}

	public void setL(Point l) {
		this.l = l;
	}

	public void setR(Point r) {
		this.r = r;
	}

	public boolean equals(Object obj) {
		Pair p = (Pair) obj;
		if ((p.l.getX() == this.l.getX()) && (p.l.getY() == this.l.getY()) && (p.r.getX() == this.r.getX())
				&& (p.r.getY() == this.r.getY()))
			// if((p.l.equals(this.l)) && (p.r.equals(this.r)))
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = result + prime * this.r.hashCode();
		result = result + prime * this.l.hashCode();

		return result;

	}

	public String toString() {
		// return " < " + this.l.toString() + "," + this.r.toString() + " > ";
		return " < " + this.l.getX() + "," + this.r.getX() + " > ";
	}

}
