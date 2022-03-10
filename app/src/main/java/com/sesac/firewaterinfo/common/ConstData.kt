package com.sesac.firewaterinfo

import android.content.Context
import com.naver.maps.map.overlay.Marker
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.LoginResult
import com.sesac.firewaterinfo.common.data.PersonInfo

var MY_LATITUDE: Double = 37.885340
var MY_LONGITUDE: Double = 127.729823
const val MY_DEBUG_TAG: String = "SSONG"

var MY_PERMISSION_ACCESS_LOCATION: Int = 100

const val ADDRESS_PORT = "59.9.249.206:8081"
const val TARGET_URL = "http://$ADDRESS_PORT"
const val FIREWATER_IMAGE_ADDRESS = "http://$ADDRESS_PORT/api/firewater/picture?name="
const val PERSON_IMAGE_ADDRESS = "http://$ADDRESS_PORT/api/firewater/pictureMe?name="

var MARKERSS = mutableListOf<Marker>()
var MYFWS = mutableListOf<AllFW>()

val SHARED_PREFERENCE = FireApplication.getFireApplication()?.getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)

var FIRST_START_APPLICATION: Boolean = true

var MY_INFO: PersonInfo? = PersonInfo(0,0L,"","","","","","","")

var LOG_ON_STATUS: LoginResult = LoginResult(name = "", digital_code=0L, result = false)

