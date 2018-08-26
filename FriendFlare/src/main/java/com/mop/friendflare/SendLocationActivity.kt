package com.mop.friendflare

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_location_request.*
import kotlinx.android.synthetic.main.activity_send_location.*

class SendLocationActivity : AppCompatActivity(), View.OnClickListener, com.google.android.gms.location.LocationListener {

    private val ZOOM_LEVEL = 12
    private val MAP_STRING_TEST = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=" + ZOOM_LEVEL


    private val FRIEND_FLARE_MSG_TEMPLATE = "Here\'s my location: %s (sent via http://www.whereu-at.com )"

    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient;

    var id = 0

    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }

    override fun onClick(v: View?) {
        if (!checkGPSEnabled()) {
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        tvLatitude.text = location!!.latitude.toString()
                        tvLongitude.text = location!!.longitude.toString()
                    } else {
                        Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
                    }
                }

    }



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

        btGetGeo.setOnClickListener(this)

        btFormURL.setOnClickListener {
            Toast.makeText(this, "Form URL", Toast.LENGTH_LONG).show()


//            tvLongURL.setText("http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=${tvLatitude.text},${tvLongitude.text}&ie=UTF8&z=$ZOOM_LEVEL")

            var bodyData = "<html> <a href=\\\"http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=${tvLatitude.text},${tvLongitude.text}&ie=UTF8&z=$ZOOM_LEVEL\">Click HERE</a> </html>"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvLongURL.setText(Html.fromHtml(bodyData,Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvLongURL.setText(Html.fromHtml(bodyData));
            }
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvLatitude.text = "Mattitude"
        tvLongitude.text = "Mongitude"

        buildGoogleApiClient()
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }
}