package com.mop.friendflare

enum class LocationRequestState(val type: String) {
    NEW("new"),
    SENT("sent"),
    FAIL("fail");
    companion object {
        fun getLocationRequestStateName(name: String) = valueOf(name.toUpperCase())
    }
}