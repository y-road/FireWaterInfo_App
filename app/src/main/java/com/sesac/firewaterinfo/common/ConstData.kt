package com.sesac.firewaterinfo

import android.content.Context
import com.naver.maps.map.overlay.Marker
import com.sesac.firewaterinfo.common.FireApplication

var MY_LATITUDE: Double = 37.885340
var MY_LONGITUDE: Double = 127.729823
const val MY_DEBUG_TAG: String = "SSONG"

var MY_PERMISSION_ACCESS_LOCATION: Int = 100

const val ADDRESS_PORT = "59.9.249.206:8081"
const val TARGET_URL = "http://$ADDRESS_PORT"
var MARKERSS = mutableListOf<Marker>()

val SHARED_PREFERENCE = FireApplication.getFireApplication()?.getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)

var FIRST_START_APPLICATION: Boolean = true


