package com.sesac.firewaterinfo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.sesac.firewaterinfo.databinding.ActivityMainBinding
import com.sesac.firewaterinfo.fragments.HomeFragment
import com.sesac.firewaterinfo.fragments.LoginFragment
import com.sesac.firewaterinfo.fragments.MapFragment
import com.sesac.firewaterinfo.fragments.MyFragment
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fragmentHo: HomeFragment
    private lateinit var fragmentMa: MapFragment
    private lateinit var fragmentMy: MyFragment
    private lateinit var fragmentLo: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            fragmentHo = HomeFragment.newInstance()
            fragmentMa = MapFragment.newInstance()
            fragmentMy = MyFragment.newInstance()
            fragmentLo = LoginFragment.newInstance()
        }

        initialFragment(fragmentMa)

        with(binding) {

//            val bottomNavigation = bottomNavigationMold
            bottomNavigationMold.selectedItemId = R.id.fireHydrantItem

            bottomNavigationMold.setOnItemSelectedListener { item ->
                var bottomIndex = when (item.itemId) {
                    R.id.homeItem -> 0
                    R.id.fireHydrantItem -> 1
                    R.id.myInfoItem -> 2
                    else -> throw IllegalStateException("Unexpected value: " + item.itemId)
                }

                val nowFragment = getVisibleFragment()

                when (bottomIndex) {
                    0 -> {
                        if (LOG_ON_STATUS.result) {
                            fragmentChange(nowFragment, fragmentHo)
                        } else {
                            fragmentChange(nowFragment,fragmentLo)
                        }
                    }
                    1 -> {
                        if (LOG_ON_STATUS.result) {
                            removeFragmentByTag(fragmentLo)
                            clearMapTextBox()
                        }
                        fragmentChange(nowFragment, fragmentMa)
                    }
                    2 -> {
                        fragmentChange(nowFragment, fragmentMy)
                    }
                }
                true
            }
        }
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

    private fun removeFragmentByTag(fragment: Fragment) {
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
            bottomNavigationMold.selectedItemId = R.id.fireHydrantItem
        }

    }

    fun clearMapTextBox() {
        fragmentMa.clearSearchText()
    }

}