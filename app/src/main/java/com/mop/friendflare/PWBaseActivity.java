package com.mop.friendflare;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;


/**
 * Base Activity to build from
 * @author paulin
 *
 */
public abstract class PWBaseActivity extends Activity implements LocationListener {
	
	public static final String TAG = "PWBaseActivity";				
	
//	/* Creates the menu items */
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuAdapter.conditionMenu(menu);
//		MenuAdapter.setMenuBackground(this);		
//	    return true;
//	}
//				
//	/* Handles item selections */
//	public boolean onOptionsItemSelected(MenuItem item) {
//		return MenuAdapter.onOptionsItemSelected(item, this);
//	}	
	
	/**
	 * Constructs the screen and set the title properly
	 * @param 	layout	The layout to use
	 * @param	title	The title for the page
	 */
	public void setLayout(int layout, String title, boolean showRight, boolean showLeft) {
        setContentView(layout);    
   	}	
		
	/**
	 * Constructs the screen and set the title properly
	 * @param 	layout	The layout to use
	 * @param	title	The title for the page
	 */
	public void setLayout(int layout, String title) {
        setLayout(layout,title,false, false);	
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}        

	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		
                
        //Start the GPS if needed
        if(usesGPS()) {
        	startGPSListening();
        }
		super.onStart();
	}	
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");

		//Turn off the GPS antenna
		shutDownGPSListening();

		super.onStop();
	}
	
	//############ GPS Related functions #################
	/**
	 * Time in milliseconds between location updates
	 */
	public abstract boolean usesGPS();
	
	
	private static long MIN_UPDATE_TIME = 0;
	private Location location;
    private LocationManager locman;		
			
	/**
	 * Starts GPS Listener
	 */
	private void startGPSListening() {
		Log.v(TAG, "GEO Starting the GPS Listener");

		locman = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		//Register for a new location
		locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_UPDATE_TIME, 0, this);		
	}
	
	private void shutDownGPSListening() {
		if(locman != null) {
			locman.removeUpdates(this);			
		}
	}
		
	public Location getLocation() {
		
		//http://marakana.com/forums/android/examples/42.html
		Criteria criteria = new Criteria();
		String bestProvider = locman.getBestProvider(criteria, false);
		Location temp = locman.getLastKnownLocation(bestProvider);		
//Log.v(LogConstants.MATT_TAG, "Location is now [" +  temp + "]");		
		if(temp != null) {
			location = temp;
		}
		return location;
	}

	/**
	 * Override this if you want to use these methods
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.v(TAG, "GEO: onLocationChanged [" 
				+ GeoUtils.formLatLonString(GeoUtils.convertLocationToGeoPoint(location)) 
				+ "]");
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.v(TAG, "GEO: onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.v(TAG, "GEO: onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.v(TAG, "GEO: onStatusChanged");		
	}		
}
