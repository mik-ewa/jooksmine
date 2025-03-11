package com.example.fitness_tracking_app.features.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragProgressBinding
import com.example.fitness_tracking_app.monthsData
import com.example.fitness_tracking_app.progressMock
import com.example.fitness_tracking_app.statisticsYearData
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressFragment : BaseFragment<FragProgressBinding>() {

    private lateinit var progressAdapter: ProgressAdapter

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragProgressBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showNotImplementedToast()
        initView()
    }

    private val listener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        val radio: RadioButton = group.findViewById(checkedId) }

    private fun initView() {
        binding.radioGroupProgress.setOnCheckedChangeListener(listener)
        val lineChart = binding.lineChart

        val orangeColor = ContextCompat.getColor(requireContext(), R.color.orange150)

        val flexboxLayoutManager = FlexboxLayoutManager(context)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.CENTER

        progressAdapter = ProgressAdapter(progressMock)
        binding.rvProgress.apply {
            layoutManager = flexboxLayoutManager
            adapter = progressAdapter
        }

        //TODO: to change the mock
        val dataSet = LineDataSet(statisticsYearData, "km").apply {
            color = orangeColor
            lineWidth = 1.5f
            setCircleColor(orangeColor)
            circleRadius = 3f
            valueTextColor = orangeColor
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(monthsData)
            granularity = 1f
            textColor = Color.BLACK
            textSize = 10f
        }

        lineChart.legend.apply {
            form = Legend.LegendForm.LINE
            textColor = Color.BLACK
        }

        lineChart.apply {
            description.isEnabled = false
            invalidate()
        }
    }
}