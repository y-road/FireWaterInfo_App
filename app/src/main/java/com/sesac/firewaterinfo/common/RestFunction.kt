package com.sesac.firewaterinfo.common

import android.app.Person
import android.graphics.Color
import android.graphics.ColorSpace
import android.util.Log
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.MarkerIcons
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.LoginResult
import com.sesac.firewaterinfo.common.data.PersonInfo
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.fragments.MapFragment
import okhttp3.ResponseBody
import org.json.JSONArray
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


    fun selectMyFW(digitalCode: Long) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<List<AllFW>> = firewaterService.requestMyFWSelect(digitalCode)

        call.enqueue(object : Callback<List<AllFW>> {
            override fun onResponse(
                call: Call<List<AllFW>>,
                response: Response<List<AllFW>>,
            ) {
                if (response.isSuccessful) {
                    val allFWList = response.body() as MutableList<AllFW>
                    Log.d(MY_DEBUG_TAG, "allFWList= $allFWList")
                    if (allFWList.isEmpty()) {
                        Toast.makeText(FireApplication.getFireApplication(),"내가 관리 중인 소화 용수가 없어요", Toast.LENGTH_SHORT).show()
                    } else {
                        initMyFW(allFWList)
                    }
                }
            }

            override fun onFailure(call: Call<List<AllFW>>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "내 소화전 정보를 읽어오는데 실패했습니다.",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    private fun initMyFW(myFWList: MutableList<AllFW>) {
        if (MYFWS.size > 0) {
            MYFWS.clear()
            Log.d(MY_DEBUG_TAG, "initMyFW(myFWList: List<AllFW>)")
        }
        MYFWS = myFWList
    }

    fun selectMyInfo(digitalCode: Long) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<PersonInfo> = firewaterService.selectMyInfo(digitalCode)

        call.enqueue(object : Callback<PersonInfo> {
            override fun onResponse(
                call: Call<PersonInfo>,
                response: Response<PersonInfo>,
            ) {
                if (response.isSuccessful) {
                    val myInfo = response.body() as PersonInfo
                    Log.d(MY_DEBUG_TAG, "myInfo= $myInfo")

                    MY_INFO = myInfo
                }
            }

            override fun onFailure(call: Call<PersonInfo>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "내 정보를 불러올 수 없음",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    fun sendLoginInfo(digitalCode: Long, pw: String) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<LoginResult> = firewaterService.selectLoginInfo(digitalCode, pw)

        call.enqueue(object : Callback<LoginResult> {
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                if (response.isSuccessful) {
                    val result = response.body() as LoginResult
                    LOG_ON_STATUS.result = result.result
                    LOG_ON_STATUS.name = result.name
                    LOG_ON_STATUS.digital_code = result.digital_code
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "통신 상태가 좋지 않아 로그인에 실패하였습니다\n잠시후 다시 시도해 주시기 바랍니다",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })



    }


}