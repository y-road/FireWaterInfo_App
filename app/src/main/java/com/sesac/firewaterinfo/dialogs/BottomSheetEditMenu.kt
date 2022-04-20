package com.sesac.firewaterinfo.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.databinding.BottomEditMenuBinding

class BottomSheetEditMenu: BottomSheetDialogFragment() {

    private var _binding: BottomEditMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var ownerDialog: BottomSheetDialogFragment
    private lateinit var allData: AllFW

    fun newInstance(ownerD: BottomSheetDialogFragment, ownerData: AllFW) : BottomSheetEditMenu {

        val dia = BottomSheetEditMenu()
        dia.ownerDialog = ownerD
        dia.allData = ownerData

        return dia
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BottomEditMenuBinding.inflate(inflater, container, false)


        with(binding) {

            editText.setOnClickListener {
                ownerDialog.dismiss()
//                val botEditMenu = BottomSheetEditMenu().newInstance(this@BottomSheetDialogDetail, alfw2)
//                botEditMenu.show(parentFragmentManager, botEditMenu.tag)
                val botde = BottomSheetDialogEdit(allData)
                botde.show(parentFragmentManager, botde.tag)
                dismiss()
            }

            return root
        }
    }

}