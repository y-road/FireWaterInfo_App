package com.sesac.firewaterinfo.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.MY_LATITUDE
import com.sesac.firewaterinfo.MY_LONGITUDE
import com.sesac.firewaterinfo.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        lateinit var locationSource: FusedLocationSource
        lateinit var naverMap: NaverMap
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

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

            return root
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
            setLogoMargin(0,0,0,0)
        }

        var camPos = CameraPosition(LatLng(MY_LATITUDE, MY_LONGITUDE), 16.0)

        nMap.cameraPosition = camPos

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow


        naverMap = nMap


    }
}



































