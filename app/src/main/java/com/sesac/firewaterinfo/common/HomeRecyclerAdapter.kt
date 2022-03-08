package com.sesac.firewaterinfo.common

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.data.AllFW

class HomeRecyclerAdapter(
    private val myFWList:MutableList<AllFW>,
    private val owner: Context
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
            if (!fwEntity.available) fwAliveTv.setBackgroundResource(R.drawable.death_background)

            fwNameTv.text = fwEntity.name
            fwAddrTv.text = fwEntity.address
            fwDescTv.text = fwEntity.detail
            fwDateTv.text = "최근 업데이트: ${fwEntity.update_date}"
        }
    }

    override fun getItemCount(): Int = myFWList.size
}