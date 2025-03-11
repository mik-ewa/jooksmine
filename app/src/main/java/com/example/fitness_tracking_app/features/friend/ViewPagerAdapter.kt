package com.example.fitness_tracking_app.features.friend

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fitness_tracking_app.data.ActivitiesPagerData
import com.example.fitness_tracking_app.data.RunActivitiesList
import com.example.fitness_tracking_app.utils.GlobalStrings

class ViewPagerAdapter(
    fragment: Fragment,
    private val data: ActivitiesPagerData
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ActivitiesFragment.newInstance(
                    runList = RunActivitiesList.createFromFirebaseRunDataList(data.activityList, data.friendsPhoto)
                )
            }

            1 -> {
                JointActivitiesFragment.newInstance(
                    jointActivityAdapterData = null
                )
            }

            else -> {
                throw Exception(GlobalStrings.ERROR_GENERIC)
            }
        }
    }
}