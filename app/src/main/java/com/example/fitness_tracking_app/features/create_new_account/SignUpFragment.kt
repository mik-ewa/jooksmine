package com.example.fitness_tracking_app.features.create_new_account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragSignUpBinding
import com.example.fitness_tracking_app.features.activity_login.LoginActivity
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.inputChanges
import com.example.fitness_tracking_app.utils.isEmailCorrect
import com.example.fitness_tracking_app.utils.isPasswordCorrect
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragSignUpBinding>() {

    private val viewModel: CreateNewAccountViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragSignUpBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            inputEmail.inputChanges().onEach { checkFields() }.launchIn(lifecycleScope)
            inputPassword.inputChanges().onEach { checkFields() }.launchIn(lifecycleScope)
            inputConfirmPassword.inputChanges().onEach { checkFields() }.launchIn(lifecycleScope)
            buttonCreateNewAccount.setOnClickListener {
                if (passwordsCheck()) {
                    viewModel.createUserWithEmailAndPassword(
                        inputEmail.text.toString(),
                        inputPassword.text.toString()
                    )
                }
            }
            arrowBack.setOnClickListener {
                (requireActivity() as LoginActivity).onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initViewModel() {
        viewModel.registerStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val intent = Intent((requireActivity() as LoginActivity), MainActivity::class.java)
                    startActivity(intent)
                    (requireActivity() as LoginActivity).finishAffinity()
                }

                is Resource.Error ->{
                    binding.errorFirebase.text = it.message ?: GlobalStrings.ERROR_GENERIC
                    binding.errorFirebase.visible()
                }
                else -> {
                    binding.errorFirebase.gone()
                    binding.errorPasswords.gone()
                }
            }
        }
    }

    private fun checkFields() {
        binding.buttonCreateNewAccount.setState(
            binding.inputEmail.text.isNotBlank() && binding.inputEmail.text.toString().isEmailCorrect()
                    && binding.inputPassword.text.isNotBlank()
                    && binding.inputPassword.text.toString().isPasswordCorrect()
                    && binding.inputConfirmPassword.text.toString().isPasswordCorrect()
        )
    }

    private fun passwordsCheck(): Boolean {
        binding.apply {
            return if (inputPassword.text.toString() != inputConfirmPassword.text.toString()) {
                errorPasswords.visible()
                inputPassword.text.clear()
                inputConfirmPassword.text.clear()
                false
            } else {
                true
            }
        }
    }
}