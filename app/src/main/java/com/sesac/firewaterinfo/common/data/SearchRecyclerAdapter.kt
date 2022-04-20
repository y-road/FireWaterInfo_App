package com.sesac.firewaterinfo.common.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.R

class SearchRecyclerAdapter(
    private val mySearchList: List<MyFavorites>,
    private val owner: Context
) :RecyclerView.Adapter<SearchRecyclerAdapter.SearchHolder>() {

    inner class SearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fwNameTv: TextView = itemView.findViewById(R.id.fwNameTV3)
        val fwAddrTv: TextView = itemView.findViewById(R.id.fwAddressTV3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecyclerAdapter.SearchHolder {
        val view = LayoutInflater.from(owner).inflate(R.layout.my_searchlist__mold, parent, false)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {

        val searchEntity = mySearchList[position]
        with(holder) {

            val fireWaterList = mutableListOf<SimpleFW>()
            val simpleFWOne = SimpleFW(
                searchEntity.number,
                searchEntity.name,
                searchEntity.latitude,
                searchEntity.longitude,
                searchEntity.available)

            fireWaterList.add(simpleFWOne)

            fwNameTv.text = searchEntity.name
            fwAddrTv.text = searchEntity.address

            fwNameTv.setOnClickListener {

                (owner as MainActivity).gotoAimMarker(fireWaterList)
            }

            fwAddrTv.setOnClickListener {

                (owner as MainActivity).gotoAimMarker(fireWaterList)
            }
        }
    }

    override fun getItemCount(): Int = mySearchList.size
}