package com.example.fitness_tracking_app.features.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.databinding.FragActivitiesBinding
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.JointActivityAdapterData
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible

class JointActivitiesFragment : BaseFragment<FragActivitiesBinding>() {

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragActivitiesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val jointActivityAdapterData = arguments?.getParcelable<JointActivityAdapterData>(BundleKeys.JOINT_ACTIVITY_ADAPTER_DATA)

        binding.apply {
            if (jointActivityAdapterData?.jointActivityList.isNullOrEmpty()) {
                tvNoJointActivities.visible()
                rvActivities.gone()
            } else {
                tvNoJointActivities.gone()
                rvActivities.visible()
                rvActivities.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    adapter = JointActivitiesAdapter(jointActivityAdapterData)
                }
            }
        }
    }

    companion object {
        fun newInstance(
            jointActivityAdapterData: JointActivityAdapterData?,
        ) = JointActivitiesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(BundleKeys.JOINT_ACTIVITY_ADAPTER_DATA, jointActivityAdapterData)
            }
        }
    }
}
