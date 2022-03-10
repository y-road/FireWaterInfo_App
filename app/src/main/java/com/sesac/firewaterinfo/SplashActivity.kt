package com.sesac.firewaterinfo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.util.Base64Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.dialogs.CustomDialog
import com.sesac.firewaterinfo.common.LocationSettingBox
import com.sesac.firewaterinfo.common.RestFunction
import com.sesac.firewaterinfo.databinding.ActivitySplashBinding
import com.sesac.firewaterinfo.fragments.MapFragment
import com.sesac.firewaterinfo.permission.MyPermissionCheck
import com.sesac.firewaterinfo.permission.MyPreferenceManager
import java.lang.StringBuilder
import java.security.DigestException
import java.security.MessageDigest
import java.util.Base64.getEncoder
import kotlin.experimental.and

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var returnLocFlag = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var fm = FuncModule()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // dp 사이즈 비율 구하기 위해 테스트하는 코드------------
//        val display = windowManager.defaultDisplay
//        val outmetrics = DisplayMetrics()
//        display.getMetrics(outmetrics)
//        val density = resources.displayMetrics.density
//        Log.d(MY_DEBUG_TAG, "outmetrics= $outmetrics")
        // ------------------------------------------------------

        window.statusBarColor = Color.rgb(235, 94, 40)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val md = fm.readLastLocation()

        if (md[0] != 0.0 && md[1] != 0.0) {
            MY_LATITUDE = md[0]
            MY_LONGITUDE = md[1]
            Log.d(MY_DEBUG_TAG, "lastLocation= ${md[0]} , ${md[1]}")
        }
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

                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {

                        MY_LATITUDE = location.latitude
                        MY_LONGITUDE = location.longitude


                        fm.saveLastLocation(MY_LATITUDE.toString(), MY_LONGITUDE.toString())
                    }
                }

                val km = 3.0
                val degree = 0.009

                val rf = RestFunction()
                rf.selectSimpleFW(
                    MY_LATITUDE - (km * degree),
                    MY_LATITUDE + (km * degree),
                    MY_LONGITUDE - (km * degree),
                    MY_LONGITUDE + (km * degree))

                Handler().postDelayed({

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