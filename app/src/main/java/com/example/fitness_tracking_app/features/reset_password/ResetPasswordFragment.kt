package com.example.fitness_tracking_app.features.reset_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragResetPasswordBinding
import com.example.fitness_tracking_app.features.activity_login.LoginActivity
import com.example.fitness_tracking_app.features.login.LoginFragment
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.inputChanges
import com.example.fitness_tracking_app.utils.isEmailCorrect
import com.example.fitness_tracking_app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment<FragResetPasswordBinding>() {

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragResetPasswordBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            inputEmail.inputChanges().onEach {
                buttonSendCode.setState(inputEmail.text.toString().isEmailCorrect())
            }.launchIn(lifecycleScope)

            buttonSendCode.setOnClickListener {
                viewModel.sendPasswordResetEmail(binding.inputEmail.text.toString())
            }
            arrowBack.setOnClickListener {
                (requireActivity() as LoginActivity).onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initViewModel() {
        viewModel.resetPasswordResult.observe(viewLifecycleOwner) {
            if (it is Resource.Success) {
                showToast(getString(R.string.code_sent_to_an_email, binding.inputEmail.text.toString()))
                (requireActivity() as LoginActivity).nextFragment(LoginFragment())
            } else {
                showToast(it.message ?: getString(R.string.error_generic))
            }
        }
    }
}
