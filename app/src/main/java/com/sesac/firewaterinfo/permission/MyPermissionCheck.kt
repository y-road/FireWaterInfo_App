package com.sesac.firewaterinfo.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import java.util.*

const val RETURN_VALUE = 1004

class MyPermissionCheck(private val context: Context, private val owner: Activity) {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
//        Manifest.permission.ACCESS_NETWORK_STATE,
//        Manifest.permission.READ_CONTACTS,
//        Manifest.permission.READ_CALL_LOG,
//        Manifest.permission.CALL_PHONE,
//        Manifest.permission.READ_CONTACTS,
//        Manifest.permission.WRITE_CONTACTS,
//        Manifest.permission.RECORD_AUDIO,
//        Manifest.permission.SEND_SMS
    )

    private val permissionList = mutableListOf<String>()

    fun checkPermission(): Boolean{
        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(context, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission)
            }
        }
        if(permissionList.isNotEmpty()){
            return false
        }
        return true
    }
    fun requestPermission(){
        ActivityCompat.requestPermissions(owner, permissionList.toTypedArray(), RETURN_VALUE)
    }
    fun permissionResult(requestCode : Int, grantResults : IntArray): Boolean {
        Log.d(MY_DEBUG_TAG, "requestCode= $requestCode , grantResults = ${Arrays.toString(grantResults)}}")

        val notEmptyFlag = grantResults.isNotEmpty() // 들어있으면 트루, 없으면 false

        if(requestCode == RETURN_VALUE && (notEmptyFlag)){
            Log.d(MY_DEBUG_TAG, "requestCode= $requestCode , grantResults.isNotEmpty() = ${notEmptyFlag}}")

            for(result in grantResults){
                if(result == -1){
                    return false
                }
            }
        }

        return true
    }
}













