package com.sesac.firewaterinfo.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.common.RestFunction
import com.sesac.firewaterinfo.databinding.FragmentMapBinding
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        lateinit var locationSource: FusedLocationSource
        lateinit var naverMap: NaverMap
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        fun newInstance(): MapFragment {
            return MapFragment()
        }

        fun reloadMarkers() {
            if (MARKERSS.isNotEmpty()) {
                MARKERSS.forEach {
                    it.map = naverMap
                }
            }
        }
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var nowCamPos: CameraPosition

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        with(binding) {
            mapView.apply {
                onCreate(savedInstanceState)
                getMapAsync(this@MapFragment)
            }

            refreshBtn.setOnClickListener {
                val rf = RestFunction()
                with(nowCamPos.target) {
                    rf.selectSimpleFW(latitude,longitude)
                }
                it.isVisible = false
            }

            FIRST_START_APPLICATION = false

            return root
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(nMap: NaverMap) {

        with(binding) {
            nMap.also {
                zoomBtn.map = it
                compassBtn.map = it
                locationBtn.map = it
            }
        }

        nMap.uiSettings.apply {
            isCompassEnabled = false
            isZoomControlEnabled = false
            isLocationButtonEnabled = false
            setLogoMargin(0, 0, 0, 0)
        }

        var camPos = CameraPosition(LatLng(MY_LATITUDE, MY_LONGITUDE), 16.0)

        naverMap = nMap.apply {

            cameraPosition = camPos
            extent = LatLngBounds(LatLng(31.43, 122.37), LatLng(44.35, 132.0))
            minZoom = 6.0
            maxZoom = 19.0

            locationSource = Companion.locationSource
            locationTrackingMode = LocationTrackingMode.Follow
//            addOnCameraChangeListener {reason, animated ->
//                if (reason == -1) {
//                    Log.d(MY_DEBUG_TAG, "addOnCameraChangeListener: $reason")
//
//                }
//            }
            addOnCameraIdleListener {
                binding.refreshBtn.also {
                    if (!it.isVisible) it.isVisible = true
                    nowCamPos = cameraPosition

                    val fm = FuncModule()
                    with(nowCamPos.target) {
                        fm.saveLastLocation(latitude.toString(), longitude.toString())
                    }
                }
            }

            setOnMapClickListener { pointF, latLng ->
                binding.searchText.also {
                    if (it.isFocused) it.clearFocus()
                }
            }


        }

        setMarkersMap()
    }

    private fun setMarkersMap() {
        MARKERSS.forEach {
            it.map = naverMap
        }
    }


}

















