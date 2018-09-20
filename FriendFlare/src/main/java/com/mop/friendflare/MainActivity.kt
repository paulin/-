package com.mop.friendflare

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    private var listRequests = ArrayList<LocationRequest>()

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.getDefault())
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        //Add it to the database
        var dbManager = LocationRequestDbManager(this)
        val mID = dbManager.insert(values)
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

            vh.ivEdit.setOnClickListener {
                updateNote(mPing)
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
        val ivEdit: ImageView
        val ivDelete: ImageView

        init {
            this.tvTitle = view?.findViewById(R.id.tvTitle) as TextView
            this.tvContent = view?.findViewById(R.id.tvContent) as TextView
            this.ivEdit = view?.findViewById(R.id.ivEdit) as ImageView
            this.ivDelete = view?.findViewById(R.id.ivDelete) as ImageView
        }
    }
}