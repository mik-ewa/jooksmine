package com.example.fitness_tracking_app.features.delete_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragDeleteAccountBinding
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountFragment : BaseFragment<FragDeleteAccountBinding>() {

    private val viewModel: DeleteAccountViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragDeleteAccountBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            btnDelete.setOnClickListener {
                if (edPassword.text.isNotBlank()) {
                    viewModel.checkCredentials(edPassword.text.toString())
                }
            }
        }
    }

    private fun initViewModel() {
        binding.apply {
            viewModel.removeResult.observe(viewLifecycleOwner) { result ->
                if (result is Resource.Error) {
                    tvError.visible()
                    flLoading.gone()
                    tvError.text = result.message
                } else if (result is Resource.Loading) {
                    tvError.gone()
                    flLoading.visible()
                }
            }
        }
    }
}