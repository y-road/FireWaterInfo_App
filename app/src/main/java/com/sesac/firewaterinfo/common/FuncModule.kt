package com.sesac.firewaterinfo.common

import com.sesac.firewaterinfo.SHARED_PREFERENCE

class FuncModule {

    fun saveLastLocation(lat: String, lon: String) {
        with(SHARED_PREFERENCE.edit()) {
            putString("lastLatitude", lat)
            putString("lastLongitude", lon)
            apply()
        }
    }

    fun readLastLocation(): MutableList<Double> {
        val md = mutableListOf<Double>()

        val lat = SHARED_PREFERENCE.getString("lastLatitude", "0")
        val lon = SHARED_PREFERENCE.getString("lastLongitude", "0")
        md.add(lat!!.toDouble())
        md.add(lon!!.toDouble())

        return md
    }

}