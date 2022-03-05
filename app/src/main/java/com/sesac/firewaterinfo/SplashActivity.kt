package com.sesac.firewaterinfo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sesac.firewaterinfo.dialogs.CustomDialog
import com.sesac.firewaterinfo.common.LocationSettingBox
import com.sesac.firewaterinfo.databinding.ActivitySplashBinding
import com.sesac.firewaterinfo.permission.MyPermissionCheck
import com.sesac.firewaterinfo.permission.MyPreferenceManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var returnLocFlag = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private var rejectedCnt = 0

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()

        returnLocFlag = checkLocationIsOn()
        Log.d(MY_DEBUG_TAG, "returnLocFlag= $returnLocFlag")

        if (returnLocFlag) {
            val permissionFlag = MyPreferenceManager.getInstance().isPermission

            if (permissionFlag) {
                Handler().postDelayed({

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                MY_LATITUDE = location.latitude
                                MY_LONGITUDE = location.longitude
                                Log.d(MY_DEBUG_TAG, "latitude= $MY_LATITUDE , longitude= $MY_LONGITUDE")
                            }
                        }

                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 2500L)
            } else {
                rejectedCnt++
                Log.d(MY_DEBUG_TAG, "rejectedCnt= $rejectedCnt")

                if (rejectedCnt > 2) {
                    CustomDialog(this).apply {
                        setCanceledOnTouchOutside(false)
                    }.show()
                } else {
                    permissionCheck()
                }
            }
        }
    }

    private lateinit var permissionCheck: MyPermissionCheck

    private fun permissionCheck() {
        permissionCheck = MyPermissionCheck(applicationContext, this)

        if (!permissionCheck.checkPermission()) {
            permissionCheck.requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty()) return

        if (permissionCheck.permissionResult(requestCode, grantResults)) {
            MyPreferenceManager.getInstance().isPermission = true
        }
    }

    private fun checkLocationIsOn(): Boolean {
        val mListener = getSystemService(LOCATION_SERVICE) as LocationManager

        val isGPSLocation = mListener.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkLocation = mListener.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        when {
            isGPSLocation -> {
                return true
            }
            isNetworkLocation -> {
                return true
            }
            else -> {
                // 단말기에 위치 설정이 되어 있지 않다면 설정 화면으로 이동한다.
                // PermissionCheckUtil kotlin 파일의 Top-Level 클래스를 호출
                val manager: FragmentManager = supportFragmentManager
                LocationSettingBox.newInstance(this@SplashActivity).showNow(manager, "위치설정")
                return false
            }
        }
    }

}