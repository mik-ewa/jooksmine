package com.example.fitness_tracking_app.features.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseBottomSheet
import com.example.fitness_tracking_app.data.FriendChatData
import com.example.fitness_tracking_app.databinding.FragBottomSheetMessageBinding
import com.example.fitness_tracking_app.utils.BundleKeys

class MessageBottomSheet : BaseBottomSheet<FragBottomSheetMessageBinding>() {

    private lateinit var friendData : FriendChatData

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragBottomSheetMessageBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        friendData = arguments?.getParcelable(BundleKeys.FRIEND_CHAT_DATA)!!
        initView()
    }

    private fun initView() {
        binding.apply {
            ivClose.setOnClickListener { dismiss() }
            Glide.with(requireContext()).load(friendData.friendPhoto).into(ivProfilePhoto)
            tvNickname.text = friendData.friendNickname

            val dataBundle = Bundle().apply { putString(BundleKeys.FRIEND_ID,  friendData.friendId) }
            showChat(MessagesContentFragment(), dataBundle = dataBundle)
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

    companion object {
        fun newInstance(
            friendChatData: FriendChatData
        ): MessageBottomSheet {
            val fragment = MessageBottomSheet()
            val args = Bundle()
            args.putParcelable(BundleKeys.FRIEND_CHAT_DATA, friendChatData)
            fragment.arguments = args
            return fragment
        }
    }
}