package com.example.fitness_tracking_app.features.complete_sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.UserLoginData
import com.example.fitness_tracking_app.databinding.FragCompleteSignUpBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.home.HomeFragment
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.filterSpaces
import com.example.fitness_tracking_app.utils.filterSpacesAndNumbers
import com.example.fitness_tracking_app.utils.inputChanges
import com.example.fitness_tracking_app.utils.invisible
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CompleteSignUpFragment : BaseFragment<FragCompleteSignUpBinding>() {

    private var selectedItem: String? = ""
    private var isUsernameTaken: Boolean = false
    private var username: String = ""
    private var name: String = ""
    private val viewModel: CompleteSignUpViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragCompleteSignUpBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(false)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            textLogOut.setOnClickListener { (requireActivity() as MainActivity).signOut() }
            inputUsername.inputChanges().onEach { username = inputUsername.filterSpaces()
                if (username.isNotEmpty()) { viewModel.checkUsername(username) }
            }.launchIn(lifecycleScope)

            inputName.inputChanges().onEach {
                name = inputName.filterSpacesAndNumbers()
                areRequiredFieldsSelected()
            }.launchIn(lifecycleScope)

            radioGroupGender.setOnCheckedChangeListener { radio, checkedId ->
                selectedItem = radio.findViewById<RadioButton>(checkedId)?.text.toString()
                areRequiredFieldsSelected()
            }

            inputPhoneNumber.inputChanges().onEach { areRequiredFieldsSelected() }.launchIn(lifecycleScope)

            buttonCompleteSignUp.setOnClickListener {
                saveUserDataToFirebase()
            }
        }
    }

    private fun saveUserDataToFirebase() {
        binding.apply {
            viewModel.saveUserDataToFirebase(
                UserLoginData(
                    username = username,
                    name = name,
                    gender = selectedItem!!,
                    phoneNumber = inputPhoneNumber.text.toString().toLong(),
                    weight = if (inputWeight.text.isNullOrEmpty()) null else inputWeight.text.toString().toDouble(),
                    height = if (inputHeight.text.isNullOrEmpty()) null else inputHeight.text.toString().toInt(),
                    date = System.currentTimeMillis()
                )
            )
        }
    }

    private fun initViewModel() {
        viewModel.saveUserStatus.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                goToHomeFragment()
            } else if (result is Resource.Error) {
                showError(result.message)
            }
        }
        viewModel.usernameResource.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                isUsernameTaken = result.data!!
                if (isUsernameTaken) binding.errorUsername.visible() else binding.errorUsername.invisible()
                areRequiredFieldsSelected()
            } else if (result is Resource.Error) {
                showError(result.message)
            }
        }
        viewModel.friendsStatus.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                goToHomeFragment()
            } else {
                showError(result.message)
                binding.buttonCompleteSignUp.setOnClickListener {
                    viewModel.addDefaultFriend()
                }
            }
        }
    }

    private fun areRequiredFieldsSelected() {
        binding.apply {
            buttonCompleteSignUp.setState(
                (radioButtonFemale.isChecked || radioButtonMale.isChecked) && inputPhoneNumber.length() == 9 && name.length > 2 && !isUsernameTaken
            )
        }
    }

    private fun goToHomeFragment() {
        (requireActivity() as MainActivity).nextFragment(HomeFragment(), addToBackStack = false)
    }

    private fun showError(errorMessage: String?) {
        showToast(errorMessage ?: getString(R.string.error_generic_try_again))
    }
}
