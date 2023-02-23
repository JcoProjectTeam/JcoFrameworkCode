
package jco.ql.tester;

public class MyGeoClass2 
{
		public static double GeoKmDistance (double lat1, double lon1, double lat2, double lon2) {
			int R = 6371;			double dLat = (lat2-lat1) * (Math.PI/180);
			double dLon = (lon2-lon1) * (Math.PI/180);
			double a = Math.pow(Math.sin(dLat/2), 2) +
						Math.cos(lat1 * (Math.PI/180)) *
						Math.cos(lat2 * (Math.PI/180)) *
						Math.pow(Math.sin(dLon/2), 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			double d = R * c;			return d;
		}
	}

