package com.sesac.firewaterinfo.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.common.RestFunction
import com.sesac.firewaterinfo.databinding.FragmentLoginBinding
import kotlinx.coroutines.*
import kotlin.math.log

class LoginFragment : Fragment() {

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            userID.addTextChangedListener {
                if (userID.length() >= 13) {
                    userPW.requestFocus()
                }
            }

            userID.setOnFocusChangeListener { view, hasFocus ->
                Log.d(MY_DEBUG_TAG, "hasFocus = $hasFocus")

                when (hasFocus) {
                    true -> userIDLayout.error = null
                    else -> if (userID.length() in 1..12) {
                        userIDLayout.error = "디지털 코드는 13자리 입니다"
                    }
                }
            }

            userPW.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    scrollViewLogin.scrollTo(0, logoLogin.top)

                }
            }

            userPW.addTextChangedListener {
                if (userPW.length() >= 8) {
                    btnLogin.apply {
                        isEnabled = true
                        background.setTint(Color.parseColor("#FFEB5E28"))
//                        userPWLayout.error = null
                        userPWLayout.helperText = null
//                        (activity as MainActivity).hideKeyboard(true)
                    }
                } else {
                    btnLogin.apply {
                        isEnabled = false
                        background.setTint(Color.parseColor("#3F3F3F"))
//                        userPWLayout.error = "조건에 알맞게 입력하세요"
                        userPWLayout.helperText = "비밀번호는 8자리 이상입니다"

                    }
                }
            }

            btnLogin.setOnClickListener {
                (activity as MainActivity).hideKeyboard(true)

                val idStr = userID.text.toString()

                val idLong = when (isNumeric(idStr)) {
                    true -> idStr.toLong()
                    else -> 0
                }

                val fc = FuncModule()
                val pw256: String = fc.getSign(userPW.text.toString())
                Log.d(MY_DEBUG_TAG, pw256)

                if (userID.length() >= 13 && userPW.length() >= 8) {
                    with(CoroutineScope(Dispatchers.IO)) {
                        val job1: Job = launch {
                            LOG_ON_STATUS.result = false
                            val rf = RestFunction()
                            rf.sendLoginInfo(idLong, "\"${pw256}\"")
                        }
                    }
                    with(CoroutineScope(Dispatchers.Main)) {
                        val job2: Job = launch {
                            var ctFlag = true
                            var tryCnt = 0
                            progressbar.visibility = View.VISIBLE

                            while (ctFlag) {
                                if (++tryCnt > 6) ctFlag = false
                                Log.d(MY_DEBUG_TAG, "login tryCnt= $tryCnt")

                                if (LOG_ON_STATUS.result) {

                                    (activity as MainActivity).clickBottomMenu(R.id.fireHydrantItem)
                                    MY_INFO!!.digital_code = LOG_ON_STATUS.digital_code
                                    MY_INFO!!.name = LOG_ON_STATUS.name
                                    Toast.makeText(this@LoginFragment.context,
                                        "${LOG_ON_STATUS.name}님 안녕하세요",
                                        Toast.LENGTH_SHORT).show()

                                    ctFlag = false
                                }
                                delay(200)
                            }

                            progressbar.visibility = View.GONE
                        }
                    }

                } else {
                    if (userID.length() < 13) {
                        userIDLayout.error = "디지털 코드는 13자리 입니다."
                    }
                    if (userPW.length() < 8) {
                        userPWLayout.error = "조건에 알맞게 입력하세요"
                    }
                }


            }


        }
    }

    fun isNumeric(str: String) = str.all { it in '0'..'9' }

    override fun onHiddenChanged(hidden: Boolean) { // hide: true, show: false
        super.onHiddenChanged(hidden)
        if (hidden) {
            Log.d(MY_DEBUG_TAG, "onHiddenChanged= $hidden")

        } else {
            Log.d(MY_DEBUG_TAG, "onHiddenChanged= $hidden")
        }
    }

}