package com.sesac.firewaterinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sesac.firewaterinfo.databinding.FragmentMyBinding

class MyFragment:Fragment() {

    companion object {
        fun newInstance() : MyFragment {
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
        return binding.root
    }
}