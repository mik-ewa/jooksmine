package com.example.fitness_tracking_app.features.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.databinding.FragAboutBinding
import com.example.fitness_tracking_app.features.profile.ProfileFragment
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.friends.FriendsFragment
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.ItemConfigurator
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragAboutBinding>() {

    @Inject lateinit var itemConfigurator: ItemConfigurator
    @Inject lateinit var dialogManager: DialogManager

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragAboutBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(true)
        initView()
    }

    private fun initView() {
        binding.apply {
            itemConfigurator.applyContentForItemAbout(item = itemProfile, icon = R.drawable.icon_profile, text = R.string.profile, action = {
                (requireActivity() as MainActivity).nextFragment(ProfileFragment())
            })
            itemConfigurator.applyContentForItemAbout(item = itemActivities, icon = R.drawable.icon_activities, text = R.string.activities, action = {
                showNotImplementedToast()
            })
            itemConfigurator.applyContentForItemAbout(item = itemFriends, icon = R.drawable.icon_friends, text = R.string.friends, action = {
                (requireActivity() as MainActivity).nextFragment(FriendsFragment())
            })
            itemConfigurator.applyContentForItemAbout(item = itemChallenges, icon = R.drawable.icon_challenges, text = R.string.challenges, action = {
                showNotImplementedToast()
            })
            itemConfigurator.applyContentForItemAbout( item = itemLogOut, icon = R.drawable.icon_log_out, text = R.string.log_out, isViewLineVisible = false,
                action = {
                showDialog {
                    (requireActivity() as MainActivity).signOut()
                }
            })
        }
    }

    private fun showDialog(onClick: () -> Unit) {
        dialogManager.showSingleActionDialog(
            buttonText = getString(R.string.log_out),
            textQuestion = getString(R.string.are_you_sure_you_want_to_log_out),
            onButtonClick = {
                onClick.invoke()
            }
        )
    }
}