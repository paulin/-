package com.mop.atat

enum class LocationRequestState(val type: String) {
    NEW("NEW"),
    SENT("SENT"),
    FAIL("FAIL");
    companion object {
        fun getLocationRequestStateName(name: String) = valueOf(name.toUpperCase())
    }
}