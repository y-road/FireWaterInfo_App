package com.sesac.firewaterinfo.common

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.dialogs.CustomDialog

class LocationSettingBox(private val owner: Activity) : DialogFragment() {
    companion object {
        fun newInstance(currentActivity: Activity): LocationSettingBox {
            return LocationSettingBox(currentActivity)
        }
        var alertCancelCnt = 0

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(owner)
            .setMessage("단말기의 위치설정이 켜져 있어야 합니다.")
            .setCancelable(false)
            .setPositiveButton("확인") { _, _ ->
                with(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(this)
                }
            }.create()
    }

    override fun onStop() {
        super.onStop()
        alertCancelCnt++
        if (alertCancelCnt > 1) {
            alertCancelCnt = 0
            owner.finish()
        }
    }
}
