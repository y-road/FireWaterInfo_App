package com.sesac.firewaterinfo.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.LoginResult
import com.sesac.firewaterinfo.common.data.SharedPrefManager
import com.sesac.firewaterinfo.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    private var autoLoginFlag = false

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Log.d(MY_DEBUG_TAG,"LoginFragment - onCreateView")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(MY_DEBUG_TAG,"LoginFragment - onViewCreated")

        with(binding) {

            btnSignin.setOnClickListener {
                (activity as MainActivity).mainFragmentChange(SignFragment.newInstance())
            }

            toggleAutoLogin.setOnCheckedChangeListener { _, b ->
                autoLoginFlag = b
            }

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


                if (userID.length() >= 13 && userPW.length() >= 8) {

                    val fc = FuncModule()
                    val pw256: String = fc.getSign(userPW.text.toString())

                    LOG_ON_STATUS = null

                    val firewaterService = RetrofitOkHttpManager.firewaterRESTService
                    val call: Call<LoginResult> =
                        firewaterService.selectLoginInfo(idLong, "\"$pw256\"")

                    call.enqueue(object : Callback<LoginResult> {
                        override fun onResponse(
                            call: Call<LoginResult>,
                            response: Response<LoginResult>,
                        ) {
                            if (response.isSuccessful) {
                                val result = response.body() as LoginResult
                                if (result.result) {

                                    LOG_ON_STATUS = LoginResult(
                                        result = result.result,
                                        name = result.name,
                                        digital_code = result.digital_code
                                    )

                                    if (autoLoginFlag) {
                                        SharedPrefManager.saveMyLoginInfo(result.name, result.digital_code, result.result)
                                    }

                                    Toast.makeText(FireApplication.getFireApplication(),
                                        "${LOG_ON_STATUS!!.name}님 안녕하세요",
                                        Toast.LENGTH_SHORT).show()
                                    (activity as MainActivity).clickBottomMenu(R.id.fireHydrantItem)

                                    if ((activity as MainActivity).isAlreadyAdded((activity as MainActivity).fragmentMy)) {
                                        (activity as MainActivity).fragmentMy!!.asyncRequestMyFavorites()
                                    }


                                } else {
//                                    TextInputLayout
                                    userPWLayout.error = "디지털코드 또는 비밀번호가 맞지 않습니다"
                                }
                            }
                        }

                        override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                            Toast.makeText(FireApplication.getFireApplication(),
                                "통신 상태가 좋지 않아 로그인에 실패하였습니다\n잠시후 다시 시도해 주시기 바랍니다",
                                Toast.LENGTH_SHORT).show()
                            Log.e(MY_DEBUG_TAG, t.toString())
                        }
                    })

                } else {
                    if (userID.length() < 13) {
                        userIDLayout.error = "디지털 코드는 13자리 입니다"
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(MY_DEBUG_TAG,"LoginFragment - onAttach")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MY_DEBUG_TAG,"LoginFragment - onCreate")

    }

    override fun onStart() {
        super.onStart()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onStart")

    }

    override fun onResume() {
        super.onResume()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onResume")

    }

    override fun onPause() {
        super.onPause()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onStop")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onDestroyView")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onDestroy")

    }

    override fun onDetach() {
        super.onDetach()
        Log.d(MY_DEBUG_TAG,"LoginFragment - onDetach")

    }
}