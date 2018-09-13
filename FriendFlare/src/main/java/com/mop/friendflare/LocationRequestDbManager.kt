package com.mop.friendflare

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.util.Date
import kotlin.collections.ArrayList

class LocationRequestDbManager {

    val TAG = "LocationRequestDbManager"
    private val dbName = "LocationRequests"
    private val dbTable = "Requests"
    private val dbVersion = 1

    companion object {
        const val COL_ID = "Id"
        const val COL_REQUEST_DATE = "Date"
        const val COL_STATE = "State"
        const val COL_NUMBER = "Number"
        const val COL_REQUESTER = "Requester"
        const val COL_NOTE = "Note"
    }


    private val CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + dbTable + " (" + COL_ID + " INTEGER PRIMARY KEY," +
            COL_REQUEST_DATE + " BIGINT, " + COL_STATE + " INTEGER, " + COL_NUMBER + " TEXT, " + COL_REQUESTER + " TEXT, " + COL_NOTE + " TEXT);"
    private var db: SQLiteDatabase? = null

    constructor(context: Context) {
        var dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
    }

    fun insert(values: ContentValues): Long {

        val ID = db!!.insert(dbTable, "", values)
        return ID
    }

    /**
     * Returns an arraylist of history in the new state
     *
     * @param    category    The category you are interested in
     */
    fun fetchLocationRequest(state: LocationRequestState): ArrayList<LocationRequest> {
        val historyArray = ArrayList<LocationRequest>()
        Log.v(LogConstants.MATT_TAG, "Fetching Location for State [" + state.name + "]")
        val query =
                "SELECT * FROM $dbTable WHERE $COL_STATE =  \"$state\""
        val cursor = db!!.rawQuery(query, null)

        var history: LocationRequest? = null
        val count = cursor.getCount()
        Log.v(TAG, "Count Location Request $count $query")
        if(cursor.moveToFirst()) {
            cursor.moveToFirst()
            for (i in 0 until count) {
                cursor.moveToPosition(i)
                var id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COL_ID)))
                var locationState = LocationRequestState.valueOf(cursor.getString(cursor.getColumnIndex(COL_STATE)))
                var date = Date(cursor.getLong(cursor.getColumnIndex(COL_REQUEST_DATE))*1000)
                var phoneNumber = cursor.getString(cursor.getColumnIndex(COL_NUMBER))
                var whoRequested = cursor.getString(cursor.getColumnIndex(COL_REQUESTER))
                var note = cursor.getString(cursor.getColumnIndex(COL_NOTE))

                history = LocationRequest(id, locationState, date, phoneNumber, whoRequested, note )
                historyArray.add(history)
            }
        }

        cursor.close()

        return historyArray
    }

    fun queryAll(): Cursor {

        return db!!.rawQuery("select * from " + dbTable, null)
    }

    fun delete(selection: String, selectionArgs: Array<String>): Int {

        val count = db!!.delete(dbTable, selection, selectionArgs)
        return count
    }

    fun update(values: ContentValues, selection: String, selectionargs: Array<String>): Int {

        val count = db!!.update(dbTable, values, selection, selectionargs)
        return count
    }

    inner class DatabaseHelper : SQLiteOpenHelper {

        var context: Context? = null

        constructor(context: Context) : super(context, dbName, null, dbVersion) {
            this.context = context
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(CREATE_TABLE_SQL)
            Toast.makeText(this.context, " database is created", Toast.LENGTH_LONG).show()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table IF EXISTS " + dbTable)
        }
    }
}