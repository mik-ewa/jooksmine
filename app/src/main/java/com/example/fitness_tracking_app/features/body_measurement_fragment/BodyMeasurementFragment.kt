package com.example.fitness_tracking_app.features.body_measurement_fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.databinding.FragBodyMeasurementBinding
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.invisible
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BodyMeasurementFragment : BaseFragment<FragBodyMeasurementBinding>() {

    @Inject lateinit var dialogManager: DialogManager

    private val viewModel: BodyMeasurementViewModel by viewModels()

    private lateinit var adapter: BodyMeasurementAdapter
    private lateinit var weightList: List<Weights>

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragBodyMeasurementBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.body_measurements))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            rvHistory.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this.context)
            }
        }
    }


    private fun initViewModel() {
        viewModel.bmi.observe(viewLifecycleOwner) { bmi ->
            binding.apply {
                clBmi.visible()
                tvBmi.text = bmi
            }
        }

        viewModel.weightList.observe(viewLifecycleOwner) { list ->
            this.weightList = list ?: emptyList()
            val mutableWeightList = this.weightList.toMutableList()

            adapter = BodyMeasurementAdapter(mutableWeightList) {
                showDeleteRecordDialog {
                    viewModel.deleteWeight(it)
                    mutableWeightList.remove(it)
                    updateAdapter(mutableWeightList)
                }
            }

            binding.buttonAddMeasurements.setOnClickListener {
                dialogManager.showMeasurementsDialog(onClick = { weight ->
                    viewModel.addWeight(weight)
                    mutableWeightList.add(element = weight)
                    updateAdapter(mutableWeightList)
                })
            }

            binding.rvHistory.scrollToPosition(mutableWeightList.size.minus(1))
            binding.rvHistory.adapter = adapter
            updateAdapter(mutableWeightList)
        }
        viewModel.personalData.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Error) {
                showToast(result.message ?: GlobalStrings.ERROR_GENERIC)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(weightList: List<Weights>) {
        adapter.notifyDataSetChanged()
        viewModel.calculateBMI(weight = weightList.last().weight)
        updateViewState(weightList)
    }

    private fun updateViewState(weightList: List<Weights>) {
        binding.apply {
            if (weightList.isNotEmpty()) {
                tvNoData.gone()
                llMeasurementData.visible()
                tvWeight.text = getString(R.string.w_kg, weightList.last().weight.toString())
                rvHistory.scrollToPosition(weightList.size - 1)
            } else {
                llMeasurementData.invisible()
                tvNoData.visible()
            }
        }
    }

    private fun showDeleteRecordDialog(deleteData: () -> Unit) {
        dialogManager.showSingleActionDialog(
            buttonText = getString(R.string.remove_record),
            textQuestion = getString(R.string.are_you_sure_you_want_to_remove_this_record),
            onButtonClick = { deleteData.invoke() }
        )
    }
}
