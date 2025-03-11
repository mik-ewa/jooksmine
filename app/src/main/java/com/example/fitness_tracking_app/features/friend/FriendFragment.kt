package com.example.fitness_tracking_app.features.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.ItemConfigurator
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.data.ActivitiesPagerData
import com.example.fitness_tracking_app.data.FriendBaseProfileData
import com.example.fitness_tracking_app.data.FriendChatData
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.databinding.FragFriendBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.messages.MessageBottomSheet
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.setGone
import com.example.fitness_tracking_app.utils.setVisible
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendFragment : BaseFragment<FragFriendBinding>() {

    @Inject lateinit var itemConfigurator: ItemConfigurator
    @Inject lateinit var dialogManager: DialogManager

    private val viewModel by viewModels<FriendViewModel>()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) = FragFriendBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val friendId = arguments?.getString(BundleKeys.FRIEND_ID) ?: return handleEmptyUser()
        viewModel.setFriendId(friendId)
        (requireActivity() as MainActivity).showBottomNavBar(false)
        initView()
        initViewModel()
    }

    private fun initView() {
        itemConfigurator.applyContentForItemAbout(
            item = binding.itemStatistics,
            icon = R.drawable.icon_statistics,
            text = R.string.statistics,
            action = { showNotImplementedToast() },
            isViewLineVisible = false
        )
    }

    private fun initViewModel() {
        viewModel.statusResource.observe(viewLifecycleOwner) { if (it is Resource.Error) { showToast(it.message ?: getString(R.string.error_generic)) } }
        viewModel.friendshipStatus.observe(viewLifecycleOwner) { status -> updateFriendshipStatus(status) }
        viewModel.combinedData.observe(viewLifecycleOwner) { combinedData ->
            if (combinedData is Resource.Loading) {
                binding.lottieRunList.visible()
            } else {
                binding.lottieRunList.gone()
                showViewPager(combinedData.data)
            }
        }
        viewModel.friendProfileBaseData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.flLoading.gone()
                    binding.clProfile.visible()
                    it.data?.let { friend -> handleBaseFriendData(friend) } ?: handleEmptyUser()
                }
                is Resource.Error -> { handleEmptyUser() }
                is Resource.Loading -> {
                    binding.clProfile.gone()
                    binding.flLoading.visible()
                }
            }
        }
    }

    private fun handleBaseFriendData(friendBaseProfileData: FriendBaseProfileData) {
        binding.apply {
            tvNickname.text = friendBaseProfileData.friendUsername
            Glide.with(requireContext()).load(friendBaseProfileData.friendImage).placeholder(R.drawable.placeholder_profile_photo).into(ivPhoto)
            ivMessage.setOnClickListener { openMessageSheet(friendBaseProfileData) }
        }
    }

    private fun showViewPager(data: ActivitiesPagerData?) {
        binding.apply {
            data?.let {
                pager.adapter = ViewPagerAdapter(fragment = this@FriendFragment, data = data)
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    tab.text = when (position) {
                        1 -> getString(R.string.joint_activity)
                        else -> getString(R.string.friend_s_activity, data.friendsName)
                    }
                }.attach()
            }
        }
    }

    private fun updateFriendshipStatus(status: FriendshipStatus?) {
        binding.apply {
            val isFriendAccepted = status == FriendshipStatus.ACCEPTED
            llFriendActivity.setVisible(isFriendAccepted)
            tvAddFriend.setGone(isFriendAccepted)
            btnFriendStatus.setStatus(status)
            btnFriendStatus.setOnClickListener { viewModel.changeFriendStatus(status) }
        }
    }

    private fun openMessageSheet(friend: FriendBaseProfileData) {
        val friendChatData = FriendChatData(
            friendId = friend.friendId!!,
            friendNickname = friend.friendUsername!!,
            friendPhoto = friend.friendImage!!
        )
        val bottomSheetFragment = MessageBottomSheet.newInstance(friendChatData = friendChatData)
        bottomSheetFragment.show(
            (requireActivity() as MainActivity).supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun handleEmptyUser() {
        (requireActivity() as MainActivity).onBackPressedDispatcher.onBackPressed()
        showToast(getString(R.string.error_this_user_does_not_exists))
    }

    private fun showRemoveFriendDialog(removeFriend: () -> Unit) {
        dialogManager.showSingleActionDialog(
            buttonText = getString(R.string.remove_friend),
            onButtonClick = {
                removeFriend.invoke()
            },
            textQuestion = getString(R.string.are_you_sure_you_want_to_remove_this_friend)
        )
    }
}
