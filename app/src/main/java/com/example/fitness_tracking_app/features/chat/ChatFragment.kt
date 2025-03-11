package com.example.fitness_tracking_app.features.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.databinding.FragChatBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.ChatInfo
import com.example.fitness_tracking_app.features.SharedChatViewModel
import com.example.fitness_tracking_app.features.messages.MessagesFragment
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragChatBinding>() {

    private val sharedViewModel: SharedChatViewModel by activityViewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragChatBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(true)
        initViewModel()
        initView()
    }

    private fun initViewModel() {
        sharedViewModel.chatMapWithInfo.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when (result){
                    is Resource.Success -> {
                        showShimmer(false)
                        if (result.data.isNullOrEmpty()) {
                            llNoMess.visible()
                        } else {
                            llNoMess.gone()
                            handleChatList(result.data.values)
                        }
                    }
                    is Resource.Error -> {
                        showToast(result.message ?: "something went wrong")
                        showShimmer(false)
                    }
                    is Resource.Loading -> { showShimmer(true) }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            ivNewMessage.setOnClickListener {
                val bottomSheetFragment = NewMessageBottomSheet()
                bottomSheetFragment.show((requireActivity() as MainActivity).supportFragmentManager, bottomSheetFragment.tag)
                showNotImplementedToast()
            }
        }
    }

    private fun handleChatList(chats: Collection<ChatInfo>?) {
        if (chats?.isNotEmpty() == true) {
            binding.rvChat.apply { layoutManager = LinearLayoutManager(this.context) }
            val rvAdapter = ChatAdapter(chats.sortedByDescending { it.date },
                onClick = {
                    (requireActivity() as MainActivity).nextFragment(
                        MessagesFragment(),
                        dataBundle = Bundle().apply { putParcelable(BundleKeys.FRIEND_CHAT_DATA, it)},
                        hasAnimation = false
                    )
                }, onRemove = {})

            binding.rvChat.adapter = rvAdapter
            val swipeToDeleteCallback = SwipeToDeleteChat(rvAdapter)
            val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvChat)
        } else {
            binding.llNoMess.visible()
        }
    }

    private fun showShimmer(shouldShow: Boolean) {
        if (shouldShow) {
            binding.shimmerLayout.startShimmer()
            binding.shimmerLayout.visible()
        } else {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.gone()
        }
    }

    private fun removeMessage() {
        //            rvChat.apply {
//                layoutManager = LinearLayoutManager(this.context)
//            }
//            val rvAdapter = ChatAdapter(requireContext(), chatList.sortedByDescending { it.date }, {it}, onRemove = {
//
//            })
//            rvChat.adapter = rvAdapter
//            val swipeToDeleteCallback = SwipeToDeleteChat(rvAdapter)
//            val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
//            itemTouchHelper.attachToRecyclerView(rvChat)
//            ivNewMessage.setOnClickListener {
//                val bottomSheetFragment = NewMessageBottomSheet()
//                bottomSheetFragment.show((requireActivity() as MainActivity).supportFragmentManager, bottomSheetFragment.tag)
//            }
    }
}