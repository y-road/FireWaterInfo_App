package com.sesac.firewaterinfo.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.*
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.LoginResult
import com.sesac.firewaterinfo.common.data.MyFavorites
import com.sesac.firewaterinfo.databinding.FragmentMyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFragment : Fragment() {

    companion object {
        fun newInstance(): MyFragment {
            return MyFragment()
        }
    }

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)

        binding.searchHydrantInStar.setOnClickListener {
            (requireActivity() as MainActivity).clickBottomMenu(R.id.fireHydrantItem)
        }
        asyncRequestMyFavorites()
        return binding.root
    }

    fun asyncRequestMyFavorites() {
        with(binding) {
            progressbar2.visibility = View.VISIBLE

            if (LOG_ON_STATUS != null) {
                if (LOG_ON_STATUS!!.result) {
                    val firewaterService = RetrofitOkHttpManager.firewaterRESTService
                    val call: Call<List<MyFavorites>> =
                        firewaterService.selectMyFavorites(LOG_ON_STATUS!!.digital_code)

                    call.enqueue(object : Callback<List<MyFavorites>> {
                        override fun onResponse(
                            call: Call<List<MyFavorites>>,
                            response: Response<List<MyFavorites>>,
                        ) {
                            if (response.isSuccessful) {
                                val resultList = response.body() as List<MyFavorites>

                                if (resultList.isEmpty()) {
                                    binding.listMyStarCardView.visibility = View.GONE
                                    binding.infoNoStar.visibility = View.VISIBLE
                                    MY_FAVORITE = resultList

                                } else {
                                    binding.listMyStarCardView.visibility = View.VISIBLE
                                    binding.infoNoStar.visibility = View.GONE
                                    refreshMyFavoriteRecycler(resultList)
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<MyFavorites>>, t: Throwable) {
                            Toast.makeText(FireApplication.getFireApplication(),
                                "즐겨찾기 정보를 불러오는데 실패했어요",
                                Toast.LENGTH_SHORT).show()
                            Log.e(MY_DEBUG_TAG, t.toString())
                        }

                    })
                }
            }
            progressbar2.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            swipeContainer.setOnRefreshListener {
                asyncRequestMyFavorites()

                Handler(Looper.getMainLooper()).postDelayed({
                    swipeContainer.isRefreshing = false
                }, 1100L)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            asyncRequestMyFavorites()
        }
    }

    private fun refreshMyFavoriteRecycler(favoriteList: List<MyFavorites>) {
        with(binding.homeRecycler) {

            MY_FAVORITE = favoriteList
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this.context,
                LinearLayoutManager(this.context).orientation))
            adapter = HomeRecyclerAdapter(favoriteList, requireActivity())
        }
    }
}