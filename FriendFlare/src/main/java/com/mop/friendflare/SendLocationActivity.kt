package com.mop.friendflare

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.mop.friendflare.R.id.*
import kotlinx.android.synthetic.main.activity_location_request.*
import kotlinx.android.synthetic.main.activity_send_location.*

class SendLocationActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    lateinit var textView : TextView

    var googleApiClient : GoogleApiClient? = null

    lateinit var locationRequest : LocationRequest

    lateinit var locationSettingRequest : LocationSettingsRequest

    var currentLocation : Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_location)

        try {
            var bundle: Bundle = intent.extras
            id = bundle.getInt("MainActId", 0)
            if (id != 0) {
                edtContent.setText(bundle.getString("MainActContent"))
            }
        } catch (ex: Exception) {
        }

        btGetGeo.setOnClickListener {

            var values = ContentValues()


            Toast.makeText(this, "Got GEO!", Toast.LENGTH_LONG).show()
            //values.put("Content", edtContent.text.toString())
        }


        btShrink.setOnClickListener {
            Toast.makeText(this, "Shrinking URL", Toast.LENGTH_LONG).show()

            var values = ContentValues()

            //values.put("Content", edtContent.text.toString())

        }

        btSend.setOnClickListener {
            Toast.makeText(this, "Sending Geo", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI() {
        // Location Update UI
        if (currentLocation != null) {
            textView.text = String.format("Latitude:%f Longitude:%f", currentLocation?.latitude, currentLocation?.longitude)
        }
    }

    private fun initGoogleAPIClient() {
        this.googleApiClient = GoogleApiClient.Builder(applicationContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationSettings() {
        val builder : LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        if (this.locationRequest != null) {
            builder.addLocationRequest(locationRequest)
            this.locationSettingRequest = builder.build()
        }
    }

    private fun isLocationEnabled(context : Context) : Boolean {
        var locationMode: Int = 0
        var locationProviders: String?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
                return locationMode != Settings.Secure.LOCATION_MODE_OFF
            } catch(e: Settings.SettingNotFoundException) {
                e.printStackTrace()

            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            return locationProviders != null && locationProviders.isNotEmpty()
        }
    }

    @Throws(SecurityException::class)
    private fun startLocationUpdates() {
        /*
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                return
            }
        }*/

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this).setResultCallback {
            status: Status -> if (status.isSuccess) {
            print("Success")
        }
        else {
            print("Failed")
        }
        }
    }

    /*
     * GoogleApiClient.ConnectionCallbacks
     */

    override fun onConnected(p0: Bundle?) {
        if (currentLocation == null) {
            try {           // checkselfpermission or SecurityException
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                // TODO Update UI
                updateUI()
            }
            catch(e : SecurityException) {

            }
        }
    }

    /*
     *  GoogleApiClient.OnConnectionFailedListener
     */

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    /*
     * LocationListener
     */

    override fun onLocationChanged(p0: Location?) {
    }
}