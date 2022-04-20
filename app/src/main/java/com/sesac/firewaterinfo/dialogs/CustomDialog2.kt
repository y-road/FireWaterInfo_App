package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.fragments.SignFragment

class CustomDialog2(private val owner: Activity, private val fragment: Fragment, val msg: String) : Dialog(owner) {

    private lateinit var confirmBtn: MaterialButton
    private lateinit var descTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_diaalog2)

        confirmBtn = findViewById(R.id.qYesBtn2)

        descTV = findViewById(R.id.exitDescTV2)
        descTV.text = msg

        confirmBtn.setOnClickListener {
            with((owner as MainActivity)) {
                removeFragment(fragment)
                viewBottomLayout(true)
                this@CustomDialog2.dismiss()
            }
        }
    }
}