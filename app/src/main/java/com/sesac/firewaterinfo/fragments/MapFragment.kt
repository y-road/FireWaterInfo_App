package com.sesac.firewaterinfo.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.databinding.FragmentMapBinding
import com.sesac.firewaterinfo.dialogs.BottomSheetDialogMy
import com.sesac.firewaterinfo.dialogs.BottomSheetSearch
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        lateinit var locationSource: FusedLocationSource
        lateinit var naverMap: NaverMap
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        lateinit var selectedMarker: Marker

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

//    private var lastCameraPosition: CameraPosition? = null

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var nowCamPos: CameraPosition

    var nowBottomSearchDialog: BottomSheetSearch? = null


    @SuppressLint("ClickableViewAccessibility")
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

                detailFilterTV1.setBackgroundResource(R.drawable.orange_background)
                detailFilterTV2.setBackgroundResource(R.drawable.none_background)
                detailFilterTV3.setBackgroundResource(R.drawable.none_background)

                if (naverMap.cameraPosition.zoom <= 11.0) {
                    val needZoom = (12.0 - naverMap.cameraPosition.zoom).toInt()
                    Toast.makeText(this@MapFragment.context,
                        "범위가 너무 넓어요. 지도를 $needZoom 단계만 확대해주세요",
                        Toast.LENGTH_SHORT).show()
                } else {

                    with(nowCamPos.target) {
                        val cr = naverMap.contentRegion
                        val latL: Double = cr[0].latitude
                        val latH: Double = cr[2].latitude
                        val lonL: Double = cr[0].longitude
                        val lonH: Double = cr[2].longitude

                        selectSimpleFW(latL, latH, lonL, lonH)
                        detailFilterTV1.setBackgroundResource(R.drawable.orange_background)
                    }
                }
                it.isVisible = false
            }


//            searchText.addTextChangedListener {
//                Log.d(MY_DEBUG_TAG, "addTextChangedListener: ${searchText.text}")
//
//                if (searchText.text.length in 1..6) {
//
//                }
//
//            }

            searchText.setOnClickListener {
//                        this.frameFragmentChange(this.fragmentSe)
//                    }
                nowBottomSearchDialog = BottomSheetSearch()
                nowBottomSearchDialog!!.show(parentFragmentManager, nowBottomSearchDialog!!.tag)
            }

            searchBtn.setOnClickListener {
//                    with((requireActivity() as MainActivity)) {
//                        this.frameFragmentChange(this.fragmentSe)
//                    }
                nowBottomSearchDialog = BottomSheetSearch()
                nowBottomSearchDialog!!.show(parentFragmentManager, nowBottomSearchDialog!!.tag)
            }

