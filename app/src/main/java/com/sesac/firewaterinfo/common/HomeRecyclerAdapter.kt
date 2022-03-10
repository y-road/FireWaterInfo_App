package com.sesac.firewaterinfo.common

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sesac.firewaterinfo.FIREWATER_IMAGE_ADDRESS
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.data.AllFW

class HomeRecyclerAdapter(
    private val myFWList: MutableList<AllFW>,
    private val owner: Context,
) : RecyclerView.Adapter<HomeRecyclerAdapter.FWHolder>() {

    inner class FWHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fwAliveTv: TextView = itemView.findViewById(R.id.fwIsAliveTV)
        val fwNameTv: TextView = itemView.findViewById(R.id.fwNameTV)
        val fwAddrTv: TextView = itemView.findViewById(R.id.fwAddressTV)
        val fwDescTv: TextView = itemView.findViewById(R.id.fwDescTV)
        val fwDateTv: TextView = itemView.findViewById(R.id.fwDateTV)
        val fwImg: ImageView = itemView.findViewById(R.id.fwImageV)
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

            if (fwEntity.img_name.isNotEmpty()) {
                val targetImage = "${FIREWATER_IMAGE_ADDRESS}${fwEntity.img_name}"
                Glide.with(this.fwImg)
                    .load(Uri.parse(targetImage))
                    .error(R.drawable.ic_no_img)
                    .transform(CenterCrop(), RoundedCorners(20))
                    .into(this.fwImg)
            }
        }
    }

    override fun getItemCount(): Int = myFWList.size
}