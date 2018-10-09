package com.mop.atat


import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class GPSService : IntentService("GPSService") {

    companion object {
        private val MAP_STRING = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=%s,%s&ie=UTF8&z=%d"
        private val ZOOM_LEVEL = 12
    }
    var TAG = "GPSService"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var dbManager: LocationRequestDbManager

    override fun onCreate() {

        Log.v(TAG, "Activating GPS Service")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbManager = LocationRequestDbManager(this)
        super.onCreate()
    }

    override fun onHandleIntent(arg0: Intent?) {
        Log.i(TAG, "Intent GPS Service started")

        //Pull up all the new locations and loop through them
        var listRequests = dbManager.fetchLocationRequest(LocationRequestState.NEW)
        for (i in listRequests.indices) {
            //Log.v(LogConstants.MATT_TAG, "$listRequests[i]")
            getLocation(listRequests[i])
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(request: LocationRequest) {
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        //Log.v(LogConstants.MATT_TAG, "Got location $location" )
                        sendTextMessages(request.phoneNumber,formatMapLink(location))
                        request.locationState = LocationRequestState.SENT
                        request.latitude = location.latitude.toString()
                        request.longitude = location.longitude.toString()
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
            Log.v(TAG, "URL [$msg]")

        } else {
            Log.e(TAG, "Can't send message, location not known")
        }
        return msg
    }

    fun sendTextMessages(contactAddress: String, msg: String) {

        val sms : SmsManager = SmsManager.getDefault ()
        sms.sendTextMessage(contactAddress, null, msg, null, null)
        //Log.e(LogConstants.MATT_TAG, "Sending [$msg] to [$contactAddress]")
        Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()
        //Log.e(LogConstants.MATT_TAG, "Sent message to [$contactAddress]")
    }
}
