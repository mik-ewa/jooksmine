package com.example.fitness_tracking_app.features.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.fitness_tracking_app.base.base_components.BaseBottomSheet
import com.example.fitness_tracking_app.databinding.FragBottomSheetNewMessageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewMessageBottomSheet : BaseBottomSheet<FragBottomSheetNewMessageBinding>() {

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragBottomSheetNewMessageBinding.inflate(layoutInflater, container, false)

    override fun onStart() {
        super.onStart()
        binding.apply {
            ivClose.setOnClickListener { dismiss() }
        }
    }
}