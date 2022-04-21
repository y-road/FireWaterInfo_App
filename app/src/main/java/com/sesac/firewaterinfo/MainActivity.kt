package com.sesac.firewaterinfo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.sesac.firewaterinfo.common.data.SharedPrefManager
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.databinding.ActivityMainBinding
import com.sesac.firewaterinfo.fragments.*
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var fragmentHo: HomeFragment // 내 정보 - 내가 수정한 소화전 목록
    lateinit var fragmentMa: MapFragment // 맵
    lateinit var fragmentMy: MyFragment // 나의 즐겨찾기
    lateinit var fragmentLo: LoginFragment // 로그인
//    lateinit var fragmentSe: SearchFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            fragmentHo = HomeFragment.newInstance()
            fragmentMa = MapFragment.newInstance()
            fragmentMy = MyFragment.newInstance()
            fragmentLo = LoginFragment.newInstance()
//            fragmentSe = SearchFragment.newInstance()
        }

        initialFragment(fragmentMa)

        with(binding) {

//            val bottomNavigation = bottomNavigationMold
            bottomNavigationMold.selectedItemId = R.id.fireHydrantItem

            bottomNavigationMold.setOnItemSelectedListener { item ->
                var bottomIndex = when (item.itemId) {
                    R.id.myFavorites -> 0 // 22.03.13: 내 정보 탭 -> 즐겨찾기 탭
                    R.id.fireHydrantItem -> 1
                    R.id.myCardInfo -> 2 // 이 탭에는 나의 인사카드와 나의 수정 이력이 들어감.
                    else -> throw IllegalStateException("Unexpected value: " + item.itemId)
                }

                var nowFragment = getVisibleFragment()
//                Log.d(MY_DEBUG_TAG, "getVisibleFragment: $nowFragment")

//                if (nowFragment == fragmentSe) {
//                    onBackPressed()
//                    nowFragment = getVisibleFragment()
//                }

                when (bottomIndex) {
                    0 -> {
                        fragmentChange(nowFragment, fragmentMy)
                    }
                    1 -> {
                        if (LOG_ON_STATUS != null && LOG_ON_STATUS!!.result) {
                            removeFragment(fragmentLo)
//                            clearMapTextBox()
                        }
                        if (isAliveLoginFragment()) {
//                            clearMapTextBox()
                        }
                        fragmentChange(nowFragment, fragmentMa)
                    }
                    2 -> {
                        if (LOG_ON_STATUS != null && LOG_ON_STATUS!!.result) {
                            fragmentChange(nowFragment, fragmentHo)
                        } else {
                            fragmentChange(nowFragment, fragmentLo)
                        }
                    }
                }
                true
            }

            val autoLoginInfo = SharedPrefManager.loadMyLoginInfo()

            if (autoLoginInfo != null) {
                LOG_ON_STATUS = autoLoginInfo
            }
        }
    }

    fun gotoAimMarker(fwList: MutableList<SimpleFW>) {

        if (fragmentMa.nowBottomSearchDialog != null) {
            fragmentMa.nowBottomSearchDialog!!.dismiss()
            fragmentMa.nowBottomSearchDialog = null
        }

//        var camPos = CameraPosition(LatLng(fwList[0].latitude, fwList[0].longitude), 16.0)
//        MapFragment.naverMap.cameraPosition = camPos
//
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(fwList[0].latitude, fwList[0].longitude))
            .animate(CameraAnimation.Easing)
        MapFragment.naverMap.moveCamera(cameraUpdate)

        fragmentMa.initMarker(fwList as List<SimpleFW>)

        MARKERSS[0].performClick()
    }

    private fun getVisibleFragment(): Fragment? {
        val fm = this.supportFragmentManager
        val fragments: List<Fragment> = fm.fragments

        for (fragment in fragments) {
            if (fragment != null && fragment.isVisible) {
                return fragment
            }
        }
        return null
    }

    private fun isAliveLoginFragment() = fragmentLo.isAdded

    fun removeFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        with(ft) {
            remove(fragment)
            commit()
        }
    }

    private fun initialFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        with(ft) {
            add(R.id.fragmentContainerFrame, fragment)
            commit()
        }
    }

    private fun fragmentChange(oldFragment: Fragment?, newFragment: Fragment) {

        val ft = supportFragmentManager.beginTransaction() // fragment transaction
        with(ft) {

            if (oldFragment == newFragment) return

            if (oldFragment != null) {
                hide(oldFragment)
            }

            if (!newFragment.isAdded) {
                add(R.id.fragmentContainerFrame, newFragment)
            } else {
                show(newFragment)
            }
            commit()
        }
    }

    fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun isAlreadyAdded(fragment: Fragment) : Boolean {
        return fragment.isAdded
    }

    fun hideKeyboard(hide: Boolean) {

        val view = this.currentFocus

        if (view != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            when (hide) {
                true -> imm.hideSoftInputFromWindow(view.windowToken, 0)
                else -> imm.showSoftInput(view, 0)
            }
        }
    }

    fun clickBottomMenu(id: Int) {

        with(binding) {
            bottomNavigationMold.selectedItemId = id
//            bottomNavigationMold.selectedItemId = R.id.fireHydrantItem
        }
    }

//    fun clearMapTextBox() {
//        fragmentMa.clearSearchText()
//    }

    fun mainFragmentChange(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        with(ft) {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
            commit()
        }
    }

    fun frameFragmentChange(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        with(ft) {
            replace(R.id.fragmentContainerFrame, fragment)
            addToBackStack(null)
            commit()
        }
    }

    fun viewBottomLayout(flag: Boolean) {
        binding.bottomNavigationMold.isVisible = flag
    }

//    override fun onPause() {
//        super.onPause()
//        Log.d(MY_DEBUG_TAG,"onPause= $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(MY_DEBUG_TAG,"onStop= $this")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(MY_DEBUG_TAG,"onDestroy= $this")
//    }
}
