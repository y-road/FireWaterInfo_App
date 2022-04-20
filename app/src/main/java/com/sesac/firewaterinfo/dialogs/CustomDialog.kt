package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", owner.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }
}