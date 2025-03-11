package com.example.fitness_tracking_app.features.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragNotificationsBinding
import com.example.fitness_tracking_app.utils.showNotImplementedToast

class NotificationsFragment : BaseFragment<FragNotificationsBinding>() {

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragNotificationsBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showNotImplementedToast()
        initView()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.notifications))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}