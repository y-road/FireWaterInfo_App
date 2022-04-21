package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.google.android.material.button.MaterialButton
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.FireApplication

class CustomDialog(private val owner: Activity) : Dialog(owner) {

    private lateinit var confirmBtn: MaterialButton
    private lateinit var exitDescTV: TextView

    private var str = ""

    val clipboard = FireApplication.getFireApplication().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    companion object {
        fun newInstance(owner: Activity, msg: String): CustomDialog {
            val cd = CustomDialog(owner)

            with(cd) {
                str = msg
                return this
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_diaalog)


        confirmBtn = findViewById(R.id.qYesBtn)
        exitDescTV = findViewById(R.id.exitDescTV)

        exitDescTV.text = str

        confirmBtn.setOnClickListener {
            owner.finish()

            if (str.contains("권한")) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", owner.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
        }

        exitDescTV.setOnClickListener {

            val clip = ClipData.newPlainText("simple text", "guswls9119@korea.kr")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(FireApplication.getFireApplication(),"클립보드에 복사되었습니다.",Toast.LENGTH_SHORT).show()
        }
    }
}