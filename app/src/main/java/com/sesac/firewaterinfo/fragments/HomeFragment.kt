package com.sesac.firewaterinfo.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sesac.firewaterinfo.MYFWS
import com.sesac.firewaterinfo.MY_DEBUG_TAG
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

        getMyFW(1020210011340)

        with(binding) {
            swipeContainer.setOnRefreshListener {

                getMyFW(1020210011340)

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
                    Log.d(MY_DEBUG_TAG, "tryCnt= $tryCnt")

                    if (MYFWS.size > 0) {
                        with(binding.homeRecycler) {
                            layoutManager = LinearLayoutManager(FireApplication.getFireApplication())
//                            val dividerItemDecoration = DividerItemDecoration(this.context, LinearLayoutManager(FireApplication.getFireApplication()).orientation)
//                            addItemDecoration(dividerItemDecoration)
                            adapter = HomeRecyclerAdapter(MYFWS, FireApplication.getFireApplication())
                        }
                        ctFlag = false
                    }
                    delay(200)
                }
            }
        }




    }

}