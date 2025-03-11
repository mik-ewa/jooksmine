package com.example.fitness_tracking_app.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.ItemConfigurator
import com.example.fitness_tracking_app.databinding.FragProfileBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.body_measurement_fragment.BodyMeasurementFragment
import com.example.fitness_tracking_app.features.motivation.MotivationFragment
import com.example.fitness_tracking_app.features.my_profile.MyProfileFragment
import com.example.fitness_tracking_app.features.notifications.NotificationsFragment
import com.example.fitness_tracking_app.features.personal_information.PersonalInformationFragment
import com.example.fitness_tracking_app.features.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()
    @Inject lateinit var itemConfigurator: ItemConfigurator

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragProfileBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(false)
        initViewModel()
        initView()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.profile))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            iconToProfile.setOnClickListener {
                (requireActivity() as MainActivity).nextFragment(MyProfileFragment())
            }
            itemConfigurator.applyContentForItemAbout(
                item = itemBodyMeasurements,
                icon = R.drawable.icon_body_measurements,
                text = R.string.body_measurements,
                action = {
                    (requireActivity() as MainActivity).nextFragment(BodyMeasurementFragment())
                })
            itemConfigurator.applyContentForItemAbout(
                item = itemMotivation,
                icon = R.drawable.icon_motivation,
                text = R.string.your_motivation,
                action = {
                    (requireActivity() as MainActivity).nextFragment(MotivationFragment())
                })
            itemConfigurator.applyContentForItemAbout(
                item = itemNotifications,
                icon = R.drawable.icon_notification,
                text = R.string.notifications,
                action = {
                    (requireActivity() as MainActivity).nextFragment(NotificationsFragment())
                })
            itemConfigurator.applyContentForItemAbout(
                item = itemPersonalInformaion,
                icon = R.drawable.icon_personal_information,
                text = R.string.personal_information,
                action = {
                    (requireActivity() as MainActivity).nextFragment(PersonalInformationFragment())
                },
            )
            itemConfigurator.applyContentForItemAbout(
                item = itemSettings,
                icon = R.drawable.icon_settings,
                text = R.string.settings,
                action = {
                    (requireActivity() as MainActivity).nextFragment(SettingsFragment())
                },
                isViewLineVisible = false
            )
        }
    }

    private fun initViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.textName.text = user.name
            binding.textUsername.text = user.username
            user?.profilePhoto.let { photo ->
                Glide.with((requireActivity() as MainActivity)).load(photo)
                    .placeholder(R.drawable.placeholder_profile_photo).into(binding.userPhoto)
            }
        }
    }
}
