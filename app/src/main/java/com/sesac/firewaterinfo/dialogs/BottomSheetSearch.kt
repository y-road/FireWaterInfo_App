package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.R
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.MyFavorites
import com.sesac.firewaterinfo.common.data.SearchRecyclerAdapter
import com.sesac.firewaterinfo.databinding.BottomSearchBinding
import com.sesac.firewaterinfo.databinding.FragmentSearchBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class BottomSheetSearch: BottomSheetDialogFragment() {

    private var _binding: BottomSearchBinding? = null
    private val binding get() = _binding!!

    private var myCompositeDisposable = CompositeDisposable()

    private var isSearchBtnGlass = true // true : 돋보기 상태, false : x 상태

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BottomSearchBinding.inflate(inflater, container, false)

        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true

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
                    handled = true
                    searchBtn.performClick()
                }
                handled
            }

            searchBtn.setOnClickListener {

                if (searchText.text.isNotEmpty()) {
                    if (isSearchBtnGlass) {
                        if (searchText.text.toString().length < 2) {
                            Toast.makeText(FireApplication.getFireApplication(),
                                "검색어를 2자 이상 입력하세요",
                                Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            if (TextUtils.isDigitsOnly(searchText.text.toString())) {
                                Toast.makeText(FireApplication.getFireApplication(),
                                    "숫자만 검색할 수는 없어요",
                                    Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val imm = requireActivity().applicationContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(searchBtn.windowToken, 0)

                                searchText.clearFocus()

                                isSearchBtnGlass = !isSearchBtnGlass
                                if (isSearchBtnGlass) {
                                    searchBtn.setBackgroundResource(R.drawable.ic_baseline_search_24)
                                } else {
                                    searchBtn.setBackgroundResource(R.drawable.ic_baseline_cancel_24)
                                }

                            }
                        }
                    } else {
                        with(searchText) {
                            text.clear()
                            requestFocus()
                        }

                        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                        isSearchBtnGlass = !isSearchBtnGlass
                        if (isSearchBtnGlass) {
                            searchBtn.setBackgroundResource(R.drawable.ic_baseline_search_24)
                        } else {
                            searchBtn.setBackgroundResource(R.drawable.ic_baseline_cancel_24)
                        }
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
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }


    private fun refreshRV(searchList: List<MyFavorites>) {
        with(binding.searchResultRV) {
            layoutManager = LinearLayoutManager(requireActivity(), androidx.recyclerview.widget.RecyclerView.VERTICAL, false)
            adapter = SearchRecyclerAdapter(searchList, requireActivity())
        }
    }

    override fun onDestroy() {
        Log.d(MY_DEBUG_TAG, "onDestroy= $this")
        this.myCompositeDisposable.clear()
        super.onDestroy()
    }
}