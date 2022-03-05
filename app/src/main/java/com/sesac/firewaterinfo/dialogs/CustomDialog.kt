package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.sesac.firewaterinfo.R

class CustomDialog(private val owner: Activity) : Dialog(owner) {

    private lateinit var confirmBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_diaalog)

        confirmBtn = findViewById(R.id.qYesBtn)

        confirmBtn.setOnClickListener {
            owner.finish()
        }
    }
}