package com.example.fitness_tracking_app.base.base_components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment <T: ViewBinding> : Fragment() {

    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(layoutInflater, container)
        return binding.root
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}