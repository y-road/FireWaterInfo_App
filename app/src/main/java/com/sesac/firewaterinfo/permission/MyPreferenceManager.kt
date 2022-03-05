package com.sesac.firewaterinfo.permission

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.sesac.firewaterinfo.common.FireApplication


/**
 * 현재 프리퍼런스 키값
 */
private const val IS_LOCATION = "is_location"

class MyPreferenceManager {

    companion object {
        private lateinit var manager: MyPreferenceManager
        private lateinit var sp: SharedPreferences
        private lateinit var spEditor: SharedPreferences.Editor

        @Suppress("DEPRECATION")
        @SuppressLint("CommitPrefEdits")
        fun getInstance(): MyPreferenceManager {
            if (this::manager.isInitialized) {
                return manager
            } else {
                sp = PreferenceManager.getDefaultSharedPreferences(
                    FireApplication.getFireApplication())
                spEditor = sp.edit()
                manager = MyPreferenceManager()
            }
            return manager
        }
    }

    /**
     * 본 앱의 퍼미션 체크 여부
     */
    var isPermission : Boolean
        get() = sp.getBoolean(IS_LOCATION, false)
        set(permissionCheck) {
            with(spEditor){
                putBoolean(IS_LOCATION, permissionCheck).apply()
            }
        }

}