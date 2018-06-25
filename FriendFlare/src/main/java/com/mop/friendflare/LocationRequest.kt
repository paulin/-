package com.mop.friendflare

import java.time.LocalDate

class LocationRequest {

    var id: Int? = null
    var date: LocalDate? = null
    var locationState: LocationRequestState? = null
    var phoneNumber: String? = null
    var requested: String? = null
    var reqNote: String? = null

    constructor(id: Int, locationState: LocationRequestState, date: LocalDate, phoneNumber: String, whoRequested: String, reqNote: String) {
        this.id = id
        this.date = date
        this.locationState = locationState
        this.phoneNumber = phoneNumber
        this.requested = whoRequested
        this.reqNote = reqNote
    }

}