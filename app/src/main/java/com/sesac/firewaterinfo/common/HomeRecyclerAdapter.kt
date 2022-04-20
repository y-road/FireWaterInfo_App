package com.sesac.firewaterinfo.common

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.CntResult
import com.sesac.firewaterinfo.common.data.MyFavorites
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.fragments.MapFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRecyclerAdapter(
    private val myFWList: List<MyFavorites>,
    private val owner: Context,
) : RecyclerView.Adapter<HomeRecyclerAdapter.FWHolder>() {

    inner class FWHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fwAliveTv: TextView = itemView.findViewById(R.id.fwIsAliveTV)
        val fwNameTv: TextView = itemView.findViewById(R.id.fwNameTV)
        val fwAddrTv: TextView = itemView.findViewById(R.id.fwAddressTV)
        val fwDescTv: TextView = itemView.findViewById(R.id.fwDescTV)
        val fwDateTv: TextView = itemView.findViewById(R.id.fwDateTV)
        val fwGotoTv: TextView = itemView.findViewById(R.id.goToMarkerTV)
        val fwRemoveFavorites: ImageButton = itemView.findViewById(R.id.removeFavoriteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FWHolder {
        val view = LayoutInflater.from(owner).inflate(R.layout.my_firewater_mold, parent, false)
        return FWHolder(view)
    }

    override fun onBindViewHolder(holder: FWHolder, position: Int) {

        val fwEntity = myFWList[position]
        Log.d(MY_DEBUG_TAG, "onBindViewHolder position= $position")
        with(holder) {
            if (!fwEntity.available) {
                fwAliveTv.text = "사용불가"
                fwAliveTv.setBackgroundResource(R.drawable.death_background)
            }

            fwNameTv.text = fwEntity.name
            fwAddrTv.text = fwEntity.address
            fwDescTv.text = fwEntity.detail
            fwDateTv.text = "최근 업데이트: ${fwEntity.update_date}"

            fwGotoTv.setOnClickListener {

                var fireWaterList = mutableListOf<SimpleFW>()
                var simpleFWOne = SimpleFW(
                    fwEntity.number,
                    fwEntity.name,
                    fwEntity.latitude,
                    fwEntity.longitude,
                    fwEntity.available)

                fireWaterList.add(simpleFWOne)

                with((owner as MainActivity)) {
                    var camPos = CameraPosition(LatLng(fwEntity.latitude, fwEntity.longitude), 16.0)
                    MapFragment.naverMap.cameraPosition = camPos
                    fragmentMa.initMarker(fireWaterList as List<SimpleFW>)
                    clickBottomMenu(R.id.fireHydrantItem)
                }

                MARKERSS[0].performClick()
            }

            fwRemoveFavorites.setOnClickListener {

                val firewaterService = RetrofitOkHttpManager.firewaterRESTService
                val call: Call<CntResult> = firewaterService.removeFavorite(fwEntity.f_number)

                call.enqueue(object : Callback<CntResult> {
                    override fun onResponse(call: Call<CntResult>, response: Response<CntResult>) {

                        val cntResult = response.body() as CntResult

                        if (cntResult.cnt >= 1) {
                            (owner as MainActivity).fragmentMy!!.asyncRequestMyFavorites()
                        }

                    }

                    override fun onFailure(call: Call<CntResult>, t: Throwable) {
                        Toast.makeText(FireApplication.getFireApplication(),
                            "즐겨찾기 삭제 도중 오류가 발생했어요",
                            Toast.LENGTH_SHORT).show()
                    }
                })


            }

        }
    }

    override fun getItemCount(): Int = myFWList.size
}