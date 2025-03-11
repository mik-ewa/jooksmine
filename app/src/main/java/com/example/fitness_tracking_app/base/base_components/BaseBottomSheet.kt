package com.example.fitness_tracking_app.base.base_components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet <T: ViewBinding> : BottomSheetDialogFragment() {

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

    override fun onStart() {
        super.onStart()
        setParams()
    }

    private fun setParams() {
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.93).toInt()
            it.layoutParams = layoutParams

            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): T
}
