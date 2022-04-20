package com.sesac.firewaterinfo.common.data

import android.content.Context
import android.content.SharedPreferences
import com.sesac.firewaterinfo.common.FireApplication

object SharedPrefManager {

    private const val SHARED_AUTO_LOGIN = "shared_auto_login"
    private const val KEY_FF_NAME = "key_f_name"
    private const val KEY_FF_DIGITAL_CODE = "key_f_digital_code"
    private const val KEY_FF_RESULT = "key_f_result"

    private const val SHARED_PROFILE_PATH = "shared_my_profile"
    private const val KEY_MY_PROFILE_PATH = "key_my_profile"

    // 개인 정보 저장
    fun saveMyLoginInfo(ffName: String, ffDigitalCode: Long, ffResult:Boolean) {

        val shared:SharedPreferences = FireApplication.getFireApplication().getSharedPreferences(
            SHARED_AUTO_LOGIN, Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = shared.edit()

        editor.putString(KEY_FF_NAME, ffName)
        editor.putLong(KEY_FF_DIGITAL_CODE, ffDigitalCode)
        editor.putBoolean(KEY_FF_RESULT, ffResult)

        editor.apply()
    }

    // 개인 정보 로드
    fun loadMyLoginInfo(): LoginResult? {

        val shared:SharedPreferences = FireApplication.getFireApplication().getSharedPreferences(
            SHARED_AUTO_LOGIN, Context.MODE_PRIVATE)

        val storedName = shared.getString(KEY_FF_NAME, "")!!
        val storedDigitalCode = shared.getLong(KEY_FF_DIGITAL_CODE, 0L)
        val storedResult = shared.getBoolean(KEY_FF_RESULT, false)

        val loginResult: LoginResult? = null

        return when {
            (storedName == "" || storedDigitalCode == 0L || !storedResult) -> loginResult
            else -> LoginResult(storedName, storedDigitalCode, storedResult)
        }
    }

    // 프로필 사진 경로 저장
    fun saveMyProfilePath(picPath: String) {
        val shared: SharedPreferences = FireApplication.getFireApplication().getSharedPreferences(
            SHARED_PROFILE_PATH, Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = shared.edit()

        editor.putString(KEY_MY_PROFILE_PATH, picPath)
        editor.apply()
    }

    // 프로필 사진 경로 로드
    fun loadMyProfilePath(): String {
        val shared: SharedPreferences = FireApplication.getFireApplication().getSharedPreferences(
            SHARED_PROFILE_PATH, Context.MODE_PRIVATE)

        return shared.getString(KEY_MY_PROFILE_PATH, "")!!
    }
}
