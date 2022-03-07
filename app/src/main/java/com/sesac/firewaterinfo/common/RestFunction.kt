package com.sesac.firewaterinfo.common

import android.graphics.Color
import android.graphics.ColorSpace
import android.util.Log
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.MarkerIcons
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.fragments.MapFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestFunction {

    fun selectSimpleFW(reqLatL: Double,reqLatH: Double, reqLonL: Double, reqLonR: Double) {
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
                        Toast.makeText(FireApplication.getFireApplication(),"현 위치 주변에 등록된 소화전이 없어요", Toast.LENGTH_SHORT).show()
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

    private fun initMarker(fireWaterList: List<SimpleFW>) {

        if (MARKERSS.size > 0) {
            MARKERSS.forEach {
                it.map = null
            }
            MARKERSS.clear()
            Log.d(MY_DEBUG_TAG, "initMarker(fireWaterList: List<SimpleFW>)")
        }

        val listener = Overlay.OnClickListener { o ->
            Toast.makeText(FireApplication.getFireApplication(),
                "선택된 마커는: ${o.tag} 번 소화전",
                Toast.LENGTH_SHORT).show()

            true // false: 이벤트를 맵으로 전파, true: 이벤트를 소비
        }

        fireWaterList.forEach {
            val marker = Marker().apply {
                position = LatLng(it.latitude, it.longitude)
                captionText = it.name
                tag = it.number
                icon = MarkerIcons.BLACK
                iconTintColor = Color.rgb(235, 94, 40)
                width = 75
                height = 120
                captionOffset = 20
                captionColor = Color.BLACK
                captionHaloColor = Color.WHITE

                if (it.available) {
                    subCaptionText = "(사용: 가능)"
                    subCaptionColor = Color.BLUE
                } else {
                    subCaptionText = "(사용: 불가능)"
                    subCaptionColor = Color.RED
                }
                subCaptionHaloColor = Color.WHITE
                subCaptionTextSize = 10f

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


}