//            textBtnnn.setOnClickListener {
//
//                val metersPerPixel = naverMap.projection.metersPerPixel // 0.3596781224379576
//                val metersPerDp = naverMap.projection.metersPerDp // 0.9441550713996387
//                val zooml = naverMap.cameraPosition.zoom
//
//                Log.d(MY_DEBUG_TAG,"zoomLevel= $zooml , metersPerPixel = $metersPerPixel , x2 = ${metersPerPixel * 2}")
//
//            }

            FIRST_START_APPLICATION = false

            detailFilterTV1.setOnClickListener {
                detailFilterTV1.setBackgroundResource(R.drawable.orange_background)
                detailFilterTV2.setBackgroundResource(R.drawable.none_background)
                detailFilterTV3.setBackgroundResource(R.drawable.none_background)
                MARKERSS.forEach {
                    it.map = naverMap
                }
            }

            detailFilterTV2.setOnClickListener {
                detailFilterTV1.setBackgroundResource(R.drawable.none_background)
                detailFilterTV2.setBackgroundResource(R.drawable.orange_background)
                detailFilterTV3.setBackgroundResource(R.drawable.none_background)

                clearMarkersMap()

                for (markers in MARKERSS) {
                    if (markers.subCaptionText == "1") {
                        markers.map = naverMap
                    }
                }
            }

            detailFilterTV3.setOnClickListener {
                detailFilterTV1.setBackgroundResource(R.drawable.none_background)
                detailFilterTV2.setBackgroundResource(R.drawable.none_background)
                detailFilterTV3.setBackgroundResource(R.drawable.orange_background)

                clearMarkersMap()

                for (markers in MARKERSS) {
                    if (markers.subCaptionText == "0") {
                        markers.map = naverMap
                    }
                }
            }

            locationBtn.setOnClickListener {

                var draw: Drawable
                with(naverMap) {
                    when (this.locationTrackingMode) {
                        LocationTrackingMode.Follow -> {
                            draw = resources.getDrawable(R.drawable.ic_location_face)
                            this.locationTrackingMode = LocationTrackingMode.Face
                        }
                        LocationTrackingMode.Face -> {
                            draw = resources.getDrawable(R.drawable.ic_nofollow_new)
                            this.locationTrackingMode = LocationTrackingMode.NoFollow
                        }
                        else -> {
                            draw = resources.getDrawable(R.drawable.ic_location_follow)
                            this.locationTrackingMode = LocationTrackingMode.Follow
                        }
                    }
                }
                locationBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(draw, null, null, null)
            }

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

    fun clearMarkersMap() {
        MARKERSS.forEach {
            it.map = null
        }
    }

    fun clearMarker() {
        if (MARKERSS.size > 0) {
            MARKERSS.forEach {
                it.map = null
            }
            MARKERSS.clear()
            Log.d(MY_DEBUG_TAG, "clearMarker()")
        }
    }

    override fun onMapReady(nMap: NaverMap) {
        Log.d(MY_DEBUG_TAG, "onMapReady()")

        with(binding) {
            nMap.also {
                zoomBtn.map = it
                compassBtn.map = it
            }

        }

        nMap.uiSettings.apply {
            isCompassEnabled = false
            isZoomControlEnabled = false
            isLocationButtonEnabled = false
//            isScaleBarEnabled = false
            setLogoMargin(0, 0, 0, 0)
        }

        var camPos = CameraPosition(LatLng(MY_LATITUDE, MY_LONGITUDE), 16.0)

//        if (lastCameraPosition != null) {
//            camPos = lastCameraPosition as CameraPosition
//        }

        // 지도의 유효영역 설정을 위해 필요한 px 값을 구하기 위함. 110dp -> px로 전환 (setContentPadding)
        val density = resources.displayMetrics.density // 2.625
        val topOutPx = (110 * density).toInt()

        naverMap = nMap.apply {

            setContentPadding(0, topOutPx, 0, 0)
            cameraPosition = camPos
            extent = LatLngBounds(LatLng(31.43, 122.37), LatLng(44.35, 132.0))
            minZoom = 6.0
            maxZoom = 19.0

            locationSource = Companion.locationSource
            locationTrackingMode = LocationTrackingMode.Follow

            addOnCameraChangeListener { reason, animated ->
                if (reason == -1) {

                    val activity = activity as MainActivity
                    Log.d(MY_DEBUG_TAG, "focus= ${activity.currentFocus}")

                    val draw = resources.getDrawable(R.drawable.ic_nofollow_new)
                    binding.locationBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(draw,
                        null,
                        null,
                        null)

//                    lastCameraPosition = naverMap.cameraPosition

                    Log.d(MY_DEBUG_TAG, "reason= $reason")
                    (activity as MainActivity).hideKeyboard(true)
                    binding.searchText.clearFocus()
                }
            }

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
                Log.d(MY_DEBUG_TAG, "setOnMapClickListener pointF= $pointF , latLng= $latLng")
                binding.searchText.also {
                    if (it.isFocused) {
                        it.clearFocus()
                    }
                }
            }
        }

        setMarkersMap()
        Log.d(MY_DEBUG_TAG, "${binding.locationBtn.width} ^ ${binding.locationBtn.height}")
    }

    private fun setMarkersMap() {
        MARKERSS.forEach {
            it.map = naverMap
        }
    }

