package com.sesac.firewaterinfo.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.common.util.Base64Utils
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.SHARED_PREFERENCE
import java.security.DigestException
import java.security.MessageDigest

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

    fun getSign(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}