package com.mop.friendflare;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.google.android.maps.GeoPoint;

/**
 * Methods for doing basic geography things
 * 
 * @author paulin
 *
 */
public class GeoUtils {
	public static final int LAT_LON_CONVERT = 1000000;

	public static GeoPoint convertLocationToGeoPoint(Location location) {
				
		if(location == null) {
			//TODO change this... The default Seattle
			GeoPoint pt = new GeoPoint(37422006, -122084095);
			
			return pt;
		}
		return createGeoPoint(location.getLongitude(), location.getLatitude());
	}
	
	public static GeoPoint createGeoPoint(double lon, double lat) {
		return new GeoPoint((int)(lat * LAT_LON_CONVERT), (int)(lon * LAT_LON_CONVERT));
	}

	public static String formLatLonString(GeoPoint location) {
		if(location == null) return "";
		String lat = Double.toString((double)location.getLatitudeE6()/(double)LAT_LON_CONVERT);
		String lon = Double.toString((double)location.getLongitudeE6()/(double)LAT_LON_CONVERT);
		return String.format("latitude=%s&longitude=%s", lat,lon);
	}

	/**
	 * Figures out the google zoom level based on the provided factors
	 * 
	 * @param radius
	 * @param latitudeE6
	 * @return
	 */
	public static int calcZoomLevel(double radius, int latitudeE6) {
		double latRad = Math.toRadians(((double)latitudeE6/(double)LAT_LON_CONVERT));
		return degreesMetersToLevel(radius * Math.cos(latRad));
	}

	public static GeoPoint createGeoPoint(JSONObject json) throws NumberFormatException, JSONException {
		
		float lon = Float.parseFloat(json.getString("longitude"));
		float lat = Float.parseFloat(json.getString("latitude"));
						
   		return GeoUtils.createGeoPoint(lon, lat);
	}
	
	/**
	 * At full zoom, equator = 256 pixels
	 *  "1:25000 map" tells me more than "level 14 zoom".
	 *  
	 * @param zoomLevel
	 * @param latitude
	 * @return
	 */
	public static String getScale(int zoomLevel, int latitude) {
//		Zoom level  0 - scale 1 : 409 600 000
//		Zoom level  1 - scale 1 : 204 800 000
//		Zoom level  2 - scale 1 : 102 400 000
//		Zoom level  3 - scale 1 :  51 200 000
//		Zoom level  4 - scale 1 :  25 600 000
//		Zoom level  5 - scale 1 :  12 800 000
//		Zoom level  6 - scale 1 :   6 400 000
//		Zoom level  7 - scale 1 :   3 200 000
//		Zoom level  8 - scale 1 :   1 600 000
//		Zoom level  9 - scale 1 :     800 000
//		Zoom level 10 - scale 1 :     400 000
//		Zoom level 11 - scale 1 :     200 000
//		Zoom level 12 - scale 1 :     100 000
//		Zoom level 13 - scale 1 :      50 000
//		Zoom level 14 - scale 1 :      25 000
//		Zoom level 15 - scale 1 :      12 500
//		Zoom level 16 - scale 1 :       6 250
//		Zoom level 17 - scale 1 :       3 125
		
		String result = "Scale 1:";
		//result = result + zoomLevel;
		result = result + degreesLevelToMeters(zoomLevel);
		return result;
	}
	
	public static int degreesLevelToMeters(double zoomLevel) {
		return (int)(1562.5 * Math.pow(2, 19 - zoomLevel));
	}	
	public static int degreesMetersToLevel(double meters) {
		int result = 14 - new BigDecimal(Math.log(meters/1565.5) / Math.log(2)).intValue();
		if(result < 0) result = 0;
		return result;
	}		
}
