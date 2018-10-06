package com.mop.friendflare

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 41
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 42
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 43
    private var MY_PERMISSIONS_REQUEST_LOCATION_CODE = 44

    private var listRequests = ArrayList<LocationRequest>()

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.getDefault())
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAllPermissions()
        loadQueryAll()

        lvRequests.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            Toast.makeText(this, "Click on " + listRequests[position].phoneNumber, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.sendLocation -> {
                    var intent = Intent(this, SendLocationActivity::class.java)
                    startActivity(intent)
                }
                R.id.fake_request -> {
                    makeFakeRequest()
                }
                R.id.action_settings -> {
                    var intent = Intent(this, DeveloperActivity::class.java)
                    startActivity(intent)
                }
                R.id.send_responses -> {
                    val i = Intent()
                    //					i.putExtra(WhereMain.FROM_NUMBER, fromNumber);  Doesn't seem to work for later versions
                    i.setClass(this, GPSService::class.java)
                    startService(i)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }

    fun makeFakeRequest() {
        //Create a new location request
        var values = ContentValues()
        val tempState = LocationRequestState.NEW.type
        val tempDate = System.currentTimeMillis()
        values.put(LocationRequestDbManager.COL_STATE, tempState)
        values.put(LocationRequestDbManager.COL_NUMBER, "2066836567")
        values.put(LocationRequestDbManager.COL_REQUESTER, "Fakey McFake Face")
        values.put(LocationRequestDbManager.COL_NOTE, "This isn't real")
        values.put(LocationRequestDbManager.COL_REQUEST_DATE, tempDate)
        values.put(LocationRequestDbManager.COL_LATITUDE, "47.6062")
        values.put(LocationRequestDbManager.COL_LONGITUDE, "-122.3321")

        //Add it to the database
        var dbManager = LocationRequestDbManager(this)
        val mID = dbManager.insert(values)

        loadQueryAll()
    }

    fun loadQueryAll() {

        var dbManager = LocationRequestDbManager(this)

        listRequests = dbManager.queryAll()

        var requestAdapter = LocationRequestAdapter(this, listRequests)
        lvRequests.adapter = requestAdapter
    }


    inner class LocationRequestAdapter : BaseAdapter {

        private var pingList = ArrayList<LocationRequest>()
        private var context: Context? = null

        constructor(context: Context, pingList: ArrayList<LocationRequest>) : super() {
            this.pingList = pingList
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val view: View?
            val vh: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.note, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
                Log.i("JSA", "set Tag for ViewHolder, position: " + position)
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }

            var mPing = pingList[position]

            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val formattedDate = formatter.format(mPing.date)

            vh.tvTitle.text = "[" + mPing.locationState + "] " + mPing.requested
            vh.tvContent.text = "Request from [" + mPing.phoneNumber + "] on [" + formattedDate + "]"

            vh.ivMap.setOnClickListener {
                openMap(mPing)
            }

            vh.ivDelete.setOnClickListener {
                var dbManager = LocationRequestDbManager(this.context!!)
                val selectionArgs = arrayOf(mPing.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return pingList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return pingList.size
        }
    }

    private fun openMap(locationRequest: LocationRequest) {
        val uri = String.format(Locale.ENGLISH, "geo:%s,%s", locationRequest.latitude, locationRequest.longitude)
        Log.i("MATT", "Location to maps: " + uri)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    private fun updateNote(locationRequest: LocationRequest) {
        var intent = Intent(this, LocationRequestActivity::class.java)
        intent.putExtra("MainActId", locationRequest.id)
        intent.putExtra("MainActTitle", locationRequest.requested)
        intent.putExtra("MainActContent", locationRequest.phoneNumber)
        startActivity(intent)
    }

    private class ViewHolder(view: View?) {
        val tvTitle: TextView
        val tvContent: TextView
        val ivMap: ImageView
        val ivDelete: ImageView

        init {
            this.tvTitle = view?.findViewById(R.id.tvTitle) as TextView
            this.tvContent = view?.findViewById(R.id.tvContent) as TextView
            this.ivMap = view?.findViewById(R.id.ivMap) as ImageView
            this.ivDelete = view?.findViewById(R.id.ivDelete) as ImageView
        }
    }

    private fun getAllPermissions() {
// The request code used in ActivityCompat.requestPermissions()
// and returned in the Activity's onRequestPermissionsResult()
        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            // Permission has already been granted
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION_CODE)
        }
    }

    //TODO Do I really need to do this?
    private fun loadContacts() {
        var builder = StringBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS)
            //callback onRequestPermissionsResult
        } else {
            builder = getContacts()
            //tvContacts.text = builder.toString()
        }
    }

    private fun getContacts(): StringBuilder {
        val builder = StringBuilder()
        val resolver: ContentResolver = contentResolver;
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null)

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(
                                    cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
                                    phoneNumValue).append("\n\n")
                            Log.e("Name ===>",phoneNumValue);
                        }
                    }
                    cursorPhone.close()
                }
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor.close()
        return builder
    }
}