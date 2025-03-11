package com.example.fitness_tracking_app.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.database.Motivation
import com.example.fitness_tracking_app.databinding.FragHomeBinding
import com.example.fitness_tracking_app.features.MotivationOption
import com.example.fitness_tracking_app.features.SharedHomeViewModel
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragHomeBinding>() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedHomeViewModel: SharedHomeViewModel by activityViewModels()
    private val combinedData = MediatorLiveData<List<HomeRecyclerItem>>()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragHomeBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
        (requireActivity() as MainActivity).showBottomNavBar(true)
    }

    private fun initView() {
        binding.swipeRefreshLayout.setOnRefreshListener { refreshData() }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.isRefreshing = false
        (requireActivity() as MainActivity).fetchWeather()
    }

    private fun initViewModel() {
        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())
        val adapter = HomeAdapter(requireContext(), emptyList())
        binding.rvHome.adapter = adapter
        combinedData.addSource(sharedHomeViewModel.weatherInfo) { weather ->
            combineData(
                weather,
                homeViewModel.motivation.value?.let { HomeRecyclerItem.MotivationItem(showMotivationText(it)) })
        }
        combinedData.addSource(homeViewModel.motivation) { motivation ->
            combineData(
                sharedHomeViewModel.weatherInfo.value,
                HomeRecyclerItem.MotivationItem(showMotivationText(motivation))
            )
        }
        combinedData.observe(viewLifecycleOwner) { data ->
            adapter.updateData(data)
        }
    }

    private fun showMotivationText(motivation: Motivation?): String? {
        return when (val motivationOption = MotivationOption.fromKey(motivation?.key)) {
            MotivationOption.CUSTOM -> { motivation?.customText }

            else -> { getRandomMotivation(motivationOption.arrayId) }
        }
    }

    private fun getRandomMotivation(array: Int): String {
        val textsArray = resources.getStringArray(array)
        return textsArray.random()
    }

    private fun combineData(
        weather: HomeRecyclerItem.WeatherItem?,
        motivation: HomeRecyclerItem.MotivationItem?
    ) {
        val combinedList = mutableListOf<HomeRecyclerItem>()
        motivation?.let { combinedList.add(it) }
        weather?.let { combinedList.add(it) }
        combinedData.value = combinedList
    }
}
