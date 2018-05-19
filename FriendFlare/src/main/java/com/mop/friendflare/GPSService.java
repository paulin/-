package com.mop.friendflare;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.pugetworks.database.DbAdapter;
import com.pugetworks.database.LocationRequest;
import com.pugetworks.database.LocationRequestState;
import com.pugetworks.utils.BitlyAndroid;
import com.pugetworks.utils.GeoUtils;
import com.pugetworks.utils.GoogleAnalytics;
import com.pugetworks.utils.LogConstants;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GPSService extends Service {
	
	private static String LOGIN = "pugetworkswhereuat";
	private static String APIKEY = "R_2372242609f29167714fb66a61f828e1";
	private static String MAP_STRING_TEST = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=12";
	private static final String MAP_STRING = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=%s,%s&ie=UTF8&z=%d";
	private static final int ZOOM_LEVEL = 12;
	private static final String FRIEND_FLARE_MSG_TEMPLATE = "Here\'s my location: %s (sent via http://www.whereu-at.com )";

	public static final long WAIT_TIME = 10000;
	
	private BitlyAndroid bitly;
	
	public static String TAG = "GPSService";
	
	
	private GPSListener mGpsListener;
	private LocationManager location_manager;
	private CountDownTimer countdown;
	private Location bestLocationYet;
		
	@Override
	public void onCreate(){

		
		mGpsListener = new GPSListener();
		bitly = new BitlyAndroid(LOGIN, APIKEY);
		location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.v(LogConstants.MATT_TAG, "Activating GPS Service");
				
		super.onCreate();
	}

	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		resendMessages();
		return super.onStartCommand(intent, flags, startId);
	}



	@Override
	public void onDestroy(){
		Log.v(LogConstants.MATT_TAG, "Destroying GPS Service");
		
		LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		location_manager.removeUpdates(mGpsListener);

		super.onDestroy();
	}
	

	private Location getGPS() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}
		
		return l;
	}
	
	private void setLocation(Location location) {
		Log.v(LogConstants.MATT_TAG, "Location is found");
		location_manager.removeUpdates(mGpsListener);

		Log.v(LogConstants.MATT_TAG, "GPS Service Sending Formed Message");
		
	    ShrinkAndSendURL task = new ShrinkAndSendURL();
	    task.execute(new String[] { formatMapLink(location) });
	}
	
	/**
	 * Grabs the location and resents
	 */
	private void resendMessages() {
		Log.v(LogConstants.MATT_TAG, "GPS Service Resending Locations");

		//Testing this to see if its faster
		bestLocationYet = getGPS();
		
		//This works but is slow
		location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, mGpsListener);
		location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0, mGpsListener);

		//I moved this before the countdown to see if flurry would be able to send the message while the countdown was going

		EasyTracker.getInstance().setContext(this);
		EasyTracker.getTracker().sendEvent(TAG, GoogleAnalytics.RESPONSE_MESSAGE_SENT, "Response Sent", 1l);



		countdown =  new CountDownTimer(WAIT_TIME, WAIT_TIME) {

		     public void onTick(long millisUntilFinished) {
		     }

		     public void onFinish() {
		    	 setLocation(bestLocationYet);
		     }
		  }.start();

		
		
		//Get the last location
//		Location loc = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if(loc != null) {
//			Log.v(LogConstants.MATT_TAG, "Location found, close enough");
//			this.setLocation(loc);
//		} else {
//			Log.v(LogConstants.MATT_TAG, "Bad Location, requesting a new one");
//			location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGpsListener, null);			
//		}		
		
	}
	
	private String formatMapLink(Location loc) {
		String msg = null;
		if (loc != null) {
			GeoUtils.formLatLonString(GeoUtils.convertLocationToGeoPoint(loc));
			msg = String.format(MAP_STRING, loc.getLatitude(),
					loc.getLongitude(), ZOOM_LEVEL);
			// "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=12";
			Log.v(LogConstants.MATT_TAG, "URL [" + msg + "]");

		} else {
	    	Log.e(LogConstants.MATT_TAG, "Can't send message, location not known");
		}
		return msg;
	}
	
	private String shrinkAndFormatMsg(String url) {
		String shortUrl = "";
		try {
			shortUrl =  String.format(FRIEND_FLARE_MSG_TEMPLATE, bitly.getShortUrl(url) );
		} catch (JSONException e) {
			Log.e(TAG, "JSON:Could not linkfy " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException : Could not linkfy " + e.getMessage());
			e.printStackTrace();
		}		
		return shortUrl;
	}
	
	private void sendThisMessage(String msg) {
		DbAdapter mDbHelper = new DbAdapter(this);
		mDbHelper.open();
		ArrayList<LocationRequest> requests = mDbHelper.fetchLocationRequest(LocationRequestState.NEW);
		if(requests.size() == 0) {
	    	Log.e(LogConstants.MATT_TAG, "No new messges to send");
		} else {
			if(msg!= null) {
				Log.v(LogConstants.MATT_TAG, "GPS Service Done!");
				for (LocationRequest locationRequest : requests) {
					String fromNumber = locationRequest.getReqFrom();
				
		    		//Send the text
		    		Log.i(LogConstants.MATT_TAG, "Message = [" + msg + "] for number [" + fromNumber + "]" );
	
		    		SmsManager sms = SmsManager.getDefault();
		    		Log.v(LogConstants.MATT_TAG, "GPS Sending now");
					sms.sendTextMessage(fromNumber, null, msg, null, null);
					insertSMS(fromNumber, msg);
					Log.v(TAG, "SMS Response sent.");
					Toast.makeText(this, "Msg Sent!", Toast.LENGTH_LONG).show();
					
					locationRequest.setLocationState(LocationRequestState.SENT);
					locationRequest.setReqNote(msg);
					mDbHelper.saveLocationRequest(locationRequest);
					
				}				
			} else {
				Toast.makeText(this, "Msg Formed Improperly", Toast.LENGTH_LONG).show();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			//Now kill yourself
			//android.os.Process.killProcess(android.os.Process.myPid());
		}
		mDbHelper.close();
		stopSelf();
	}
	
	public void insertSMS(String address, String body){
		ContentResolver resolver = null;
		resolver = getContentResolver();//context is your instance of Activity 
		ContentValues values = new ContentValues();
		values.put("address", address);
		values.put("body", body);
		resolver.insert(Uri.parse("content://sms/sent"), values);
	}
	
	//Network calls
    /**
     * The IAdderService is defined through IDL
     */
    private final IRemoteGPSService.Stub mBinder = new IRemoteGPSService.Stub() {

		@Override
		public int resentTexts() throws android.os.RemoteException {
			resendMessages();
			return 0;
		}

    };  

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(LogConstants.MATT_TAG, "GPS Service onBind");
		return mBinder;
	}	
	

	
    /**
     * Used to pull bitly calls off the main thread
     * @author paulin42
     *
     */
	private class ShrinkAndSendURL extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				if(url != null) { //BUG https://office.pugetworks.com/redmine/issues/5456
					response = shrinkAndFormatMsg(url);					
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String msg) {
			sendThisMessage(msg);
		}
	}
	
	
	/**
	 * Used to listen for location changes
	 * @author paulin42
	 *
	 */
    class GPSListener implements LocationListener {

    	public void onLocationChanged(Location location){
    		if(isBetterLocation(location, bestLocationYet)) {
    			bestLocationYet = location;
    		}
    	}   
		public void onProviderDisabled(String provider){}
		public void onProviderEnabled(String provider){}
		public void onStatusChanged(String provider, int status, Bundle extras){}
    }
    
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
