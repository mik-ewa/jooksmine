package com.example.fitness_tracking_app.features.motivation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.databinding.FragMotivationBinding
import com.example.fitness_tracking_app.features.MotivationOption
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MotivationFragment : BaseFragment<FragMotivationBinding>() {

    @Inject lateinit var dialogManager: DialogManager

    private val viewModel: MotivationViewModel by viewModels()

    private val motivationTexts = listOf(
        MotivationOption.FOR_FUN,
        MotivationOption.WEIGHT_LOSS,
        MotivationOption.PHYSICAL_CONDITION,
        MotivationOption.HEALTH,
        MotivationOption.CUSTOM,
        MotivationOption.NONE
    )

    private val listener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        val selectedOption = binding.radioGroupMotivation.findViewById<RadioButton>(checkedId)?.tag as? String
        if (selectedOption == MotivationOption.CUSTOM.key) {
            showMotivationDialog { motivation ->
                viewModel.saveMotivation(MotivationOption.fromKey(selectedOption), motivation)
            }
        } else {
            viewModel.saveMotivation(MotivationOption.fromKey(selectedOption))
        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragMotivationBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        initRadioButtons()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.your_motivation))
            header.setOnBackClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        }
    }

    private fun initRadioButtons() {
        motivationTexts.forEach { option ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = getString(option.titleId)
                tag = option.key
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.height_40)
                ).apply {
                    setMargins(
                        resources.getDimensionPixelSize(R.dimen.margin_5),
                        resources.getDimensionPixelSize(R.dimen.margin_5),
                        resources.getDimensionPixelSize(R.dimen.margin_5),
                        resources.getDimensionPixelSize(R.dimen.margin_5)
                    )
                }
                background = if (option == MotivationOption.NONE) {
                    ContextCompat.getDrawable(context, R.drawable.bg_radio_button_remove_color)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.bg_radio_button)
                }
                typeface = ResourcesCompat.getFont(requireContext(), R.font.kanit_light)

                buttonDrawable = null
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 14f
            }
            binding.radioGroupMotivation.addView(radioButton)
        }
    }

    private fun initViewModel() {
        viewModel.motivation.observe(viewLifecycleOwner) { result ->
            val selectedOption = MotivationOption.fromKey(result?.key)
            binding.radioGroupMotivation.setOnCheckedChangeListener(null)
            binding.radioGroupMotivation.children.forEach { view ->
                if (view is RadioButton) {
                    view.isChecked = (view.tag == selectedOption.key)
                }
            }

            if (selectedOption == MotivationOption.CUSTOM) {
                binding.myMotivationText.visible()
                binding.myMotivationText.text = getString(
                    R.string.my_motivation_is_input,
                    result?.customText ?: getString(R.string.just_for_fun)
                )
            } else {
                binding.myMotivationText.gone()
            }
            binding.radioGroupMotivation.setOnCheckedChangeListener(listener)
        }
    }

    private fun showMotivationDialog(onClick: (String) -> Unit) {
        dialogManager.showMotivationDialog(
            onButtonClick = { onClick.invoke(it as String) }
        )
    }
}
