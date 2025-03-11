package com.example.fitness_tracking_app.features.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.ChatMetadata
import com.example.fitness_tracking_app.data.MessageDataSender
import com.example.fitness_tracking_app.databinding.FragMessagesContentBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.home.HomeFragment
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesContentFragment : BaseFragment<FragMessagesContentBinding>() {

    private val viewModel: MessageViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragMessagesContentBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(BundleKeys.FRIEND_ID)?.let { id -> viewModel.fetchFromMyBase(id) }
            ?: handleEmptyId()
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            buttonSend.setOnClickListener {
                if ((requireActivity() as MainActivity).networkTracker.isConnectedToInternet()) {
                    val message = etMessage.text
                    if (!message.isNullOrBlank()) viewModel.sendMessage(
                        message = message.trim().toString()
                    )
                    message.clear()
                } else {
                    showToast(GlobalStrings.ERROR_NO_INTERNET_CONNECTION)
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Success -> {
                        if (it.data?.second.isNullOrEmpty()) {
                            llSendNewMessage.visible()
                            rvMessages.gone()
                        } else {
                            llSendNewMessage.gone()
                            rvMessages.visible()
                            showMessages(it.data)
                        }
                    }

                    is Resource.Error -> {}

                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun showMessages(messages: Pair<ChatMetadata?, List<MessageDataSender>?>?) {
        val rvMessages = binding.rvMessages
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this.context)
        }
        val adapter = messages?.let { data ->
            MessagesAdapter(data.second!!, messages.first?.lastRead)
        }
        messages?.second?.size?.let { position ->
            rvMessages.scrollToPosition(position - 1)
        }
        rvMessages.adapter = adapter

        rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvMessages.canScrollVertically(1)) {
                    viewModel.readChat()
                }
            }
        })
    }

    private fun handleEmptyId() {
        (requireActivity() as MainActivity).nextFragment(HomeFragment(), addToBackStack = false, hasAnimation = false)
        showToast(getString(R.string.error_this_chat_does_not_exists))
    }
}
