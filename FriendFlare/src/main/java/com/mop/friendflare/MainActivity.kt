package com.mop.friendflare

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
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {

    private var listRequests = ArrayList<LocationRequest>()

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.getDefault())


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
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }

    fun loadQueryAll() {

        var dbManager = LocationRequestDbManager(this)
        val cursor = dbManager.queryAll()

        listRequests.clear()
        if (cursor.moveToFirst()) {

            do {
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val state = LocationRequestState.getLocationRequestStateName(cursor.getString(cursor.getColumnIndex("State")))
                val date = LocalDate.ofEpochDay(cursor.getLong(cursor.getColumnIndex("Date")))
                val phoneNumber = cursor.getString(cursor.getColumnIndex("Phone"))
                val who = cursor.getString(cursor.getColumnIndex("Who"))
                val note = cursor.getString(cursor.getColumnIndex("Note"))

                listRequests.add(LocationRequest(id, state, date, phoneNumber, who, note))

            } while (cursor.moveToNext())
        }

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

            vh.tvTitle.text = mPing.requested
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