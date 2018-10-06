package com.mop.friendflare

import android.content.ContentValues
import java.util.*

class LocationRequest {


    var id: Int? = null
    var date: Date? = null
    var locationState: LocationRequestState? = null
    var phoneNumber: String = ""
    var requested: String? = null
    var reqNote: String? = null
    var latitude: String? = null
    var longitude: String? = null

    constructor(id: Int, locationState: LocationRequestState, date: Date, phoneNumber: String, whoRequested: String, reqNote: String, latitude: String, longitude: String) {
        this.id = id
        this.date = date
        this.locationState = locationState
        this.phoneNumber = phoneNumber
        this.requested = whoRequested
        this.reqNote = reqNote
        this.latitude = latitude
        this.longitude = longitude
    }

    fun toContext() : ContentValues {
        //Create a new location request
        var values = ContentValues()
        val tempState = this.locationState.toString()
        val tempDate  = this.date?.time
        values.put(LocationRequestDbManager.COL_ID, this.id)
        values.put(LocationRequestDbManager.COL_STATE, tempState )
        values.put(LocationRequestDbManager.COL_NUMBER, this.phoneNumber)
        values.put(LocationRequestDbManager.COL_REQUESTER, this.requested)
        values.put(LocationRequestDbManager.COL_NOTE, this.reqNote)
        values.put(LocationRequestDbManager.COL_REQUEST_DATE, tempDate)
        values.put(LocationRequestDbManager.COL_LATITUDE, latitude)
        values.put(LocationRequestDbManager.COL_LONGITUDE, longitude)
        return values;
    }
}