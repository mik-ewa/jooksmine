package com.example.fitness_tracking_app.features.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.FriendChatData
import com.example.fitness_tracking_app.databinding.FragMessagesBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.friend.FriendFragment
import com.example.fitness_tracking_app.features.home.HomeFragment
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.showGenericErrorToast
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import com.example.fitness_tracking_app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesFragment : BaseFragment<FragMessagesBinding>() {

    private lateinit var friendChatData: FriendChatData

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragMessagesBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(false)
        friendChatData = arguments?.getParcelable(BundleKeys.FRIEND_CHAT_DATA) ?: return handleEmptyData()
        initView()
    }

    private fun initView() {
        binding.apply {
            Glide.with(requireContext()).load(friendChatData.friendPhoto).into(ivProfilePhoto)
            tvNickname.text = friendChatData.friendNickname
            binding.iconIsActive.visibility = if (friendChatData.isFriendActive) View.VISIBLE else View.GONE
            iconCall.setOnClickListener { showNotImplementedToast() }
            iconBack.setOnClickListener {
                (requireActivity() as MainActivity).onBackPressedDispatcher.onBackPressed()
            }
            val dataBundle = Bundle().apply { putString(BundleKeys.FRIEND_ID, friendChatData.friendId) }
            tvNickname.setOnClickListener {
                (requireActivity() as MainActivity).nextFragment(fragment = FriendFragment(), dataBundle = dataBundle)
            }
            showChat(MessagesContentFragment(), Bundle().apply { putString(BundleKeys.FRIEND_ID, friendChatData.friendId) })
        }
    }

    private fun showChat(
        fragment: Fragment,
        dataBundle: Bundle? = null,
    ) {
        val ft: FragmentTransaction = childFragmentManager.beginTransaction()

        fragment.arguments = dataBundle

        ft.replace(R.id.cl_placeholder, fragment)

        ft.commit()
    }

    private fun handleEmptyData() {
        (requireActivity() as MainActivity).onBackPressedDispatcher.onBackPressed()
        showToast(getString(R.string.error_this_chat_does_not_exists))
    }
}