package com.sesac.firewaterinfo.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.data.EditedHistory

class HomeRecyclerAdapterEditHistory(
    private val myEHList: MutableList<EditedHistory>,
    private val owner: Context,
) : RecyclerView.Adapter<HomeRecyclerAdapterEditHistory.EHHolder>() {

    inner class EHHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val IVEditPhoto : ImageView = itemView.findViewById(R.id.IVEditedPhoto)
        val TVEditName: TextView = itemView.findViewById(R.id.TVEditedName)
        val TVEditType: TextView = itemView.findViewById(R.id.TVEditedType)
        val TVEditDate: TextView = itemView.findViewById(R.id.TVEditedDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EHHolder {
        val view = LayoutInflater.from(owner).inflate(R.layout.my_edited_history_mold, parent, false)
        return EHHolder(view)
    }

    override fun onBindViewHolder(holder: EHHolder, position: Int) {

        val fwEHEntity = myEHList[position]

        with(holder) {
            if (fwEHEntity.editedType == "photo") {

                TVEditType.text = "사진"
                TVEditType.setBackgroundResource(R.drawable.eh_photo_bg)
            } else {

                TVEditType.text = "내용"
                TVEditType.setBackgroundResource(R.drawable.eh_text_bg)
            }

            TVEditName.text = fwEHEntity.fwName
            TVEditDate.text = fwEHEntity.editedDate
        }
    }

    override fun getItemCount(): Int = myEHList.size
}
