package com.sesac.firewaterinfo.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.HomeRecyclerAdapter
import com.sesac.firewaterinfo.common.RestFunction
import com.sesac.firewaterinfo.databinding.FragmentHomeBinding
import kotlinx.coroutines.*

class HomeFragment: Fragment() {
    companion object {
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (LOG_ON_STATUS.result) {
            if (MY_INFO != null) {
                try {
                    getPerson(MY_INFO!!.digital_code)
                    getMyFW(MY_INFO!!.digital_code)
                } catch (e: Exception) {
                    Log.d(MY_DEBUG_TAG,"e= $e")
                    Toast.makeText(this@HomeFragment.context,
                        "정보 불러오기에 실패했습니다\n앱을 재실행 해보시고 안되시면 관리자에게 문의바랍니다",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

//        getPerson(1020210011340)
//        getMyFW(1020210011340)

        with(binding) {

            swipeContainer.setOnRefreshListener {

                if (LOG_ON_STATUS.result) {
                    if (MY_INFO!!.digital_code != 0L) {
                        try {
//                            getPerson(MY_INFO!!.digital_code)
                            getMyFW(MY_INFO!!.digital_code)
                        } catch (e: Exception) {
                            Toast.makeText(this@HomeFragment.context,
                                "정보 불러오기에 실패했습니다\n앱을 재실행 해보시고 안되시면 관리자에게 문의바랍니다",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    swipeContainer.isRefreshing = false
                }, 1100L)
            }
        }
    }

    private fun getMyFW(digCode: Long) {

        with(CoroutineScope(Dispatchers.IO)) {
            val job1: Job = launch {
                MYFWS.clear()
                val rf = RestFunction()
                rf.selectMyFW(digCode)
            }
        }
        with(CoroutineScope(Dispatchers.Main)) {
            val job2: Job = launch {
                var ctFlag = true
                var tryCnt = 0

                while (ctFlag) {
                    if (++tryCnt > 6) ctFlag = false
                    Log.d(MY_DEBUG_TAG, "getMyFW tryCnt= $tryCnt")

                    if (MYFWS.size > 0) {
                        with(binding.homeRecycler) {
                            layoutManager = LinearLayoutManager(FireApplication.getFireApplication())
                            val dividerItemDecoration = DividerItemDecoration(this.context, LinearLayoutManager(FireApplication.getFireApplication()).orientation)
//                            addItemDecoration(dividerItemDecoration)
                            dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.divider_layer, null))
                            addItemDecoration(dividerItemDecoration)
                            adapter = HomeRecyclerAdapter(MYFWS, FireApplication.getFireApplication())
                        }
                        ctFlag = false
                    }
                    delay(200)
                }
            }
        }
    }

    private fun getPerson(digCode: Long) {
        with(CoroutineScope(Dispatchers.IO)) {
            val job1: Job = launch {
//                MY_INFO = null
                val rf = RestFunction()
                rf.selectMyInfo(digCode)
            }
        }
        with(CoroutineScope(Dispatchers.Main)) {
            val job2: Job = launch {
                var ctFlag = true
                var tryCnt = 0

                while (ctFlag) {
                    if (++tryCnt > 6) ctFlag = false
                    Log.d(MY_DEBUG_TAG, "getPerson tryCnt= $tryCnt")

                    if (MY_INFO!!.number != 0) {
                        with(binding) {
                            myBelong1TV.text = MY_INFO!!.belong_station
                            myBelong2TV.text = MY_INFO!!.belong_center
                            digitalCodeTV.text = MY_INFO!!.digital_code.toString()
                            myNameTV.text = MY_INFO!!.name
                            rankTV.text = MY_INFO!!.rank

                            if (MY_INFO!!.img_name.isNotEmpty()) {
                                val targetImage = "$PERSON_IMAGE_ADDRESS${MY_INFO!!.img_name}"
                                Glide.with(pictureIV)
                                    .load(Uri.parse(targetImage))
                                    .error(R.drawable.picture_back)
                                    .circleCrop()
                                    .into(pictureIV)
                            }
                            ctFlag = false
                        }
                    }
                    delay(200)
                }
            }
        }
    }

}