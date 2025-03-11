package com.example.fitness_tracking_app.features.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.databinding.FragActivitiesBinding
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.RunActivitiesList
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible

class ActivitiesFragment : BaseFragment<FragActivitiesBinding>() {

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragActivitiesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val runList = arguments?.getParcelable<RunActivitiesList>(BundleKeys.RUN_LIST)

        binding.apply {
            if (runList?.activitiesList.isNullOrEmpty()) {
                tvNoActivities.visible()
                rvActivities.gone()
            } else {
                tvNoActivities.gone()
                rvActivities.visible()
                rvActivities.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    adapter = ActivitiesAdapter(context,  runList = runList!!)
                }
            }
        }
    }

    companion object {
        fun newInstance(runList: RunActivitiesList) = ActivitiesFragment().apply {
            arguments = Bundle().apply { putParcelable(BundleKeys.RUN_LIST ,runList) }
        }
    }
}
