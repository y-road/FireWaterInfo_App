package com.sesac.firewaterinfo.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import com.sesac.firewaterinfo.LOG_ON_STATUS
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.MyFavorites
import com.sesac.firewaterinfo.common.data.SearchRecyclerAdapter
import com.sesac.firewaterinfo.databinding.FragmentSearchBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var myCompositeDisposable = CompositeDisposable()

    private var isSearchBtnGlass = true // true : 돋보기 상태, false : x 상태

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        with(binding) {

            val editTextChangeObservable: InitialValueObservable<CharSequence> =
                searchText.textChanges()

            val searchEditTextSubscription: Disposable =
                editTextChangeObservable.debounce(800, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                        onNext = {
                            Log.d(MY_DEBUG_TAG, "searchEditTextSubscription - onNext: $it")

                            if (checkText(it.trim().toString())) {

                                val firewaterService = RetrofitOkHttpManager.firewaterRESTService
                                val call: Call<List<MyFavorites>> =
                                    firewaterService.selectSearch(it.toString())

                                call.enqueue(object : Callback<List<MyFavorites>> {
                                    override fun onResponse(
                                        call: Call<List<MyFavorites>>,
                                        response: Response<List<MyFavorites>>
                                    ) {
                                        if (response.isSuccessful) {
                                            val resultList = response.body() as List<MyFavorites>

                                            if (resultList.isEmpty()) {
                                                noResultTV.visibility = View.VISIBLE
                                            } else {
                                                noResultTV.visibility = View.GONE
                                                refreshRV(resultList)
                                            }
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<List<MyFavorites>>,
                                        t: Throwable
                                    ) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        },
                        onComplete = {
                            Log.d(MY_DEBUG_TAG, "searchEditTextSubscription - onComplete")
                        },
                        onError = {
                            Log.d(MY_DEBUG_TAG, "searchEditTextSubscription - onError: $it")
                        }
                    )
            myCompositeDisposable.add(searchEditTextSubscription)

            searchText.setOnEditorActionListener { v, keyCode, event ->

                var handled = false
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(this@SearchFragment.context,
                        "${searchText.text}",
                        Toast.LENGTH_SHORT).show()
                    handled = true
                    (activity as MainActivity).hideKeyboard(true)
                    searchText.clearFocus()
                    searchBtn.setBackgroundResource(R.drawable.ic_baseline_cancel_24)
                }
                handled
            }

            searchBtn.setOnClickListener {

                if (searchText.text.isNotEmpty()) {
                    if (isSearchBtnGlass) {
                        Toast.makeText(this@SearchFragment.context,
                            "${searchText.text}",
                            Toast.LENGTH_SHORT)
                            .show()
                        (activity as MainActivity).hideKeyboard(true)
                        searchText.clearFocus()
                    } else {
                        with(searchText) {
                            text.clear()
                            requestFocus()
                            (activity as MainActivity).hideKeyboard(false)
                        }
                    }

                    isSearchBtnGlass = !isSearchBtnGlass
                    if (isSearchBtnGlass) {
                        searchBtn.setBackgroundResource(R.drawable.ic_baseline_search_24)
                    } else {
                        searchBtn.setBackgroundResource(R.drawable.ic_baseline_cancel_24)
                    }
                }
            }



            return root
        }
    }

    private fun checkText(txt: String) : Boolean {

        if (txt.length > 1) {
            return !TextUtils.isDigitsOnly(txt)
        }
        return false
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchText.requestFocus()

    }

    private fun refreshRV(searchList: List<MyFavorites>) {
        with(binding.searchResultRV) {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = SearchRecyclerAdapter(searchList, requireActivity())
        }
    }

    override fun onDestroy() {
        Log.d(MY_DEBUG_TAG, "onDestroy= $this")
        this.myCompositeDisposable.clear()
        super.onDestroy()
    }

}