//    fun clearSearchText() {
//        binding.searchText.setText("")
//    }

    fun selectSimpleFW(reqLatL: Double, reqLatH: Double, reqLonL: Double, reqLonR: Double) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<List<SimpleFW>> = firewaterService.requestSimpleFWSelect(reqLatL, reqLatH,
            reqLonL, reqLonR)

        call.enqueue(object : Callback<List<SimpleFW>> {
            override fun onResponse(
                call: Call<List<SimpleFW>>,
                response: Response<List<SimpleFW>>,
            ) {
                if (response.isSuccessful) {
                    val simpleFWList = response.body() as List<SimpleFW>
                    if (simpleFWList.isEmpty()) {
                        Toast.makeText(FireApplication.getFireApplication(),
                            "현 위치 주변에 등록된 소화전이 없어요",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        initMarker(simpleFWList)
                    }
                }
            }

            override fun onFailure(call: Call<List<SimpleFW>>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "초기 주변 소화전 정보를 읽어오는데 실패했습니다.",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }


    fun initMarker(fireWaterList: List<SimpleFW>) {

        if (MARKERSS.size > 0) {
            MARKERSS.forEach {
                it.map = null
            }
            MARKERSS.clear()
            Log.d(MY_DEBUG_TAG, "initMarker(fireWaterList: List<SimpleFW>)")
        }

        val listener = Overlay.OnClickListener { o ->
//            Toast.makeText(FireApplication.getFireApplication(),
//                "선택된 마커는: ${o.tag} 번 소화전",
//                Toast.LENGTH_SHORT).show()
            selectedMarker = o as Marker
            selectOneFWDetail(o.tag as Int)

            true // false: 이벤트를 맵으로 전파, true: 이벤트를 소비
        }

        fireWaterList.forEach {
            val marker = Marker().apply {
                position = LatLng(it.latitude, it.longitude)
                captionText = it.name
                tag = it.number
//                icon = MarkerIcons.BLACK
//                iconTintColor = Color.rgb(235, 94, 40)
                width = 50
                height = 80
                captionOffset = 20
                captionColor = Color.BLACK
                captionHaloColor = Color.WHITE

                if (it.available) {
                    subCaptionText = "1"
//                    subCaptionColor = Color.BLUE
                    icon = OverlayImage.fromResource(R.drawable.ic_marker_alive)
                } else {
                    subCaptionText = "0"
//                    subCaptionColor = Color.RED
                    icon = OverlayImage.fromResource(R.drawable.ic_marker_die)

                }
//                subCaptionHaloColor = Color.WHITE
                subCaptionTextSize = 0f

                isHideCollidedSymbols = true // 시인성 향상을 위해 마커가 표시된 부분의 지도 상 심볼을 가림
                isHideCollidedMarkers = false // 지도 축소로 인해 마커가 겹쳐질 경우 하나로 뭉쳐짐
                isHideCollidedCaptions = true // 마커의 아이콘은 유지되고 겹쳐진 캡션만 안보임

                onClickListener = listener
            }
            MARKERSS.add(marker)

            if (!FIRST_START_APPLICATION) {
                MapFragment.reloadMarkers()
            }
        }
        Log.d(MY_DEBUG_TAG, "END")
    }

    fun selectOneFWDetail(number: Int) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<AllFW> = firewaterService.selectOneFWDetail(number)

        call.enqueue(object : Callback<AllFW> {
            override fun onResponse(call: Call<AllFW>, response: Response<AllFW>) {
                if (response.isSuccessful) {
                    val fwDetail = response.body() as AllFW
                    if (fwDetail.number > 0) {
                        val bots = BottomSheetDialogMy(fwDetail).newInstance(fwDetail)
                        bots.show(parentFragmentManager, bots.tag)
                    }
                }
            }

            override fun onFailure(call: Call<AllFW>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "디테일 소화전 정보를 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    //    override fun onPause() {
//        super.onPause()
//        Log.d(MY_DEBUG_TAG,"onPause= $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(MY_DEBUG_TAG,"onStop= $this")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.d(MY_DEBUG_TAG,"onDestroyView= $this")
//    }
//

//
//    override fun onDetach() {
//        super.onDetach()
//        Log.d(MY_DEBUG_TAG,"onDetach= $this")
//    }

//    private fun hideKeyboard(hide: Boolean) {
//        val activity = activity as MainActivity
//        val view = activity.currentFocus
//
//        if (view != null) {
//            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            when (hide) {
//                true -> imm.hideSoftInputFromWindow(view.windowToken, 0)
//                else -> imm.showSoftInput(view, 0)
//            }
//        }
//    }

}

















