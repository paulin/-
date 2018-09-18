package com.mop.friendflare


import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class GPSService : IntentService("MyIntentService") {

    companion object {

        private val LOGIN = "pugetworkswhereuat"
        private val APIKEY = "R_2372242609f29167714fb66a61f828e1"
        private val MAP_STRING_TEST = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=12"
        private val MAP_STRING = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=%s,%s&ie=UTF8&z=%d"
        private val ZOOM_LEVEL = 12
        private val FRIEND_FLARE_MSG_TEMPLATE = "Here\'s my location: %s (sent via http://www.whereu-at.com )"

        val WAIT_TIME: Long = 10000

        var TAG = "GPSService"

        private val TWO_MINUTES = 1000 * 60 * 2
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var dbManager: LocationRequestDbManager

    override fun onCreate() {

        Log.v(LogConstants.MATT_TAG, "Activating GPS Service")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbManager = LocationRequestDbManager(this)
        super.onCreate()
    }

    override fun onHandleIntent(arg0: Intent?) {
        Log.i(LogConstants.MATT_TAG, "Intent Service started")

        //Pull up all the new locations and loop through them
        var listRequests = dbManager.fetchLocationRequest(LocationRequestState.NEW)
        for (i in listRequests.indices) {
            Log.v(LogConstants.MATT_TAG, "$listRequests[i]")
            getLocation(listRequests[i])
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(request: LocationRequest) {
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.v(LogConstants.MATT_TAG, "Got location $location" )
                        sendTextMessages(request.phoneNumber,formatMapLink(location))
                        request.locationState = LocationRequestState.SENT
                        var selectionArs = arrayOf(request.id.toString())
                        val mID = dbManager.update(request.toContext(), "Id=?", selectionArs)
                    } else {
                        Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
                    }
                }

    }

    private fun formatMapLink(loc: Location?): String {
        var msg: String = "no message"
        if (loc != null) {
            msg = String.format(MAP_STRING, loc.latitude,
                    loc.longitude, ZOOM_LEVEL)
            // "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=12";
            Log.v(LogConstants.MATT_TAG, "URL [$msg]")

        } else {
            Log.e(LogConstants.MATT_TAG, "Can't send message, location not known")
        }
        return msg
    }

    public fun sendTextMessages(contactAddress: String, msg: String) {

        val sms : SmsManager = SmsManager.getDefault ()
        sms.sendTextMessage(contactAddress, null, msg, null, null)
        Log.e(LogConstants.MATT_TAG, "Sending [$msg] to [$contactAddress]")
        Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()
        Log.e(LogConstants.MATT_TAG, "Sent message to [$contactAddress]")
    }

    //Network calls
//    /**
//     * The IAdderService is defined through IDL
//     */
//    private val mBinder = object : IRemoteGPSService.Stub() {
//
//        @Throws(android.os.RemoteException::class)
//        override fun resentTexts(): Int {
//            resendMessages()
//            return 0
//        }
//
//    }

//    override fun onBind(intent: Intent): IBinder? {
//        Log.v(LogConstants.MATT_TAG, "GPS Service onBind")
//        return mBinder
//    }

//    /**
//     * Grabs the location and resents
//     */
//    private fun resendMessages() {
//        Log.v(LogConstants.MATT_TAG, "GPS Service Resending Locations")
//
//        getLocation()
//    }


//    private var mGpsListener: GPSListener? = null
//    private var location_manager: LocationManager? = null
//    private var countdown: CountDownTimer? = null
//    private var bestLocationYet: Location? = null
//
//
//    private/*
//		 * Loop over the array backwards, and if you get an accurate location,
//		 * then break out the loop
//		 */ val gps: Location?
//        get() {
//            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            val providers = lm.getProviders(true)
//            var l: Location? = null
//
//            for (i in providers.indices.reversed()) {
//                l = lm.getLastKnownLocation(providers[i])
//                if (l != null)
//                    break
//            }
//
//            return l
//        }
//


//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        resendMessages()
//        return super.onStartCommand(intent, flags, startId)
//    }
//
//
//    override fun onDestroy() {
//        Log.v(LogConstants.MATT_TAG, "Destroying GPS Service")
//
////        val location_manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
////
////        location_manager.removeUpdates(mGpsListener)
//
//        super.onDestroy()
//    }

//    private fun setLocation(location: Location?) {
//        Log.v(LogConstants.MATT_TAG, "Location is found")
//        location_manager!!.removeUpdates(mGpsListener)
//
//        Log.v(LogConstants.MATT_TAG, "GPS Service Sending Formed Message")
//
//        val task = ShrinkAndSendURL()
//        task.execute(*arrayOf<String>(formatMapLink(location)))
//    }
//
//    /**
//     * Grabs the location and resents
//     */
//    private fun resendMessages() {
//        Log.v(LogConstants.MATT_TAG, "GPS Service Resending Locations")
//
//        //Testing this to see if its faster
//        bestLocationYet = gps
//
//        //This works but is slow
//        location_manager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0f, mGpsListener)
//        location_manager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0f, mGpsListener)
//
//        //TODO add tracking here
//
//
//        countdown = object : CountDownTimer(WAIT_TIME, WAIT_TIME) {
//
//            override fun onTick(millisUntilFinished: Long) {}
//
//            override fun onFinish() {
//                setLocation(bestLocationYet)
//            }
//        }.start()
//
//
//    }


//
//    private fun shrinkAndFormatMsg(url: String): String {
//        var shortUrl = ""
//        try {
//            shortUrl = String.format(FRIEND_FLARE_MSG_TEMPLATE, bitly!!.getShortUrl(url))
//        } catch (e: JSONException) {
//            Log.e(TAG, "JSON:Could not linkfy " + e.message)
//            e.printStackTrace()
//        } catch (e: IOException) {
//            Log.e(TAG, "IOException : Could not linkfy " + e.message)
//            e.printStackTrace()
//        }
//
//        return shortUrl
//    }
//
//    private fun sendThisMessage(msg: String?) {
//        val dbManager = LocationRequestDbManager(this)
//        val requests = dbManager.fetchLocationRequest(LocationRequestState.NEW)
//        if (requests.size == 0) {
//            Log.e(LogConstants.MATT_TAG, "No new messges to send")
//        } else {
//            if (msg != null) {
//                Log.v(LogConstants.MATT_TAG, "GPS Service Done!")
//                for (locationRequest in requests) {
//                    val fromNumber = locationRequest.getReqFrom()
//
//                    //Send the text
//                    Log.i(LogConstants.MATT_TAG, "Message = [$msg] for number [$fromNumber]")
//
//                    val sms = SmsManager.getDefault()
//                    Log.v(LogConstants.MATT_TAG, "GPS Sending now")
//                    sms.sendTextMessage(fromNumber, null, msg, null, null)
//                    insertSMS(fromNumber, msg)
//                    Log.v(TAG, "SMS Response sent.")
//                    Toast.makeText(this, "Msg Sent!", Toast.LENGTH_LONG).show()
//
//                    locationRequest.locationState = LocationRequestState.SENT
//                    locationRequest.reqNote = msg
//                    mDbHelper.saveLocationRequest(locationRequest)
//
//                }
//            } else {
//                Toast.makeText(this, "Msg Formed Improperly", Toast.LENGTH_LONG).show()
//            }
//            try {
//                Thread.sleep(1000)
//            } catch (e: InterruptedException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            }
//
//
//            //Now kill yourself
//            //android.os.Process.killProcess(android.os.Process.myPid());
//        }
//        mDbHelper.close()
//        stopSelf()
//    }
//
//    fun insertSMS(address: String, body: String) {
//        var resolver: ContentResolver? = null
//        resolver = contentResolver//context is your instance of Activity
//        val values = ContentValues()
//        values.put("address", address)
//        values.put("body", body)
//        resolver!!.insert(Uri.parse("content://sms/sent"), values)
//    }
//

//
//
//    /**
//     * Used to pull bitly calls off the main thread
//     * @author paulin42
//     */
//    private inner class ShrinkAndSendURL : AsyncTask<String, Void, String>() {
//        override fun doInBackground(vararg urls: String): String {
//            var response = ""
//            for (url in urls) {
//                if (url != null) { //BUG https://office.pugetworks.com/redmine/issues/5456
//                    response = shrinkAndFormatMsg(url)
//                }
//            }
//            return response
//        }
//
//        override fun onPostExecute(msg: String) {
//            sendThisMessage(msg)
//        }
//    }
//
//
//    /**
//     * Used to listen for location changes
//     * @author paulin42
//     */
//    internal inner class GPSListener : LocationListener {
//
//        override fun onLocationChanged(location: Location) {
//            if (isBetterLocation(location, bestLocationYet)) {
//                bestLocationYet = location
//            }
//        }
//
//        override fun onProviderDisabled(provider: String) {}
//        override fun onProviderEnabled(provider: String) {}
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//    }
//
//    /** Determines whether one Location reading is better than the current Location fix
//     * @param location  The new Location that you want to evaluate
//     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
//     */
//    protected fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
//        if (currentBestLocation == null) {
//            // A new location is always better than no location
//            return true
//        }
//
//        // Check whether the new location fix is newer or older
//        val timeDelta = location.time - currentBestLocation.time
//        val isSignificantlyNewer = timeDelta > TWO_MINUTES
//        val isSignificantlyOlder = timeDelta < -TWO_MINUTES
//        val isNewer = timeDelta > 0
//
//        // If it's been more than two minutes since the current location, use the new location
//        // because the user has likely moved
//        if (isSignificantlyNewer) {
//            return true
//            // If the new location is more than two minutes older, it must be worse
//        } else if (isSignificantlyOlder) {
//            return false
//        }
//
//        // Check whether the new location fix is more or less accurate
//        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
//        val isLessAccurate = accuracyDelta > 0
//        val isMoreAccurate = accuracyDelta < 0
//        val isSignificantlyLessAccurate = accuracyDelta > 200
//
//        // Check if the old and new location are from the same provider
//        val isFromSameProvider = isSameProvider(location.provider,
//                currentBestLocation.provider)
//
//        // Determine location quality using a combination of timeliness and accuracy
//        if (isMoreAccurate) {
//            return true
//        } else if (isNewer && !isLessAccurate) {
//            return true
//        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
//            return true
//        }
//        return false
//    }
//
//    /** Checks whether two providers are the same  */
//    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
//        return if (provider1 == null) {
//            provider2 == null
//        } else provider1 == provider2
//    }
}
