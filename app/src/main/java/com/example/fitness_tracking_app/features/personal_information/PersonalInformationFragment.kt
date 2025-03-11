package com.example.fitness_tracking_app.features.personal_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.ItemConfigurator
import dagger.hilt.android.AndroidEntryPoint
import com.example.fitness_tracking_app.databinding.FragPersonalInformationBinding
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.showItemContainer
import com.example.fitness_tracking_app.utils.showToast
import javax.inject.Inject

@AndroidEntryPoint
class PersonalInformationFragment : BaseFragment<FragPersonalInformationBinding>() {

    @Inject lateinit var itemConfigurator: ItemConfigurator

    private val viewModel: PersonalInformationViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragPersonalInformationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.personal_information))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            itemConfigurator.applyContentForItemEdit(item = itemGender,
                text = R.string.gender_at_birth,
                icon = R.drawable.icon_gender,
                action = {
                    containerGender.showItemContainer(40)
                })
            itemConfigurator.applyContentForItemEdit(item = itemBirthdate,
                text = R.string.year_of_birth,
                icon = R.drawable.icon_birthdate,
                action = {
                    containerBirthdate.showItemContainer(40)
                })
            itemConfigurator.applyContentForItemEdit(
                item = itemHeight,
                text = R.string.height,
                icon = R.drawable.icon_height,
                action = {
                    containerHeight.showItemContainer(40)
                })
            itemConfigurator.applyContentForItemEdit(item = itemPhoneNumber,
                text = R.string.phone_number,
                icon = R.drawable.icon_phone,
                action = {
                    containerPhoneNumber.showItemContainer(40)
                })
        }
        binding.buttonSave.setOnClickListener {
            viewModel.updatePersonalData()
        }
    }

    private fun initViewModel() {
        viewModel.result.observe(viewLifecycleOwner) {result ->
            if (result is Resource.Success) {
                showToast(getString(R.string.data_successfully_updated))
            } else {
                showToast(result.message ?: getString(R.string.error_generic))
            }
        }
        viewModel.personalData.observe(viewLifecycleOwner) { personalData ->
            binding.apply {
                tvHeight.text = personalData?.height?.toString() ?: "??"
                tvYear.text = personalData?.birthday?.toString() ?: "??"
                etPhoneNumber.hint = personalData?.phoneNumber.toString()

                val heightValue = personalData?.height ?: 150
                icHeightMinus.setOnClickListener {
                    viewModel.updateHeight(heightValue-1)
                }
                icHeightPlus.setOnClickListener {
                    viewModel.updateHeight(heightValue+1)
                }

                val yearValue = personalData?.birthday ?: 1995
                icYearPlus.setOnClickListener {
                    viewModel.updateBirthYear(yearValue+1)
                }
                icYearMinus.setOnClickListener {
                    viewModel.updateBirthYear(yearValue-1)
                }
                
                rgGender.children.forEach {view ->
                    if (view is RadioButton) {
                        view.isChecked = (view.text.toString() == personalData?.gender.toString())
                    }
                }

                rgGender.setOnCheckedChangeListener { radioGroup, checkedId ->
                    val selectedOption = radioGroup.findViewById<RadioButton>(checkedId).text
                    viewModel.updateGender(selectedOption.toString())
                }
            }
        }
    }
}
