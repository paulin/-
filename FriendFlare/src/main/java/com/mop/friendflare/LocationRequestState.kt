package com.mop.friendflare

enum class LocationRequestState(val value: Int) {
    NEW(1),
    SENT(2),
    FAIL(3)
    private val map = LocationRequestState.values().associateBy(LocationRequestState::value);
    fun fromInt(type: Int) = map[type]
}