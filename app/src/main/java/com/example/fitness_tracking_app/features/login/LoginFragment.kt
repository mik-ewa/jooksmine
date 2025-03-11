package com.example.fitness_tracking_app.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness_tracking_app.base.DataState
import com.example.fitness_tracking_app.features.create_new_account.SignUpFragment
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragLoginBinding
import com.example.fitness_tracking_app.features.activity_login.LoginActivity
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.utils.inputChanges
import com.example.fitness_tracking_app.utils.isEmailCorrect
import com.example.fitness_tracking_app.utils.isPasswordCorrect
import com.example.fitness_tracking_app.features.reset_password.ResetPasswordFragment
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            inputEmail.inputChanges().onEach { checkFields() }.launchIn(lifecycleScope)
            inputPassword.inputChanges().onEach { checkFields() }.launchIn(lifecycleScope)

            buttonLogIn.setOnClickListener {
                viewModel.login(inputEmail.text.toString(), inputPassword.text.toString())
            }
            viewCreateNewAccount.setOnClickListener {
                (requireActivity() as LoginActivity).nextFragment(SignUpFragment())
            }
            viewForgotPassword.setOnClickListener {
                (requireActivity() as LoginActivity).nextFragment(ResetPasswordFragment())
            }
        }
    }

    private fun initViewModel() {
        binding.apply {
            viewModel.loginStatus.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        val intent = Intent((requireActivity() as LoginActivity), MainActivity::class.java)
                        startActivity(intent)
                        (requireActivity() as LoginActivity).finishAffinity()
                    }
                    is Resource.Error ->  {
                        errorFirebase.text = it.message
                        errorFirebase.visible()
                        inputPassword.text.clear()
                    }
                    else -> {
                        errorFirebase.gone()
                        binding.buttonLogIn.setButtonState(DataState.Loading)
                    }
                }
            }
        }
    }

    private fun checkFields() {
        binding.buttonLogIn.setState(
            binding.inputEmail.text.isNotBlank() && binding.inputEmail.text.toString().isEmailCorrect() && binding.inputPassword.text.isNotBlank() &&
                 binding.inputPassword.text.toString().isPasswordCorrect()
        )
    }
}
